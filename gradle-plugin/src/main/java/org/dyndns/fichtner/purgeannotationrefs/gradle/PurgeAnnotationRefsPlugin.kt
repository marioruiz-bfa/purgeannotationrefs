package org.dyndns.fichtner.purgeannotationrefs.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class PurgeAnnotationRefsPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.tasks.register("purgeAnnotationRefs", PurgeAnnotationRefsTask::class.java)
  }
}