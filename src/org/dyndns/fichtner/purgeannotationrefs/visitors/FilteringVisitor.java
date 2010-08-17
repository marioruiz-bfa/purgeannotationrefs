package org.dyndns.fichtner.purgeannotationrefs.visitors;

/**
 * Visitors that are able to filter annotations.
 * 
 * @author Peter Fichtner
 */
public interface FilteringVisitor {

	/**
	 * Add the passed annotation to the list of filtered annotations.
	 * 
	 * @param anno the annotation to filter
	 */
	void addFiltered(final String anno);

}
