package org.dyndns.fichtner.purgeannotationrefs.visitors;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;

/**
 * Implementations are able to filter.
 *
 * @author Peter Fichtner
 */
public interface Filterable {

  /**
   * Add the passed Matcher to the List of Matchers
   *
   * @param matcher the Matcher for filtering
   */
  void addFiltered(Matcher<String> matcher);

}
