package org.dyndns.fichtner.purgeannotationrefs.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.dyndns.fichtner.purgeannotationrefs.ant.types.AnnotationRemoveSelector;
import org.dyndns.fichtner.purgeannotationrefs.optimizer.ZipOptimizer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.dyndns.fichtner.purgeannotationrefs.Util.isClass;
import static org.dyndns.fichtner.purgeannotationrefs.Util.isZip;

public class PurgeAnnotationRefsTask extends MatchingTask {

  private final List<AnnotationRemoveSelector> selectors = new ArrayList<>();
  private Path src;
  private File targetDir;
  private boolean overwrite;

  private static InputStream createInputStream(final File srcFile,
                                               final boolean srcIsTarget) throws IOException {
    return srcIsTarget ? readToMem(srcFile) : new FileInputStream(srcFile);
  }

  private static InputStream readToMem(final File file) throws IOException {
    try (DataInputStream dis = new DataInputStream(new FileInputStream(
        file))) {
      final byte[] buffer = new byte[(int) file.length()];
      dis.readFully(buffer);
      return new ByteArrayInputStream(buffer);
    }
  }

  /**
   * Adds a configuration to the task.
   *
   * @param ars the AnnotationRemoveSelector to add
   */
  public void addConfiguredRemove(final AnnotationRemoveSelector ars) {
    if (ars.getMatcher() == null) {
      throw new BuildException(
          "missing argument for remove, either name or regexp has to be set"); //$NON-NLS-1$
    }
    // don't check target: it may be null (indicates "all")
    this.selectors.add(ars);
  }

  /**
   * Forces overwrite (when using targetDir) even if targetfile is newer than
   * sourcefile.
   *
   * @param overwrite <code>true</code> forces overwriting
   */
  public void setOverwrite(final boolean overwrite) {
    this.overwrite = overwrite;
  }

  private void doWork(final File srcFile,
                      final AnnotationReferenceRemover remover) throws IOException {
    boolean srcIsTarget = this.targetDir == null;
    final File targetFile = srcIsTarget ? srcFile : new File(
        this.targetDir, srcFile.getName());
    // reevaluate
    srcIsTarget = targetFile.equals(srcFile);
    if (!srcIsTarget && !this.overwrite && targetFile.exists()
        && targetFile.lastModified() > srcFile.lastModified()) {
      log("File " + targetFile + " is uptodate"); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      log("Processing file " + srcFile); //$NON-NLS-1$
      if (isClass(srcFile.getName())) {
        try (final InputStream input = createInputStream(srcFile,
            srcIsTarget)) {
          try (final FileOutputStream output = new FileOutputStream(targetFile)) {
            remover.optimize(input, output);
          }
        }
      } else if (isZip(srcFile.getName())) {
        try (final ZipInputStream input = new ZipInputStream(
            createInputStream(srcFile, srcIsTarget))) {
          try (final ZipOutputStream output = new ZipOutputStream(
              new FileOutputStream(targetFile))) {
            new ZipOptimizer(remover) {
              @Override
              protected void processClass(final String name,
                                          final ZipInputStream zis, final ZipOutputStream zos)
                  throws IOException {
                log("Processing class " + name); //$NON-NLS-1$
                super.processClass(name, zis, zos);

              }
            }.optimize(input, output);
          }
        }
      }
    }
  }

  private AnnotationReferenceRemover createConfigured() {
    final AnnotationReferenceRemover remover = new AnnotationReferenceRemover();
    configure(remover);
    return remover;
  }

  private void configure(final AnnotationReferenceRemover remover) {
    for (final AnnotationRemoveSelector annotationRemoveSelector : this.selectors) {
      final RemoveFrom removeFrom = annotationRemoveSelector.getFrom();
      if (removeFrom == RemoveFrom.ALL || removeFrom == null) {
        for (RemoveFrom tmp : RemoveFrom.values()) {
          if (tmp != RemoveFrom.ALL) {
            configure(remover, annotationRemoveSelector, tmp);
          }
        }
      } else {
        configure(remover, annotationRemoveSelector, removeFrom);
      }
    }
  }

  private void configure(final AnnotationReferenceRemover remover,
                         final AnnotationRemoveSelector annotationRemoveSelector,
                         final RemoveFrom removeFrom) {
    remover.removeFrom(removeFrom, annotationRemoveSelector.getMatcher());
  }

  @Override
  public void execute() throws BuildException {
    if (this.src == null) {
      throw new BuildException("src path not set"); //$NON-NLS-1$
    }
    if (this.selectors.isEmpty()) {
      throw new BuildException("no annotations set to be removed"); //$NON-NLS-1$
    }

    final AnnotationReferenceRemover remover = createConfigured();
    final Project prj = getProject();
    for (final String file : this.src.list()) {
      final File pathElement = prj.resolveFile(file);
      if (!pathElement.exists()) {
        throw new BuildException(pathElement.getPath()
            + " does not exist!", getLocation()); //$NON-NLS-1$
      }
      try {
        doWork(pathElement.getAbsoluteFile(), remover);
      } catch (final IOException e) {
        throw new BuildException(e);
      }
    }
  }

  /**
   * Adds a path that should be processed as input.
   *
   * @param path the Path to process
   */
  public void addConfiguredSrc(final Path path) {
    this.src = extendClassPath(this.src, path);
  }

  private Path extendClassPath(final Path arg, final Path add) {
    final Path classPath = arg == null ? new Path(getProject()) : arg;
    if (add != null) {
      classPath.add(add);
    }
    return classPath;
  }

  public void setTargetDir(final File targetDir) {
    this.targetDir = targetDir;
  }

}
