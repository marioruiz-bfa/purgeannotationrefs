package org.dyndns.fichtner.purgeannotationrefs.testcode;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.TestHelper.removeAnno;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.classToJasmin;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.streamToJasmin;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Iterator;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.dyndns.fichtner.purgeannotationrefs.testcode.cuts.ExampleClass;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.objectweb.asm.Type;

public class TestExampleClass {

	private final static Class<MyAnno> annoClazz = MyAnno.class;

	@Test
	public void checkOriginalClassHas5Annotations() throws IOException {
		String[] dump = classToJasmin(ExampleClass.class).split("\\n");
		assertAnnoCountIs(5, dump);
		assertThat(count(dump, isAnno(annoClazz), TYPE), is(1));
		assertThat(count(dump, isAnno(annoClazz), FIELD), is(1));
		assertThat(count(dump, isAnno(annoClazz), CONSTRUCTOR), is(1));
		assertThat(count(dump, isAnno(annoClazz), METHOD), is(1));
		assertThat(count(dump, isAnno(annoClazz), PARAMETER), is(1));
		assertThat(count(dump, isAnno(annoClazz), LOCAL_VARIABLE), is(0));
	}

	@Test
	public void whenRemovingAllRefsTheAnnotationIsNotFoundAtAll()
			throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
				ExampleClass.class,
				new AnnotationReferenceRemover().remove(new StringMatcher(
						annoClazz.getName()))));
		try {
			assertAnnoCountIs(0, streamToJasmin(is).split("\\n"));
		} finally {
			is.close();
		}
	}

	@Test
	public void removeFromTypeOnly() throws IOException {
		removeOnlyType(TYPE);
	}

	@Test
	public void removeFromMethodOnly() throws IOException {
		removeOnlyType(METHOD);
	}

	@Test
	public void removeFromConstructorOnly() throws IOException {
		removeOnlyType(CONSTRUCTOR);
	}

	@Test
	public void removeFromFieldOnly() throws IOException {
		removeOnlyType(FIELD);
	}

	private void removeOnlyType(ElementType toRemove) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
				ExampleClass.class,
				new AnnotationReferenceRemover().removeFrom(toRemove,
						new StringMatcher(annoClazz.getName()))));
		try {
			String[] dump = streamToJasmin(is).split("\\n");
			assertCount(toRemove, dump, TYPE);
			assertCount(toRemove, dump, FIELD);
			assertCount(toRemove, dump, CONSTRUCTOR);
			assertCount(toRemove, dump, METHOD);
			assertCount(toRemove, dump, PARAMETER);
			// Local variable annotations are not retained in class files (JLS
			// 9.6.1.2)
			assertThat(count(dump, isAnno(annoClazz), LOCAL_VARIABLE), is(0));

		} finally {
			is.close();
		}
	}

	private void assertCount(ElementType removedElement, String[] dump,
			ElementType elementToCount) {
		assertThat(count(dump, isAnno(annoClazz), elementToCount),
				is(removedElement == elementToCount ? 0 : 1));
	}

	private int count(String[] lines, Matcher<String> matcher, ElementType type) {
		int cnt = 0;

		Type elementType = Type.getType(ElementType.class);
		for (Iterator<String> iterator = Arrays.asList(lines).iterator(); iterator
				.hasNext();) {
			String line = iterator.next();
			if (matcher.matches(line)
					&& iterator.hasNext()
					&& ("value e " + elementType.getDescriptor() + " = \""
							+ type + "\"").equals(iterator.next())) {
				cnt++;
			}

		}
		return cnt;
	}

	private void assertAnnoCountIs(int count, String[] s) {
		assertThat(count(s, isAnno(annoClazz)), is(count));
	}

	private static Matcher<String> isAnno(Class<MyAnno> clazz) {
		return is(".annotation" + " " + "visible" + " "
				+ Type.getType(clazz).getDescriptor());
	}

	private static int count(String[] lines, Matcher<String> matcher) {
		int cnt = 0;
		for (String line : lines) {
			if (matcher.matches(line)) {
				cnt++;
			}

		}
		return cnt;
	}

}
