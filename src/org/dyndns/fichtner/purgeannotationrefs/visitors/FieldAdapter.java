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
	public FieldAdapter(FieldVisitor fv) {
		this.fv = fv;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return this.fv.visitAnnotation(arg0, arg1);
	}

	@Override
	public void visitAttribute(Attribute arg0) {
		this.fv.visitAttribute(arg0);
	}

	@Override
	public void visitEnd() {
		this.fv.visitEnd();
	}

}
