package org.dyndns.fichtner.purgeannotationrefs.gradle

import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom
import org.dyndns.fichtner.purgeannotationrefs.Util
import org.dyndns.fichtner.purgeannotationrefs.optimizer.ZipOptimizer
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkerExecutor
import java.io.*
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@CacheableTask
open class PurgeAnnotationRefsTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val fileSystemOperations: FileSystemOperations
) : DefaultTask() {
    @get:Input
    @get:Optional
    val overwrite: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceDir: DirectoryProperty = objectFactory.directoryProperty()

    @get:OutputDirectory
    val targetDir: DirectoryProperty = objectFactory.directoryProperty()

    @get:Input
    val selectors: ListProperty<AnnotationRemoveSelector> = objectFactory.listProperty()

    fun selectors(action: Action<ListProperty<AnnotationRemoveSelector>>) {
        action.execute(selectors)
    }

    @TaskAction
    fun purgeAnnotations() {
        val srcIsTarget = sourceDir.asFile.get().equals(targetDir.asFile.get())
        if (overwrite.get() && !srcIsTarget) {
            fileSystemOperations.delete {
                delete(targetDir)
            }
        }
        targetDir.get().asFile.mkdirs()
        val remover = createConfigured()

        sourceDir.asFileTree.forEach {
            doWork(it, remover, srcIsTarget)
        }
    }

    private fun createConfigured(): AnnotationReferenceRemover {
        return AnnotationReferenceRemover().apply {
            for (removeSelector in selectors.get()) {
                val removeFrom: RemoveFrom = removeSelector.from
                if (removeFrom == RemoveFrom.ALL) {
                    for (tmp in RemoveFrom.values().filter { it != RemoveFrom.ALL }) {
                        removeFrom(tmp, removeSelector.matcher)
                    }
                } else {
                    removeFrom(removeFrom, removeSelector.matcher)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun doWork(
        srcFile: File,
        remover: AnnotationReferenceRemover,
        srcIsTarget: Boolean
    ) {
        val targetFile = File(
            targetDir.asFile.get().absolutePath, srcFile.name
        )
        // reevaluate
        logger.info("Processing file $srcFile") //$NON-NLS-1$
        if (Util.isClass(srcFile.name)) {
            createInputStream(
                srcFile,
                srcIsTarget
            ).use { input ->
                FileOutputStream(targetFile).use { output ->
                    remover.optimize(
                        input,
                        output
                    )
                }
            }
        } else if (Util.isZip(srcFile.name)) {
            ZipInputStream(
                createInputStream(
                    srcFile,
                    srcIsTarget
                )
            ).use { input ->
                ZipOutputStream(
                    FileOutputStream(targetFile)
                ).use { output ->
                    object : ZipOptimizer(remover) {
                        @Throws(IOException::class)
                        override fun processClass(
                            name: String,
                            zis: ZipInputStream, zos: ZipOutputStream
                        ) {
                            logger.info("Processing class $name") //$NON-NLS-1$
                            super.processClass(name, zis, zos)
                        }
                    }.optimize(input, output)
                }
            }
        }
    }

    companion object {

        @Throws(IOException::class)
        private fun createInputStream(
            srcFile: File,
            srcIsTarget: Boolean
        ): InputStream {
            return if (srcIsTarget) readToMem(srcFile) else FileInputStream(srcFile)
        }

        @Throws(IOException::class)
        private fun readToMem(file: File): InputStream {
            DataInputStream(
                FileInputStream(
                    file
                )
            ).use { dis ->
                return ByteArrayInputStream(dis.readAllBytes())
            }
        }
    }
}