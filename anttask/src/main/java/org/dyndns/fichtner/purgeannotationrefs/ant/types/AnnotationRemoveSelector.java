package org.dyndns.fichtner.purgeannotationrefs.ant.types;

import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher;
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AnnotationRemoveSelector {

  private RemoveFrom from;
  private Predicate<String> matcher;

  public AnnotationRemoveSelector() {
    super();
  }

  public RemoveFrom getFrom() {
    return this.from;
  }

  public void setFrom(RemoveFrom removeFrom) {
    this.from = removeFrom;
  }

  public Predicate<String> getMatcher() {
    return this.matcher;
  }

  public void setName(String annoName) {
    this.matcher = new StringMatcher(annoName);
  }

  public void setRegexp(String regexp) {
    this.matcher = new RegExpMatcher(Pattern.compile(regexp));
  }

}
