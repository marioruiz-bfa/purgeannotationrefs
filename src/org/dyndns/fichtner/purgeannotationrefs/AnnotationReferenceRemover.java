package org.dyndns.fichtner.purgeannotationrefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
		private final EnumMap<RemoveFrom, List<String>> map = new EnumMap<RemoveFrom, List<String>>(
				RemoveFrom.class);

		public void addFiltered(final RemoveFrom key, final String anno) {
			List<String> data = this.map.get(key);
			if (data == null) {
				data = new ArrayList<String>();
				this.map.put(key, data);
			}
			data.add(anno);
		}

		public List<String> getFiltered(final RemoveFrom key) {
			return this.map.get(key);
		}

	}

	private enum RemoveFrom {
		CLASS_HEADER, CONSTRUCTORS, METHODS, FIELDS, PARAMETERS;
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

	private RewriteMode rewriteMode = RewriteMode.SKIP_DEBUG;

	/**
	 * Creates a new instance for the passed class (.class-file/bytecode). This
	 * class must be readable.
	 * 
	 * @param classfile path to the classfile
	 */
	public AnnotationReferenceRemover() {
	}

	/**
	 * Remove the passed annotation from <b>all</b> elements (class/methods/...)
	 * (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover remove(final String anno) {
		removeFromClass(anno);
		removeFromFields(anno);
		removeFromConstructors(anno);
		removeFromMethods(anno);
		removeFromParameters(anno);
		// TODO statements, ...?
		return this;
	}

	/**
	 * Remove the passed annotation from the class only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromClass(final String anno) {
		this.config.addFiltered(RemoveFrom.CLASS_HEADER, anno);
		return this;
	}

	/**
	 * Remove the passed annotation from the fields only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromFields(final String anno) {
		this.config.addFiltered(RemoveFrom.FIELDS, anno);
		return this;
	}

	/**
	 * Remove the passed annotation from the constructors only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromConstructors(final String anno) {
		this.config.addFiltered(RemoveFrom.CONSTRUCTORS, anno);
		return this;
	}

	/**
	 * Remove the passed annotation from the methods only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromMethods(final String anno) {
		this.config.addFiltered(RemoveFrom.METHODS, anno);
		return this;
	}

	/**
	 * Remove the passed annotation from the method and constructor parameters
	 * only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromParameters(final String anno) {
		this.config.addFiltered(RemoveFrom.PARAMETERS, anno);
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
				RemoveFrom.PARAMETERS);
	}

	private AnnotationFieldVisitor createFieldVisitor(
			final AnnotationMethodVisitor methodVisitor) {
		return configure(new AnnotationFieldVisitor(methodVisitor),
				RemoveFrom.FIELDS);
	}

	private AnnotationMethodVisitor createMethodVisitor(
			final AnnotationConstructorVisitor constructorVisitor) {
		return configure(new AnnotationMethodVisitor(constructorVisitor),
				RemoveFrom.METHODS);
	}

	private AnnotationConstructorVisitor createConstructorVisitor(
			final AnnotationClassVisitor classVisitor) {
		return configure(new AnnotationConstructorVisitor(classVisitor),
				RemoveFrom.CONSTRUCTORS);
	}

	private AnnotationClassVisitor createClassVisitor(
			final ClassWriter classWriter) {
		return configure(new AnnotationClassVisitor(classWriter),
				RemoveFrom.CLASS_HEADER);
	}

	private <T extends FilteringVisitor> T configure(final T filteringVisitor,
			final RemoveFrom removeFrom) {
		for (final String anno : this.config.getFiltered(removeFrom)) {
			filteringVisitor.addFiltered(anno);
		}
		return filteringVisitor;
	}

}
