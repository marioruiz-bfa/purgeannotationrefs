package org.dyndns.fichtner.purgeannotationrefs.testcode;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.TYPES;

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.junit.Test;

public class AnnotationReferenceRemoverTest {

	@Test
	public void typeIsSupported() {
		new AnnotationReferenceRemover().removeFrom(TYPES, anyMatcher());
	}

	private Matcher<String> anyMatcher() {
		return new StringMatcher("anyString");
	}

}
