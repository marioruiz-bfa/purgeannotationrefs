package org.dyndns.fichtner.purgeannotationrefs.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.RecordComponentVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.dyndns.fichtner.purgeannotationrefs.Util.*;
import static org.objectweb.asm.Opcodes.ASM9;

public class AnnotationRecordComponentVisitor extends ClassVisitor implements
    Filterable {

  private final List<Predicate<String>> filtered = new ArrayList<>();

  /**
   * Creates a new instance delegating all calls to the passed visitor.
   *
   * @param classVisitor delegate visitor
   */
  public AnnotationRecordComponentVisitor(ClassVisitor classVisitor) {
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
  public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
    return new RecordComponentVisitor(ASM9, this.cv.visitRecordComponent(name, descriptor, signature)) {
      @Override
      public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return matches(descriptor) ? annotationRemover() : super.visitAnnotation(descriptor, visible);
      }
    };
  }

  private boolean matches(String desc) {
    return atLeastOneMatches(this.filtered, typeToClassname(desc));
  }
}
