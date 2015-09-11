package org.dyndns.fichtner.purgeannotationrefs.visitors;

import static org.dyndns.fichtner.purgeannotationrefs.Util.isConstructor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Adapter that delegates constructors (no methods ) to the instance ClasVisitor
 * passed in the constructor.
 * 
 * @author Peter Fichtner
 */
public class AnnotationConstructorVisitor extends
		DefaultAnnotationMethodVisitor {

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public AnnotationConstructorVisitor(ClassVisitor classVisitor) {
		super(classVisitor);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		return isConstructor(name) ? super.visitMethod(access, name, desc,
				signature, exceptions) : this.cv.visitMethod(access, name,
				desc, signature, exceptions);
	}

}
