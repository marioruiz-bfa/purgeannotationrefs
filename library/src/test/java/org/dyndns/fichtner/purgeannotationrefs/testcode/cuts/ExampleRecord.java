package org.dyndns.fichtner.purgeannotationrefs.testcode.cuts;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.testcode.MyAnno;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.*;

@MyAnno(TYPES)
public record ExampleRecord(@MyAnno(RECORD_COMPONENTS) String stringComponent, int intComponent) {


  private static String field = "ok1";

  @MyAnno(FIELDS)
  private static String annotatedField = "ok2";

  @MyAnno(CONSTRUCTORS)
  public ExampleRecord {

  }

  public ExampleRecord(int value) {
    this("", value);
  }

  @MyAnno(METHODS)
  public String frobnicate(@MyAnno(PARAMETERS) int extra) throws SecurityException, NoSuchMethodException, NoSuchFieldException {
    System.out.println("record component " + Arrays.toString(ExampleRecord.class.getRecordComponents()[0].getAnnotations()));
    System.out.println("class " + Arrays.toString(ExampleRecord.class.getAnnotations()));
    System.out.println("constructor " + Arrays.toString(ExampleRecord.class.getDeclaredConstructor(String.class, int.class).getAnnotations()));
    System.out.println("method " + Arrays.toString(ExampleRecord.class.getDeclaredMethod("frobnicate", int.class).getAnnotations()));
    System.out.println("field " + Arrays.toString(ExampleRecord.class.getDeclaredField("annotatedField").getAnnotations()));
    System.out.println(field);
    System.out.println(annotatedField);
    return stringComponent + intComponent + extra;
  }

  private void methodWithoutAnnotation() {
    System.out.println("anotherMethod");
  }


  public static void main(String[] args) throws IOException, SecurityException, NoSuchMethodException, NoSuchFieldException {
    new ExampleRecord("E", 7).frobnicate(9);

    InputStream resource = ExampleRecord.class.getResourceAsStream(ExampleRecord.class.getName().replace('.', File.separatorChar) + ".class");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    new AnnotationReferenceRemover().remove(new Matcher.StringMatcher(MyAnno.class.getName())).optimize(resource, outputStream);

    outputStream.close();
    System.out.println(Arrays.toString(outputStream.toByteArray()));
  }
}
