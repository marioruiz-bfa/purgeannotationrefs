package org.dyndns.fichtner.purgeannotationrefs.mojo;

import static java.util.Collections.unmodifiableMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher;

/**
 * Removes annotation references from classfiles.
 */
@Mojo(name = PurgeAnnoRefsMojo.PAR, defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class PurgeAnnoRefsMojo extends AbstractMojo {

	private static final Map<String, ElementType> mapping = unmodifiableMap(mapping());

	protected static final String PAR = "par";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(property = PAR + ".removes", required = true)
	private Remove[] removes;

	public void execute() throws MojoExecutionException, MojoFailureException {
		AnnotationReferenceRemover remover = getConfigured();
		for (File file : collectFiles(new File(project.getBuild()
				.getOutputDirectory()))) {
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
		FileOutputStream stream = new FileOutputStream(file, false);
		try {
			stream.write(content);
		} finally {
			stream.close();
		}
	}

	private AnnotationReferenceRemover getConfigured() {
		AnnotationReferenceRemover remover = new AnnotationReferenceRemover();
		for (Remove remove : removes) {
			Matcher<String> matcher = new RegExpMatcher(
					Pattern.compile(remove.regexp));
			if (remove.removeFroms == null) {
				remover.remove(matcher);
			} else {
				for (ElementType elementType : configToTypes(remove.removeFroms)) {
					remover.removeFrom(elementType, matcher);
				}
			}
		}
		return remover;
	}

	private static Iterable<ElementType> configToTypes(
			Iterable<String> removeFroms) {
		Set<ElementType> elements = new HashSet<ElementType>();
		for (String removeFrom : removeFroms) {
			ElementType elementType = mapping.get(removeFrom);
			if (elementType == null) {
				throw new IllegalStateException(removeFrom
						+ " not a valid element type, supported types are "
						+ mapping.keySet());
			}
			elements.add(elementType);
		}
		return elements;
	}

	private static Map<String, ElementType> mapping() {
		Map<String, ElementType> mapping = new HashMap<String, ElementType>();
		mapping.put("types", ElementType.TYPE);
		mapping.put("fields", ElementType.FIELD);
		mapping.put("constructors", ElementType.CONSTRUCTOR);
		mapping.put("methods", ElementType.METHOD);
		mapping.put("parameters", ElementType.PARAMETER);
		return mapping;
	}

	private static List<File> collectFiles(File root) {
		return collectTo(root, new ArrayList<File>(200));
	}

	private static List<File> collectTo(File baseDir, List<File> files) {
		for (File file : baseDir.listFiles()) {
			if (isClass(file)) {
				files.add(file);
			} else if (isDirectory(file)) {
				collectTo(file, files);
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
