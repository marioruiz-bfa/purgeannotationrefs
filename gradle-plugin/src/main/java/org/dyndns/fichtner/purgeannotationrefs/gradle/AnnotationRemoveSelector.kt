package org.dyndns.fichtner.purgeannotationrefs.gradle

import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher
import org.dyndns.fichtner.purgeannotationrefs.Matcher.StringMatcher
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom
import java.util.function.Predicate
import java.util.regex.Pattern

data class AnnotationRemoveSelector(var from: RemoveFrom = RemoveFrom.ALL, var matcher: Predicate<String>) {
  companion object {
    @JvmStatic
    @JvmOverloads
    fun named(annotationName: String, from: RemoveFrom = RemoveFrom.ALL): AnnotationRemoveSelector {
      return AnnotationRemoveSelector(from, StringMatcher(annotationName))
    }

    @JvmStatic
    @JvmOverloads
    fun regexp(regexp: String, from: RemoveFrom = RemoveFrom.ALL): AnnotationRemoveSelector {
      return AnnotationRemoveSelector(from, RegExpMatcher(Pattern.compile(regexp)))
    }

    @JvmStatic
    @JvmOverloads
    fun matching(from: RemoveFrom = RemoveFrom.ALL, matcher: Predicate<String>): AnnotationRemoveSelector {
      return AnnotationRemoveSelector(from, matcher)
    }
  }
}