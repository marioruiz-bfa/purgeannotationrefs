package org.dyndns.fichtner.purgeannotationrefs.testcode.cuts;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.util.Arrays;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.dyndns.fichtner.purgeannotationrefs.testcode.MyAnno;

@MyAnno(ElementType.TYPE)
public class ExampleClass {

	private String field = "ok1";

	@MyAnno(FIELD)
	private String annotatedField = "ok2";

	@MyAnno(CONSTRUCTOR)
	public ExampleClass(int bar) {
		super();
	}

	public static void main(String[] args) throws IOException,
			SecurityException, NoSuchMethodException, NoSuchFieldException {
		@MyAnno(LOCAL_VARIABLE)
		int v = 9;
		new ExampleClass(7).foobar(v);

		InputStream resource = ExampleClass.class
				.getResourceAsStream(ExampleClass.class.getName().replace('.',
						File.separatorChar)
						+ ".class");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		new AnnotationReferenceRemover().remove(
				new StringMatcher(MyAnno.class.getName())).optimize(resource,
				outputStream);

		outputStream.close();
		System.out.println(Arrays.toString(outputStream.toByteArray()));
	}

	@MyAnno(METHOD)
	private void foobar(@MyAnno(PARAMETER) int foo) throws SecurityException,
			NoSuchMethodException, NoSuchFieldException {
		System.out.println("class "
				+ Arrays.toString(ExampleClass.class.getAnnotations()));
		System.out.println("constructor "
				+ Arrays.toString(ExampleClass.class.getDeclaredConstructor(
						int.class).getAnnotations()));
		System.out.println("method "
				+ Arrays.toString(ExampleClass.class.getDeclaredMethod(
						"foobar", int.class).getAnnotations()));
		System.out.println("field "
				+ Arrays.toString(ExampleClass.class.getDeclaredField(
						"annotatedField").getAnnotations()));
		System.out.println(this.field);
		System.out.println(this.annotatedField);
	}

}
