package org.dyndns.fichtner.purgeannotationrefs.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.dyndns.fichtner.purgeannotationrefs.Util.isMethod;

/**
 * Adapter that delegates methods (no constructors) to the instance ClasVisitor
 * passed in the constructor.
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
  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions) {
    return isMethod(name) ? super.visitMethod(access, name, desc,
        signature, exceptions) : this.cv.visitMethod(access, name,
        desc, signature, exceptions);
  }

}
