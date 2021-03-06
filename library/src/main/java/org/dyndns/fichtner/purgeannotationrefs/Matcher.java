package org.dyndns.fichtner.purgeannotationrefs;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Matcher implementations decide whether a passed string matches or not.
 *
 * @param <T> type
 * @author Peter Fichtner
 */
@FunctionalInterface
public interface Matcher<T> extends Predicate<T> {

  /**
   * A Comparison based matcher (matches if the two Strings are equals).
   *
   * @author Peter Fichtner
   */
  class StringMatcher implements Matcher<String> {

    private final String pattern;

    /**
     * Creates a new StringMatcher that matches if the passed string is
     * identical to the queried one.
     *
     * @param name the name to use
     */
    public StringMatcher(final String name) {
      this.pattern = name;
    }

    public boolean test(final String string) {
      return this.pattern.equals(string);
    }

  }

  /**
   * A RegExp based matcher (matches if RegExp matches the String).
   *
   * @author Peter Fichtner
   */
  class RegExpMatcher implements Matcher<String> {

    private final Pattern pattern;

    /**
     * Creates a new RegExpMatcher that matches using the passed pattern.
     *
     * @param pattern the pattern to use
     */
    public RegExpMatcher(final Pattern pattern) {
      this.pattern = pattern;
    }

    public boolean test(final String string) {
      return this.pattern.matcher(string).matches();
    }

  }

}