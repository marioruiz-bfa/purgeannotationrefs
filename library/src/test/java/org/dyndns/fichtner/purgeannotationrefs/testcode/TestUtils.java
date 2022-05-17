package org.dyndns.fichtner.purgeannotationrefs.testcode;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

public class TestUtils {

  private TestUtils() {
  }

  public static int count(String[] lines, Predicate<String> matcher) {
    int cnt = 0;
    for (String line : lines) {
      if (matcher.test(line)) {
        cnt++;
      }

    }
    return cnt;
  }

  public static int count(String[] lines, Predicate<String> matcher,
                          RemoveFrom removeFrom, Class<? extends Annotation> annotationClass) {
    Type valueType = Type.getType(getType(annotationClass, "value"));
    int cnt = 0;

    for (Iterator<String> iterator = Arrays.asList(lines).iterator(); iterator
        .hasNext(); ) {
      String line = iterator.next();
      if (matcher.test(line)
          && iterator.hasNext()
          && ("value e " + valueType.getDescriptor() + " = \""
          + removeFrom + "\"").equals(iterator.next())) {
        cnt++;
      }

    }
    return cnt;
  }


  private static Class<?> getType(Class<? extends Annotation> annoClass, String field) {
    try {
      return annoClass.getDeclaredMethod(field).getReturnType();
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }
}
