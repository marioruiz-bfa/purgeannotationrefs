package org.dyndns.fichtner.purgeannotationrefs;

import org.dyndns.fichtner.purgeannotationrefs.optimizer.ClassOptimizer;
import org.dyndns.fichtner.purgeannotationrefs.visitors.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Predicate;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.*;

/**
 * Class for removing annotation references from classes (and their methods,
 * field, statements, ...)
 *
 * @author Peter Fichtner
 */
public class AnnotationReferenceRemover implements ClassOptimizer {

  private final Config config = new Config();
  private Set<RewriteMode> rewriteMode = Collections.emptySet();

  /**
   * Creates a new instance for the passed class (.class-file/bytecode). This
   * class must be readable.
   */
  public AnnotationReferenceRemover() {
    super();
  }

  private static int toInt(Iterable<RewriteMode> rewriteModes) {
    int value = 0;
    for (RewriteMode rewriteMode : rewriteModes) {
      value |= rewriteMode.value;
    }
    return value;
  }

  /**
   * Remove the passed annotation from <b>all</b> elements (class/methods/...)
   * (if present).
   *
   * @param matcher the annotation that should be removed
   * @return this instance
   */
  public AnnotationReferenceRemover remove(Predicate<String> matcher) {
    for (RemoveFrom removeFrom : RemoveFrom.values()) {
      removeFrom(removeFrom, matcher);
    }
    return this;
  }

  /**
   * Remove the passed annotation from the passed elements (if present).
   *
   * @param removeFrom remove from which elements
   * @param matcher    the annotation that should be removed
   * @return this instance
   */
  public AnnotationReferenceRemover removeFrom(RemoveFrom removeFrom, Predicate<String> matcher) {
    this.config.addFiltered(removeFrom, matcher);
    return this;
  }

  /**
   * Set the rewrite mode.
   *
   * @param rewriteMode the rewrite mode to set
   */
  public void setRewriteMode(Set<RewriteMode> rewriteMode) {
    this.rewriteMode = rewriteMode;
  }

  /**
   * Writes the class back (replaces the existing class).
   *
   * @param inputStream  the stream to read from
   * @param outputStream the stream to write to
   * @throws IOException on write errors
   */
  public void optimize(InputStream inputStream, OutputStream outputStream) throws IOException {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    new ClassReader(inputStream).accept(createVisitors(classWriter), toInt(this.rewriteMode));
    outputStream.write(classWriter.toByteArray());
  }

  private AnnotationParameterVisitor createVisitors(ClassWriter classWriter) {
    return createParameterVisitor(createFieldVisitor(createRecordComponentVisitor(createMethodVisitor(createConstructorVisitor(createClassVisitor(classWriter))))));
  }

  private AnnotationParameterVisitor createParameterVisitor(ClassVisitor classVisitor) {
    return configure(new AnnotationParameterVisitor(classVisitor), PARAMETERS);
  }

  private AnnotationFieldVisitor createFieldVisitor(ClassVisitor classVisitor) {
    return configure(new AnnotationFieldVisitor(classVisitor), FIELDS);
  }

  private AnnotationMethodVisitor createMethodVisitor(ClassVisitor classVisitor) {
    return configure(new AnnotationMethodVisitor(classVisitor), METHODS);
  }

  private AnnotationConstructorVisitor createConstructorVisitor(ClassVisitor classVisitor) {
    return configure(new AnnotationConstructorVisitor(classVisitor), CONSTRUCTORS);
  }

  private AnnotationClassVisitor createClassVisitor(ClassVisitor classVisitor) {
    return configure(new AnnotationClassVisitor(classVisitor), TYPES);
  }

  private AnnotationRecordComponentVisitor createRecordComponentVisitor(ClassVisitor classVisitor) {
    return configure(new AnnotationRecordComponentVisitor(classVisitor), RECORD_COMPONENTS);
  }

  private <T extends Filterable> T configure(T filteringVisitor, RemoveFrom removeFrom) {
    for (Predicate<String> matcher : this.config.getFiltered(removeFrom)) {
      filteringVisitor.addFiltered(matcher);
    }
    return filteringVisitor;
  }

  /**
   * Int/Enum-wrapper for the Asm RewriteMode.
   *
   * @author Peter Fichtner
   */
  public enum RewriteMode {
    /**
     * @see ClassReader#EXPAND_FRAMES
     */
    EXPAND_FRAMES(ClassReader.EXPAND_FRAMES),
    /**
     * @see ClassReader#SKIP_CODE
     */
    SKIP_CODE(ClassReader.SKIP_CODE),
    /**
     * @see ClassReader#SKIP_DEBUG
     */
    SKIP_DEBUG(ClassReader.SKIP_DEBUG),
    /**
     * @see ClassReader#SKIP_FRAMES
     */
    SKIP_FRAMES(ClassReader.SKIP_FRAMES);

    private final int value;

    RewriteMode(int value) {
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

  private static class Config {

    private final EnumMap<RemoveFrom, List<Predicate<String>>> map = new EnumMap<>(RemoveFrom.class);

    public void addFiltered(RemoveFrom removeFrom, Predicate<String> matcher) {
      if (removeFrom != ALL) {
        List<Predicate<String>> data = this.map.computeIfAbsent(removeFrom, k -> new ArrayList<>());
        data.add(matcher);
      }
    }

    public List<Predicate<String>> getFiltered(RemoveFrom removeFrom) {
      return this.map.getOrDefault(removeFrom, Collections.emptyList());
    }

  }

}
