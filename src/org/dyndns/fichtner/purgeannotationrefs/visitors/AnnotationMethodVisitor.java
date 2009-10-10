package org.dyndns.fichtner.purgeannotationrefs.visitors;


import org.dyndns.fichtner.purgeannotationrefs.Util;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * Class for removing annotations from methods.
 * 
 * @author Peter Fichtner
 */
public class AnnotationMethodVisitor extends DefaultAnnotationMethodVisitor {

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public AnnotationMethodVisitor(ClassVisitor classVisitor) {
		super(classVisitor);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		return Util.isMethod(name) ? super.visitMethod(access, name, desc,
				signature, exceptions) : new MethodAdapter(this.cv.visitMethod(
				access, name, desc, signature, exceptions));
	}

}
