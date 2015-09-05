package org.dyndns.fichtner.purgeannotationrefs.ant.types;

import java.util.regex.Pattern;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;

public class AnnotationRemoveSelector {

	private Target from;
	private Matcher matcher;

	public AnnotationRemoveSelector() {
		super();
	}

	public Target getFrom() {
		return this.from;
	}

	public void setFrom(final Target target) {
		this.from = target;
	}

	public Matcher getMatcher() {
		return this.matcher;
	}

	/**
	 * Please use {@link #setName(String)}.
	 * 
	 * @param annotation the annotation to purge
	 * @deprecated Please use {@link #setName(String)}
	 */
	@Deprecated
	public void setAnnotation(final String annotation) {
		setName(annotation);
	}

	public void setName(final String annoName) {
		this.matcher = new StringMatcher(annoName);
	}

	public void setRegexp(final String regexp) {
		this.matcher = new RegExpMatcher(Pattern.compile(regexp));
	}
}
