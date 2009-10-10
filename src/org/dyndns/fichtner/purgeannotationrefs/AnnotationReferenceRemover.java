package org.dyndns.fichtner.purgeannotationrefs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationClassVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationConstructorVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationFieldVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationMethodVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationParameterVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Class for removing annotation references from classes (and their methods,
 * field, statements, ...)
 * 
 * @author Peter Fichtner
 */
public class AnnotationReferenceRemover {

	private final String classfile;

	private final ClassWriter classWriter;

	private final AnnotationClassVisitor classVisitor;

	private final AnnotationConstructorVisitor constructorVisitor;

	private final AnnotationMethodVisitor methodVisitor;

	private final AnnotationFieldVisitor fieldVisitor;

	private final AnnotationParameterVisitor parameterVisitor;

	/** flag if the class was written back already */
	private boolean classWrittenBack;

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

		private RewriteMode(int value) {
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
	 * @throws IOException if the class could not be read
	 */
	public AnnotationReferenceRemover(final String classfile)
			throws IOException {
		this.classfile = classfile;
		this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		this.classVisitor = new AnnotationClassVisitor(this.classWriter);
		this.constructorVisitor = new AnnotationConstructorVisitor(
				this.classVisitor);
		this.methodVisitor = new AnnotationMethodVisitor(
				this.constructorVisitor);
		this.fieldVisitor = new AnnotationFieldVisitor(this.methodVisitor);
		this.parameterVisitor = new AnnotationParameterVisitor(
				this.fieldVisitor);
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
		this.classVisitor.addFiltered(anno);
		return this;
	}

	/**
	 * Remove the passed annotation from the fields only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromFields(final String anno) {
		this.fieldVisitor.addFiltered(anno);
		return this;
	}

	/**
	 * Remove the passed annotation from the constructors only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromConstructors(final String anno) {
		this.constructorVisitor.addFiltered(anno);
		return this;
	}

	/**
	 * Remove the passed annotation from the methods only (if present).
	 * 
	 * @param anno the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFromMethods(final String anno) {
		this.methodVisitor.addFiltered(anno);
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
		this.parameterVisitor.addFiltered(anno);
		return this;
	}

	/**
	 * Set the rewrite mode.
	 * 
	 * @param rewriteMode the rewrite mode to set
	 */
	public void setRewriteMode(RewriteMode rewriteMode) {
		this.rewriteMode = rewriteMode;
	}

	/**
	 * Writes the class back (replaces the existing class).
	 * 
	 * @throws IOException on write errors
	 */
	public void rewriteClass() throws IOException {
		new ClassReader(new FileInputStream(this.classfile)).accept(
				this.parameterVisitor, this.rewriteMode.getValue());
		write(new FileOutputStream(this.classfile));
	}

	private void write(final OutputStream outputStream) throws IOException {
		if (this.classWrittenBack) {
			throw new IllegalStateException("class already written");
		}
		outputStream.write(this.classWriter.toByteArray());
		this.classWrittenBack = true;
		outputStream.close();
	}

}
