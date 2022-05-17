package org.dyndns.fichtner.purgeannotationrefs.testcode;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.dyndns.fichtner.purgeannotationrefs.testcode.cuts.ExampleRecord;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.*;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.TestUtils.count;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.TestHelper.removeAnno;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.classToJasmin;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.streamToJasmin;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestExampleRecord {

  private final static Class<MyAnno> annoClazz = MyAnno.class;

  private static StringMatcher isAnno(Class<MyAnno> clazz) {
    return new StringMatcher(".annotation" + " " + "visible" + " "
        + Type.getType(clazz).getDescriptor());
  }



  @Test
  public void checkOriginalClassHas5Annotations() throws IOException {

    String[] dump = classToJasmin(ExampleRecord.class).split("(\\r\\n|\\r|\\n)");
    assertAnnoCountIs(5, dump);
    assertEquals(1, count(dump, isAnno(annoClazz), TYPES, MyAnno.class));
    assertEquals(1, count(dump, isAnno(annoClazz), METHODS,MyAnno.class));
    assertEquals(1, count(dump, isAnno(annoClazz), FIELDS, MyAnno.class));
    assertEquals(1, count(dump, isAnno(annoClazz), CONSTRUCTORS, MyAnno.class));
    assertEquals(1, count(dump, isAnno(annoClazz), RECORD_COMPONENTS, MyAnno.class));
    assertEquals(1, count(dump, isAnno(annoClazz), PARAMETERS, MyAnno.class));
    // assertThat(count(dump, isAnno(annoClazz), LOCAL_VARIABLES), is(0));
  }

  @Test
  public void whenRemovingAllRefsTheAnnotationIsNotFoundAtAll()
      throws IOException {

    try (ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
        ExampleRecord.class,
        new AnnotationReferenceRemover().remove(new StringMatcher(
            annoClazz.getName()))))) {
      assertAnnoCountIs(0, streamToJasmin(is).split("(\\r\\n|\\r|\\n)"));
    }
  }

  @Test
  public void removeFromTypeOnly() {
    assertDoesNotThrow(() -> removeOnlyType(TYPES));
  }

  @Test
  public void removeFromMethodOnly(){
    assertDoesNotThrow(() -> removeOnlyType(METHODS));

  }

  @Test
  public void removeFromConstructorOnly(){
    assertDoesNotThrow(() -> removeOnlyType(CONSTRUCTORS));

  }

  @Test
  public void removeFromFieldOnly() {
    assertDoesNotThrow(() -> removeOnlyType(FIELDS));
  }

  @Test
  public void removeFromRecordComponentOnly() {
    assertDoesNotThrow(() -> removeOnlyType(RECORD_COMPONENTS));
  }

  private void removeOnlyType(RemoveFrom removeFrom) throws IOException {
    try (ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
        ExampleRecord.class,
        new AnnotationReferenceRemover().removeFrom(removeFrom,
            new StringMatcher(annoClazz.getName()))))) {
      String[] dump = streamToJasmin(is).split("(\\r\\n|\\r|\\n)");
      assertCount(removeFrom, dump, TYPES);
      assertCount(removeFrom, dump, FIELDS);
      assertCount(removeFrom, dump, RECORD_COMPONENTS);
      assertCount(removeFrom, dump, CONSTRUCTORS);
      assertCount(removeFrom, dump, METHODS);
      assertCount(removeFrom, dump, PARAMETERS);
      // Local variable annotations are not retained in class files (JLS
      // 9.6.1.2)
      // assertThat(count(dump, isAnno(annoClazz), LOCAL_VARIABLE),
      // is(0));

    }
  }

  private void assertCount(RemoveFrom removeFrom, String[] dump,
                           RemoveFrom elementToCount) {
    assertEquals(removeFrom == elementToCount ? 0 : 1, count(dump, isAnno(annoClazz), elementToCount, MyAnno.class));
  }




  private void assertAnnoCountIs(int count, String[] s) {
    assertEquals(count, count(s, isAnno(annoClazz)));
  }

}
