package org.dyndns.fichtner.purgeannotationrefs.mojo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher;

/**
 * Removes annotation references from classfiles.
 */
@Mojo(name = "process-classes")
public class PurgeAnnoRefsMojo extends AbstractMojo {

	public static class RemoveFrom {
		private boolean types;
		private boolean constructors;
		private boolean fields;
		private boolean methods;
		private boolean parameters;
	}

	@Parameter(property = "project", required = true, readonly = true)
	private MavenProject project;

	@Parameter(property = "regexp", required = true)
	private String toRemove;

	@Parameter(property = "removeFrom")
	private RemoveFrom removeFrom;

	@Parameter(property = "dir", defaultValue = "${project.build.outputDirectory}")
	private String dir;

	public void execute() throws MojoExecutionException, MojoFailureException {
		AnnotationReferenceRemover remover = getConfigured();
		for (File file : collectFiles(new File(this.dir))) {
			enhanceClass(remover, file);
		}
	}

	private void enhanceClass(AnnotationReferenceRemover remover, File file)
			throws MojoFailureException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			try {
				remover.optimize(new FileInputStream(file), outputStream);
			} finally {
				outputStream.close();
			}
			replace(file, outputStream.toByteArray());
		} catch (FileNotFoundException e) {
			throw new MojoFailureException("Failed to load " + file);
		} catch (IOException e) {
			throw new MojoFailureException("IOException " + e.getMessage());
		}
	}

	private void replace(File file, byte[] content) throws IOException {
		FileOutputStream stream = new FileOutputStream(file);
		try {
			stream.write(content);
		} finally {
			stream.close();
		}
	}

	private AnnotationReferenceRemover getConfigured() {
		AnnotationReferenceRemover remover = new AnnotationReferenceRemover();
		Matcher<String> matcher = new RegExpMatcher(
				Pattern.compile(this.toRemove));
		if (this.removeFrom == null) {
			remover.remove(matcher);
		} else {
			for (ElementType elementType : configToTypes(this.removeFrom)) {
				remover.removeFrom(elementType, matcher);
			}
		}
		return remover;
	}

	private static Collection<ElementType> configToTypes(RemoveFrom removeFrom) {
		List<ElementType> elements = new ArrayList<ElementType>();
		if (removeFrom.types) {
			elements.add(ElementType.TYPE);
		}
		if (removeFrom.fields) {
			elements.add(ElementType.FIELD);
		}
		if (removeFrom.constructors) {
			elements.add(ElementType.CONSTRUCTOR);
		}
		if (removeFrom.methods) {
			elements.add(ElementType.METHOD);
		}
		if (removeFrom.parameters) {
			elements.add(ElementType.PARAMETER);
		}
		return elements;
	}

	private List<File> collectFiles(File root) {
		List<File> files = new ArrayList<File>();
		for (File file : root.listFiles()) {
			if (isClass(file)) {
				files.add(file);
			} else if (isDirectory(file)) {
				files.addAll(collectFiles(file));
			}
		}
		return files;
	}

	private static boolean isClass(File file) {
		return file.isFile() && file.getName().endsWith(".class");
	}

	private static boolean isDirectory(File file) {
		return file.isDirectory();
	}

}
