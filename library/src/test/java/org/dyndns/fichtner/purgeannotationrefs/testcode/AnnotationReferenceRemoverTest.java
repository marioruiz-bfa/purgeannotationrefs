package org.dyndns.fichtner.purgeannotationrefs.testcode;

import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.TYPE;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.junit.Test;

public class AnnotationReferenceRemoverTest {

	@Test
	public void typeIsSupported() {
		new AnnotationReferenceRemover().removeFrom(TYPE, anyMatcher());
	}

	@Test(expected = IllegalArgumentException.class)
	public void localVariableNotSupported() {
		new AnnotationReferenceRemover().removeFrom(LOCAL_VARIABLE,
				anyMatcher());
	}

	private Matcher<String> anyMatcher() {
		return new StringMatcher("anyString");
	}

}
