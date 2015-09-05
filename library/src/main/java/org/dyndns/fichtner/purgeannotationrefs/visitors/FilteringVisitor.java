package org.dyndns.fichtner.purgeannotationrefs.visitors;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;

/**
 * Visitors that are able to filter annotations.
 * 
 * @author Peter Fichtner
 */
public interface FilteringVisitor {

	/**
	 * Add the passed annotation to the list of filtered annotations.
	 * 
	 * @param matcher the annotation to filter
	 */
	void addFiltered(final Matcher<String> matcher);

}
