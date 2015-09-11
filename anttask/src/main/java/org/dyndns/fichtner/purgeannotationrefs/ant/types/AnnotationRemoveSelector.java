package org.dyndns.fichtner.purgeannotationrefs.ant.types;

import java.util.regex.Pattern;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;

public class AnnotationRemoveSelector {

	private RemoveFrom from;
	private Matcher<String> matcher;

	public AnnotationRemoveSelector() {
		super();
	}

	public RemoveFrom getFrom() {
		return this.from;
	}

	public void setFrom(RemoveFrom removeFrom) {
		this.from = removeFrom;
	}

	public Matcher<String> getMatcher() {
		return this.matcher;
	}

	public void setName(String annoName) {
		this.matcher = new StringMatcher(annoName);
	}

	public void setRegexp(String regexp) {
		this.matcher = new RegExpMatcher(Pattern.compile(regexp));
	}

}
