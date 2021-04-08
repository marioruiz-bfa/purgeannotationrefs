package org.dyndns.fichtner.purgeannotationrefs.testcode;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.CONSTRUCTORS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.FIELDS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.METHODS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.PARAMETERS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.TYPES;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.TestHelper.removeAnno;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.classToJasmin;
import static org.dyndns.fichtner.purgeannotationrefs.testcode.util.jasmin.JasminUtil.streamToJasmin;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Iterator;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.dyndns.fichtner.purgeannotationrefs.testcode.cuts.ExampleClass;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.objectweb.asm.Type;

public class TestExampleClass {

	private final static Class<MyAnno> annoClazz = MyAnno.class;

	@Test
	public void checkOriginalClassHas5Annotations() throws IOException {
		String[] dump = classToJasmin(ExampleClass.class).split("(\\r\\n|\\r|\\n)");
		assertAnnoCountIs(5, dump);
		assertThat(count(dump, isAnno(annoClazz), TYPES), is(1));
		assertThat(count(dump, isAnno(annoClazz), FIELDS), is(1));
		assertThat(count(dump, isAnno(annoClazz), CONSTRUCTORS), is(1));
		assertThat(count(dump, isAnno(annoClazz), METHODS), is(1));
		assertThat(count(dump, isAnno(annoClazz), PARAMETERS), is(1));
		// assertThat(count(dump, isAnno(annoClazz), LOCAL_VARIABLES), is(0));
	}

	@Test
	public void whenRemovingAllRefsTheAnnotationIsNotFoundAtAll()
			throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
				ExampleClass.class,
				new AnnotationReferenceRemover().remove(new StringMatcher(
						annoClazz.getName()))));
		try {
			assertAnnoCountIs(0, streamToJasmin(is).split("(\\r\\n|\\r|\\n)"));
		} finally {
			is.close();
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
		ByteArrayInputStream is = new ByteArrayInputStream(removeAnno(
				ExampleClass.class,
				new AnnotationReferenceRemover().removeFrom(removeFrom,
						new StringMatcher(annoClazz.getName()))));
		try {
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

		} finally {
			is.close();
		}
	}

	private void assertCount(RemoveFrom removeFrom, String[] dump,
			RemoveFrom elementToCount) {
		assertThat(count(dump, isAnno(annoClazz), elementToCount),
				is(removeFrom == elementToCount ? 0 : 1));
	}

	private int count(String[] lines, Matcher<String> matcher,
			RemoveFrom removeFrom) {
		Type valueType = Type.getType(getType(MyAnno.class, "value"));
		int cnt = 0;
		for (Iterator<String> iterator = Arrays.asList(lines).iterator(); iterator
				.hasNext();) {
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
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
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
