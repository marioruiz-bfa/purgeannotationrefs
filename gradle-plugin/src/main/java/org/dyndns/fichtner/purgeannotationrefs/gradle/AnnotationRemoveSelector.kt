package org.dyndns.fichtner.purgeannotationrefs.gradle

import org.dyndns.fichtner.purgeannotationrefs.Matcher
import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom
import java.util.regex.Pattern

data class AnnotationRemoveSelector(var from: RemoveFrom = RemoveFrom.ALL, var matcher: Matcher<String>) {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun named(annotationName: String, from: RemoveFrom = RemoveFrom.ALL): AnnotationRemoveSelector {
            return AnnotationRemoveSelector(from, Matcher.StringMatcher(annotationName))
        }
        @JvmStatic
        @JvmOverloads
        fun regexp(regexp: String, from: RemoveFrom = RemoveFrom.ALL): AnnotationRemoveSelector {
            return AnnotationRemoveSelector(from, RegExpMatcher(Pattern.compile(regexp)))
        }
        @JvmStatic
        @JvmOverloads
        fun matching(from: RemoveFrom = RemoveFrom.ALL, matcher: Matcher<String>): AnnotationRemoveSelector {
            return AnnotationRemoveSelector(from, matcher)
        }
    }
}