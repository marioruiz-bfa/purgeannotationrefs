package org.dyndns.fichtner.purgeannotationrefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

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

	private static class Config {

		private final EnumMap<ElementType, List<Matcher>> map = new EnumMap<ElementType, List<Matcher>>(
				ElementType.class);

		public void addFiltered(final ElementType key, final Matcher matcher) {
			List<Matcher> data = this.map.get(key);
			if (data == null) {
				this.map.put(key, data = new ArrayList<Matcher>());
			}
			data.add(matcher);
		}

		public List<Matcher> getFiltered(final ElementType key) {
			return this.map.get(key);
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
		 * Rewrite all attributes
		 */
		ALL(0),
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

	private RewriteMode rewriteMode = RewriteMode.ALL;

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
	public AnnotationReferenceRemover remove(final Matcher matcher) {
		removeFrom(ElementType.TYPE, matcher);
		removeFrom(ElementType.FIELD, matcher);
		removeFrom(ElementType.CONSTRUCTOR, matcher);
		removeFrom(ElementType.METHOD, matcher);
		removeFrom(ElementType.PARAMETER, matcher);
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
			final Matcher matcher) {
		this.config.addFiltered(removeFrom, matcher);
		return this;
	}

	/**
	 * Set the rewrite mode.
	 * 
	 * @param rewriteMode the rewrite mode to set
	 */
	public void setRewriteMode(final RewriteMode rewriteMode) {
		this.rewriteMode = rewriteMode;
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
				this.rewriteMode.getValue());
		outputStream.write(classWriter.toByteArray());
	}

	private AnnotationParameterVisitor createVisitors(
			final ClassWriter classWriter) {
		return createParameterVisitor(createFieldVisitor(createMethodVisitor(createConstructorVisitor(createClassVisitor(classWriter)))));
	}

	private AnnotationParameterVisitor createParameterVisitor(
			final AnnotationFieldVisitor fieldVisitor) {
		return configure(new AnnotationParameterVisitor(fieldVisitor),
				ElementType.PARAMETER);
	}

	private AnnotationFieldVisitor createFieldVisitor(
			final AnnotationMethodVisitor methodVisitor) {
		return configure(new AnnotationFieldVisitor(methodVisitor),
				ElementType.FIELD);
	}

	private AnnotationMethodVisitor createMethodVisitor(
			final AnnotationConstructorVisitor constructorVisitor) {
		return configure(new AnnotationMethodVisitor(constructorVisitor),
				ElementType.METHOD);
	}

	private AnnotationConstructorVisitor createConstructorVisitor(
			final AnnotationClassVisitor classVisitor) {
		return configure(new AnnotationConstructorVisitor(classVisitor),
				ElementType.CONSTRUCTOR);
	}

	private AnnotationClassVisitor createClassVisitor(
			final ClassWriter classWriter) {
		return configure(new AnnotationClassVisitor(classWriter),
				ElementType.TYPE);
	}

	private <T extends FilteringVisitor> T configure(final T filteringVisitor,
			final ElementType removeFrom) {
		for (final Matcher matcher : this.config.getFiltered(removeFrom)) {
			filteringVisitor.addFiltered(matcher);
		}
		return filteringVisitor;
	}

}
