package org.dyndns.fichtner.purgeannotationrefs.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.dyndns.fichtner.purgeannotationrefs.Util.*;
import static org.objectweb.asm.Opcodes.ASM9;

/**
 * Class for removing annotations from method and constructor parameters.
 *
 * @author Peter Fichtner
 */
public class AnnotationParameterVisitor extends ClassVisitor implements
    Filterable {

  private final List<Predicate<String>> filtered = new ArrayList<>();

  /**
   * Creates a new instance delegating all calls to the passed visitor.
   *
   * @param classVisitor delegate visitor
   */
  public AnnotationParameterVisitor(ClassVisitor classVisitor) {
    super(ASM9, classVisitor);
  }

  /**
   * Add an annotation that should be filtered.
   *
   * @param matcher the annotation to filter
   */
  @Override
  public void addFiltered(Predicate<String> matcher) {
    this.filtered.add(matcher);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
                                   String signature, String[] exceptions) {
    return new MethodVisitor(ASM9, this.cv.visitMethod(access, name, desc,
        signature, exceptions)) {
      @Override
      public AnnotationVisitor visitParameterAnnotation(int parameter,
                                                        String desc, boolean visible) {
        return matches(desc) ? annotationRemover() : super
            .visitParameterAnnotation(parameter, desc, visible);
      }

    };
  }

  private boolean matches(String desc) {
    return atLeastOneMatches(this.filtered, typeToClassname(desc));
  }

}
