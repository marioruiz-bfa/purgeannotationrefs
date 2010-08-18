package org.dyndns.fichtner.purgeannotationrefs;

/**
 * Matcher implementations decide whether a passed string matches or not.
 * 
 * @author Peter Fichtner
 */
public interface Matcher {

	/**
	 * Returns <code>true</code> if the passed String matches.
	 * 
	 * @param string the String to check
	 * @return <code>true</code> if the passed String matches
	 */
	boolean matches(String string);

}