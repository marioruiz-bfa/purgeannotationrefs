package org.dyndns.fichtner.purgeannotationrefs;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import org.dyndns.fichtner.purgeannotationrefs.optimizer.ClassOptimizer;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationClassVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationConstructorVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationFieldVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationMethodVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationParameterVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.FilteringVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Class for removing annotation references from classes (and their methods,
 * field, statements, ...)
 * 
 * @author Peter Fichtner
 */
public class AnnotationReferenceRemover implements ClassOptimizer {

	private final static List<ElementType> supportedTypes = Collections
			.unmodifiableList(Arrays.asList(TYPE, FIELD, CONSTRUCTOR, METHOD,
					PARAMETER));

	private static class Config {

		private final EnumMap<ElementType, List<Matcher<String>>> map = new EnumMap<ElementType, List<Matcher<String>>>(
				ElementType.class);

		public void addFiltered(final ElementType key,
				final Matcher<String> matcher) {
			List<Matcher<String>> data = this.map.get(key);
			if (data == null) {
				this.map.put(key, data = new ArrayList<Matcher<String>>());
			}
			data.add(matcher);
		}

		public List<Matcher<String>> getFiltered(final ElementType key) {
			List<Matcher<String>> matchers = this.map.get(key);
			return matchers == null ? Collections.<Matcher<String>> emptyList()
					: matchers;
		}

	}

	private final Config config = new Config();

	/**
	 * Int/Enum-wrapper for the Asm RewriteMode.
	 * 
	 * @author Peter Fichtner
	 */
	public static enum RewriteMode {
		/**
		 * @see ClassReader.EXPAND_FRAMES
		 */
		EXPAND_FRAMES(ClassReader.EXPAND_FRAMES), /**
		 * @see ClassReader.SKIP_CODE
		 */
		SKIP_CODE(ClassReader.SKIP_CODE), /**
		 * @see ClassReader.SKIP_DEBUG
		 */
		SKIP_DEBUG(ClassReader.SKIP_DEBUG), /**
		 * @see ClassReader.SKIP_FRAMES
		 */
		SKIP_FRAMES(ClassReader.SKIP_FRAMES);

		private final int value;

		private RewriteMode(final int value) {
			this.value = value;
		}

		/**
		 * Return Asm's int value.
		 * 
		 * @return Asm's int value
		 */
		public int getValue() {
			return this.value;
		}

	}

	private Set<RewriteMode> rewriteMode = Collections.emptySet();

	/**
	 * Creates a new instance for the passed class (.class-file/bytecode). This
	 * class must be readable.
	 * 
	 * @param classfile path to the classfile
	 */
	public AnnotationReferenceRemover() {
		super();
	}

	/**
	 * Remove the passed annotation from <b>all</b> elements (class/methods/...)
	 * (if present).
	 * 
	 * @param matcher the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover remove(final Matcher<String> matcher) {
		for (ElementType elementType : supportedTypes) {
			removeFrom(elementType, matcher);
		}
		return this;
	}

	/**
	 * Remove the passed annotation from the passed elements (if present).
	 * 
	 * @param removeFrom remove from which elements
	 * @param matcher the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFrom(final ElementType removeFrom,
			final Matcher<String> matcher) {
		if (!supportedTypes.contains(removeFrom)) {
			throw new IllegalArgumentException(removeFrom
					+ " not supported, supported types are " + supportedTypes);
		}
		this.config.addFiltered(removeFrom, matcher);
		return this;
	}

	/**
	 * Set the rewrite mode.
	 * 
	 * @param rewriteMode the rewrite mode to set
	 */
	public void setRewriteMode(Set<RewriteMode> rewriteMode) {
		this.rewriteMode = rewriteMode;
	}

	private static int toInt(Iterable<RewriteMode> rewriteModes) {
		int value = 0;
		for (RewriteMode rewriteMode : rewriteModes) {
			value |= rewriteMode.value;
		}
		return value;
	}

	/**
	 * Writes the class back (replaces the existing class).
	 * 
	 * @param inputStream the stream to read from
	 * @param outputStream the stream to write to
	 * 
	 * @param classfile the classfile to read and write
	 * 
	 * @throws IOException on write errors
	 */
	public void optimize(final InputStream inputStream,
			final OutputStream outputStream) throws IOException {
		final ClassWriter classWriter = new ClassWriter(
				ClassWriter.COMPUTE_MAXS);
		new ClassReader(inputStream).accept(createVisitors(classWriter),
				toInt(this.rewriteMode));
		outputStream.write(classWriter.toByteArray());
	}

	private AnnotationParameterVisitor createVisitors(
			final ClassWriter classWriter) {
		return createParameterVisitor(createFieldVisitor(createMethodVisitor(createConstructorVisitor(createClassVisitor(classWriter)))));
	}

	private AnnotationParameterVisitor createParameterVisitor(
			final AnnotationFieldVisitor fieldVisitor) {
		return configure(new AnnotationParameterVisitor(fieldVisitor),
				PARAMETER);
	}

	private AnnotationFieldVisitor createFieldVisitor(
			final AnnotationMethodVisitor methodVisitor) {
		return configure(new AnnotationFieldVisitor(methodVisitor), FIELD);
	}

	private AnnotationMethodVisitor createMethodVisitor(
			final AnnotationConstructorVisitor constructorVisitor) {
		return configure(new AnnotationMethodVisitor(constructorVisitor),
				METHOD);
	}

	private AnnotationConstructorVisitor createConstructorVisitor(
			final AnnotationClassVisitor classVisitor) {
		return configure(new AnnotationConstructorVisitor(classVisitor),
				CONSTRUCTOR);
	}

	private AnnotationClassVisitor createClassVisitor(
			final ClassWriter classWriter) {
		return configure(new AnnotationClassVisitor(classWriter), TYPE);
	}

	private <T extends FilteringVisitor> T configure(final T filteringVisitor,
			final ElementType removeFrom) {
		for (final Matcher<String> matcher : this.config
				.getFiltered(removeFrom)) {
			filteringVisitor.addFiltered(matcher);
		}
		return filteringVisitor;
	}

}
