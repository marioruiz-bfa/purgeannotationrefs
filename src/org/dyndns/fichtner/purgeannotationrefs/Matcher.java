package org.dyndns.fichtner.purgeannotationrefs;

import java.util.regex.Pattern;

/**
 * Matcher implementations decide whether a passed string matches or not.
 * 
 * @author Peter Fichtner
 */
public interface Matcher {

	/**
	 * A RegExp based matcher (matches if the two Strings are equals).
	 * 
	 * @author Peter Fichtner
	 */
	public class StringMatcher implements Matcher {

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

		public boolean matches(final String string) {
			return this.pattern.equals(string);
		}

	}

	/**
	 * A RegExp based matcher (matches if RegExp matches the String).
	 * 
	 * @author Peter Fichtner
	 */
	public class RegExpMatcher implements Matcher {

		private final Pattern pattern;

		/**
		 * Creates a new RegExpMatcher that matches using the passed pattern.
		 * 
		 * @param pattern the pattern to use
		 */
		public RegExpMatcher(final Pattern pattern) {
			this.pattern = pattern;
		}

		public boolean matches(final String string) {
			return this.pattern.matcher(string).matches();
		}

	}

	/**
	 * Returns <code>true</code> if the passed String matches.
	 * 
	 * @param pattern the String to check
	 * @return <code>true</code> if the passed String matches
	 */
	boolean matches(String pattern);

}