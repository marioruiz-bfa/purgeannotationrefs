package org.dyndns.fichtner.purgeannotationrefs.testcode;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.dyndns.fichtner.purgeannotationrefs.testcode.cuts.ExampleClass;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.*;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.TestHelper.removeAnno;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.classToJasmin;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.streamToJasmin;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestExampleClass {

  private final static Class<MyAnno> annoClazz = MyAnno.class;

  private static StringMatcher isAnno(Class<MyAnno> clazz) {
    return new StringMatcher(".annotation" + " " + "visible" + " "
        + Type.getType(clazz).getDescriptor());
  }

  private static int count(String[] lines, StringMatcher matcher) {
    int cnt = 0;
    for (String line : lines) {
      if (matcher.matches(line)) {
        cnt++;
      }

    }
    return cnt;
  }

  @Test
  public void checkOriginalClassHas5Annotations() throws IOException {
    String[] dump = classToJasmin(ExampleClass.class).split("(\\r\\n|\\r|\\n)");
    assertAnnoCountIs(5, dump);
    assertEquals(1, count(dump, isAnno(annoClazz), TYPES));
    assertEquals(1, count(dump, isAnno(annoClazz), FIELDS));
    assertEquals(1, count(dump, isAnno(annoClazz), CONSTRUCTORS));
    assertEquals(1, count(dump, isAnno(annoClazz), METHODS));
    assertEquals(1, count(dump, isAnno(annoClazz), PARAMETERS));
    // assertThat(count(dump, isAnno(annoClazz), LOCAL_VARIABLES), is(0));
  }

  @Test
  public void whenRemovingAllRefsTheAnnotationIsNotFoundAtAll()
      throws IOException {
    try (ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
        ExampleClass.class,
        new AnnotationReferenceRemover().remove(new StringMatcher(
            annoClazz.getName()))))) {
      assertAnnoCountIs(0, streamToJasmin(is).split("(\\r\\n|\\r|\\n)"));
    }
  }

  @Test
  public void removeFromTypeOnly() throws IOException {
    removeOnlyType(TYPES);
  }

  @Test
  public void removeFromMethodOnly() throws IOException {
    removeOnlyType(METHODS);
  }

  @Test
  public void removeFromConstructorOnly() throws IOException {
    removeOnlyType(CONSTRUCTORS);
  }

  @Test
  public void removeFromFieldOnly() throws IOException {
    removeOnlyType(FIELDS);
  }

  private void removeOnlyType(RemoveFrom removeFrom) throws IOException {
    try (ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
        ExampleClass.class,
        new AnnotationReferenceRemover().removeFrom(removeFrom,
            new StringMatcher(annoClazz.getName()))))) {
      String[] dump = streamToJasmin(is).split("(\\r\\n|\\r|\\n)");
      assertCount(removeFrom, dump, TYPES);
      assertCount(removeFrom, dump, FIELDS);
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
    assertEquals(removeFrom == elementToCount ? 0 : 1, count(dump, isAnno(annoClazz), elementToCount));
  }

  private int count(String[] lines, Matcher<String> matcher,
                    RemoveFrom removeFrom) {
    Type valueType = Type.getType(getType(MyAnno.class, "value"));
    int cnt = 0;

    for (Iterator<String> iterator = Arrays.asList(lines).iterator(); iterator
        .hasNext(); ) {
      String line = iterator.next();
      if (matcher.matches(line)
          && iterator.hasNext()
          && ("value e " + valueType.getDescriptor() + " = \""
          + removeFrom + "\"").equals(iterator.next())) {
        cnt++;
      }

    }
    return cnt;
  }

  private Class<?> getType(Class<? extends Annotation> annoClass, String field) {
    try {
      return annoClass.getDeclaredMethod(field).getReturnType();
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private void assertAnnoCountIs(int count, String[] s) {
    assertEquals(count, count(s, isAnno(annoClazz)));
  }

}
