package org.dyndns.fichtner.purgeannotationrefs.testcode;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.dyndns.fichtner.purgeannotationrefs.testcode.cuts.ExampleRecord;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.*;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.TestUtils.count;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.TestHelper.removeAnno;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.classToJasmin;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.streamToJasmin;
import static org.junit.jupiter.api.Assertions.*;


public class TestExampleRecord {

  private final static Class<MyAnno> defaultAnnoKlass = MyAnno.class;

  private static Matcher<String> isAnno(Class<? extends Annotation> klass) {
    return new Matcher.RegExpMatcher(Pattern.compile("\\.annotation (in)?visible " + Type.getType(klass).getDescriptor()));
  }

  @Test
  public void checkOriginalClassHas5Annotations() throws IOException {

    String[] dump = classToJasmin(ExampleRecord.class).split("(\\r\\n|\\r|\\n)");
    assertAnnoCountIs(5, dump);
    assertAll(
        () -> assertEquals(1, count(dump, isAnno(defaultAnnoKlass), TYPES, MyAnno.class), "Mismatch on ElementType: " + TYPES),
        () -> assertEquals(1, count(dump, isAnno(defaultAnnoKlass), METHODS, MyAnno.class), "Mismatch on ElementType: " + METHODS),
        () -> assertEquals(1, count(dump, isAnno(defaultAnnoKlass), FIELDS, MyAnno.class), "Mismatch on ElementType: " + FIELDS),
        () -> assertEquals(1, count(dump, isAnno(defaultAnnoKlass), CONSTRUCTORS, MyAnno.class), "Mismatch on ElementType: " + CONSTRUCTORS),
        () -> assertEquals(1, count(dump, isAnno(MyRecordAnno.class), RECORD_COMPONENTS, MyRecordAnno.class), "Mismatch on ElementType: " + RECORD_COMPONENTS),
        () -> assertEquals(1, count(dump, isAnno(defaultAnnoKlass), PARAMETERS, MyAnno.class), "Mismatch on ElementType: " + PARAMETERS)
    );
    // assertThat(count(dump, isAnno(annoklass), LOCAL_VARIABLES), is(0));
  }

  @Test
  public void whenRemovingAllRefsTheAnnotationIsNotFoundAtAll() throws IOException {

    try (ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(ExampleRecord.class, new AnnotationReferenceRemover().remove(new StringMatcher(defaultAnnoKlass.getName()))))) {
      assertAnnoCountIs(0, streamToJasmin(is).split("(\\r\\n|\\r|\\n)"));
    }
  }

  @Test
  public void removeFromTypeOnly() {
    assertDoesNotThrow(() -> removeOnlyType(TYPES));
  }

  @Test
  public void removeFromMethodOnly() {
    assertDoesNotThrow(() -> removeOnlyType(METHODS));
  }

  @Test
  public void removeFromConstructorOnly() {
    assertDoesNotThrow(() -> removeOnlyType(CONSTRUCTORS));
  }

  @Test
  public void removeFromFieldOnly() {
    assertDoesNotThrow(() -> removeOnlyType(FIELDS));
  }

  @Test
  public void removeFromRecordComponentOnly() {
    //assertDoesNotThrow(() -> removeOnlyType(RECORD_COMPONENTS));
    assertDoesNotThrow(() -> removeOnlyRecordComponent(RECORD_COMPONENTS, MyRecordAnno.class));

  }

  private void removeOnlyType(RemoveFrom removeFrom) throws IOException {
    removeOnlyType(removeFrom, defaultAnnoKlass);
  }

  private void removeOnlyType(RemoveFrom removeFrom, Class<? extends Annotation> annoKlass) throws IOException {
    try (ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(ExampleRecord.class, new AnnotationReferenceRemover().removeFrom(removeFrom, new StringMatcher(annoKlass.getName()))))) {
      String[] dump = streamToJasmin(is).split("(\\r\\n|\\r|\\n)");
      assertAll(
          () -> assertCount(removeFrom, dump, TYPES, annoKlass),
          () -> assertCount(removeFrom, dump, FIELDS, annoKlass),
          () -> assertCount(removeFrom, dump, CONSTRUCTORS, annoKlass),
          () -> assertCount(removeFrom, dump, METHODS, annoKlass),
          () -> assertCount(removeFrom, dump, PARAMETERS, annoKlass)
      );
      // Local variable annotations are not retained in class files (JLS
      // 9.6.1.2)
      // assertThat(count(dump, isAnno(annoklass), LOCAL_VARIABLE),
      // is(0));

    }
  }

  private void removeOnlyRecordComponent(RemoveFrom removeFrom, Class<? extends Annotation> annoKlass) throws IOException {
    try (ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(ExampleRecord.class, new AnnotationReferenceRemover().removeFrom(removeFrom, new StringMatcher(annoKlass.getName()))))) {
      String[] dump = streamToJasmin(is).split("(\\r\\n|\\r|\\n)");
      assertAll(
          () -> assertCount(removeFrom, dump, TYPES, defaultAnnoKlass),
          () -> assertCount(removeFrom, dump, FIELDS, defaultAnnoKlass),
          () -> assertCount(removeFrom, dump, CONSTRUCTORS, defaultAnnoKlass),
          () -> assertCount(removeFrom, dump, METHODS, defaultAnnoKlass),
          () -> assertCount(removeFrom, dump, RECORD_COMPONENTS, annoKlass),
          () -> assertCount(removeFrom, dump, PARAMETERS, defaultAnnoKlass)
      );
      // Local variable annotations are not retained in class files (JLS
      // 9.6.1.2)
      // assertThat(count(dump, isAnno(annoklass), LOCAL_VARIABLE),
      // is(0));

    }
  }

  private void assertCount(RemoveFrom removeFrom, String[] dump, RemoveFrom elementToCount, Class<? extends Annotation> annoKlass) {
    assertEquals(removeFrom == elementToCount ? 0 : 1, count(dump, isAnno(annoKlass), elementToCount, annoKlass), "Count Mismatch for Type: " + removeFrom + " vs " + elementToCount);
  }

  private void assertAnnoCountIs(int count, String[] s) {
    assertEquals(count, count(s, isAnno(defaultAnnoKlass)));
  }

}
