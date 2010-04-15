package org.dyndns.fichtner.purgeannotationrefs.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

/**
 * 
 * Generic FieldVisitor adapter.
 * 
 * @author Peter Fichtner
 */
public class FieldAdapter implements FieldVisitor {

	private final FieldVisitor fv;

	/**
	 * Creates a new instance delegating the calls to the passed FieldVisitor.
	 * 
	 * @param fv delegate instance
	 */
	public FieldAdapter(final FieldVisitor fv) {
		this.fv = fv;
	}

	public AnnotationVisitor visitAnnotation(final String string,
			final boolean b) {
		return this.fv.visitAnnotation(string, b);
	}

	public void visitAttribute(final Attribute attribute) {
		this.fv.visitAttribute(attribute);
	}

	public void visitEnd() {
		this.fv.visitEnd();
	}

}
