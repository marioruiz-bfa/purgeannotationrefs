package org.dyndns.fichtner.purgeannotationrefs.ant.tasks;

import static org.dyndns.fichtner.purgeannotationrefs.Util.isClass;
import static org.dyndns.fichtner.purgeannotationrefs.Util.isZip;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.ant.types.AnnotationRemoveSelector;
import org.dyndns.fichtner.purgeannotationrefs.ant.types.Target;
import org.dyndns.fichtner.purgeannotationrefs.optimizer.ZipOptimizer;

public class PurgeAnnotationRefsTask extends MatchingTask {

	private Path src;
	private File targetDir;
	private boolean overwrite;

	private final List<AnnotationRemoveSelector> selectors = new ArrayList<AnnotationRemoveSelector>();
	private static final Map<Target, ElementType> MAP = Collections
			.unmodifiableMap(createMap());

	private static Map<Target, ElementType> createMap() {
		final Map<Target, ElementType> map = new EnumMap<Target, ElementType>(
				Target.class);
		map.put(Target.TYPES, ElementType.TYPE);
		map.put(Target.FIELDS, ElementType.FIELD);
		map.put(Target.CONSTRUCTORS, ElementType.CONSTRUCTOR);
		map.put(Target.METHODS, ElementType.METHOD);
		return map;
	}

	private static ElementType getElementType(final Target target) {
		final ElementType elementType = MAP.get(target);
		if (elementType == null) {
			throw new IllegalStateException(Target.class.getName()
					+ " unknown type " + target); //$NON-NLS-1$
		}
		return elementType;
	}

	private static InputStream createInputStream(final File srcFile,
			final boolean srcIsTarget) throws IOException,
			FileNotFoundException {
		return srcIsTarget ? readToMem(srcFile) : new FileInputStream(srcFile);
	}

	private static InputStream readToMem(final File file) throws IOException {
		final DataInputStream dis = new DataInputStream(new FileInputStream(
				file));
		try {
			final byte[] buffer = new byte[(int) file.length()];
			dis.readFully(buffer);
			return new ByteArrayInputStream(buffer);
		} finally {
			dis.close();
		}
	}

	/**
	 * Adds a configuration to the task.
	 * 
	 * @param ars the AnnotationRemoveSelector to add
	 */
	public void addConfiguredRemove(final AnnotationRemoveSelector ars) {
		if (ars.getMatcher() == null) {
			throw new BuildException(
					"missing argument for remove, either name or regexp has to be set"); //$NON-NLS-1$
		}
		// don't check target: it may be null (indicates "all")
		this.selectors.add(ars);
	}

	/**
	 * Forces overwrite (when using targetDir) even if targetfile is newer than
	 * sourcefile.
	 * 
	 * @param overwrite <code>true</code> forces overwriting
	 */
	public void setOverwrite(final boolean overwrite) {
		this.overwrite = overwrite;
	}

	private void doWork(final File srcFile,
			final AnnotationReferenceRemover remover) throws IOException {
		boolean srcIsTarget = this.targetDir == null;
		final File targetFile = srcIsTarget ? srcFile : new File(
				this.targetDir, srcFile.getName());
		// reevaluate
		srcIsTarget = targetFile.equals(srcFile);
		if (!srcIsTarget && !this.overwrite && targetFile.exists()
				&& targetFile.lastModified() > srcFile.lastModified()) {
			log("File " + targetFile + " is uptodate"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			log("Processing file " + srcFile); //$NON-NLS-1$
			if (isClass(srcFile.getName())) {
				final InputStream input = createInputStream(srcFile,
						srcIsTarget);
				final FileOutputStream output = new FileOutputStream(targetFile);
				remover.optimize(input, output);
				close(input, output);
			} else if (isZip(srcFile.getName())) {
				final ZipInputStream input = new ZipInputStream(
						createInputStream(srcFile, srcIsTarget));
				final ZipOutputStream output = new ZipOutputStream(
						new FileOutputStream(targetFile));
				new ZipOptimizer(remover) {
					@Override
					protected void processClass(final String name,
							final ZipInputStream zis, final ZipOutputStream zos)
							throws IOException {
						log("Processing class " + name); //$NON-NLS-1$
						super.processClass(name, zis, zos);

					}
				}.optimize(input, output);
				close(input, output);
			}
		}
	}

	private void close(final Closeable... closeables) {
		for (final Closeable closeable : closeables) {
			close(closeable);
		}
	}

	private void close(final Closeable closeable) {
		try {
			closeable.close();
		} catch (final IOException e) {
			log("IOException while closing: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	private AnnotationReferenceRemover createConfigured() {
		final AnnotationReferenceRemover remover = new AnnotationReferenceRemover();
		configure(remover);
		return remover;
	}

	private void configure(final AnnotationReferenceRemover remover) {
		for (final AnnotationRemoveSelector annotationRemoveSelector : this.selectors) {
			final Target target = annotationRemoveSelector.getFrom();
			if (target == null) {
				for (final ElementType elementType : ElementType.values()) {
					configure(remover, annotationRemoveSelector, elementType);
				}
			} else {
				configure(remover, annotationRemoveSelector,
						getElementType(target));
			}
		}
	}

	private void configure(final AnnotationReferenceRemover remover,
			final AnnotationRemoveSelector annotationRemoveSelector,
			final ElementType elementType) {
		remover.removeFrom(elementType, annotationRemoveSelector.getMatcher());
	}

	@Override
	public void execute() throws BuildException {
		if (this.src == null) {
			throw new BuildException("src path not set"); //$NON-NLS-1$
		}
		if (this.selectors.isEmpty()) {
			throw new BuildException("no annotations set to be removed"); //$NON-NLS-1$
		}

		final AnnotationReferenceRemover remover = createConfigured();
		final Project prj = getProject();
		for (final String file : this.src.list()) {
			final File pathElement = prj.resolveFile(file);
			if (!pathElement.exists()) {
				throw new BuildException(pathElement.getPath()
						+ " does not exist!", getLocation()); //$NON-NLS-1$
			}
			try {
				doWork(pathElement.getAbsoluteFile(), remover);
			} catch (final IOException e) {
				throw new BuildException(e);
			}
		}
	}

	/**
	 * Adds a path that should be processed as input.
	 * 
	 * @param path the Path to process
	 */
	public void addConfiguredSrc(final Path path) {
		this.src = extendClassPath(this.src, path);
	}

	private Path extendClassPath(final Path arg, final Path add) {
		final Path classPath = arg == null ? new Path(getProject()) : arg;
		if (add != null) {
			classPath.add(add);
		}
		return classPath;
	}
	
	public void setTargetDir(final File targetDir) {
		this.targetDir = targetDir;
	}

}
