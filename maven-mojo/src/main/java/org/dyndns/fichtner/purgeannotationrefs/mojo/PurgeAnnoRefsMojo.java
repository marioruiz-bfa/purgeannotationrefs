package org.dyndns.fichtner.purgeannotationrefs.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dyndns.fichtner.purgeannotationrefs.AnnotationReferenceRemover;

import org.dyndns.fichtner.purgeannotationrefs.Matcher.RegExpMatcher;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Removes annotation references from classfiles.
 */
@Mojo(name = PurgeAnnoRefsMojo.PAR, defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class PurgeAnnoRefsMojo extends AbstractMojo {

  protected static final String PAR = "par";
  private static final Map<String, RemoveFrom> mapping = Arrays.stream(RemoveFrom.values())
      .collect(Collectors.toUnmodifiableMap(it -> it.name().toLowerCase(), Function.identity()));
  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(property = PAR + ".removes", required = true)
  private Remove[] removes;

  private static Iterable<RemoveFrom> configToTarget(Iterable<String> strings) {
    return
        StreamSupport.stream(strings.spliterator(), false)
            .map(mapping::get)
            .map(it -> Objects.requireNonNull(it, () -> "supported types are " + mapping.keySet()))
            .collect(Collectors.toSet());

  }

  private static List<File> collectFiles(File root) {
    return collectTo(root, new ArrayList<>(200));
  }

  private static List<File> collectTo(File baseDir, List<File> files) {
    for (File file : Objects.requireNonNullElseGet(baseDir.listFiles(), () -> new File[]{})) {
      if (file.isFile() && file.getName().endsWith(".class")) {
        files.add(file);
      } else if (file.isDirectory()) {
        collectTo(file, files);
      }
    }
    return files;
  }
  public void execute() throws MojoExecutionException, MojoFailureException {
    AnnotationReferenceRemover remover = getConfigured();
    for (File file : collectFiles(new File(project.getBuild().getOutputDirectory()))) {
      enhanceClass(remover, file);
    }
  }

  private void enhanceClass(AnnotationReferenceRemover remover, File file)
      throws MojoFailureException {

    try (var outputStream = new ByteArrayOutputStream()) {
      try (var inputStream = new FileInputStream(file)) {
        remover.optimize(inputStream, outputStream);
      }
      replace(file, outputStream.toByteArray());
    } catch (FileNotFoundException e) {
      throw new MojoFailureException("Failed to load " + file);
    } catch (IOException e) {
      throw new MojoFailureException("IOException " + e.getMessage());
    }
  }

  private void replace(File file, byte[] content) throws IOException {
    try (FileOutputStream stream = new FileOutputStream(file, false)) {
      stream.write(content);
    }
  }

  private AnnotationReferenceRemover getConfigured() {
    AnnotationReferenceRemover remover = new AnnotationReferenceRemover();
    for (Remove remove : removes) {
      Predicate<String> matcher = new RegExpMatcher(
          Pattern.compile(remove.regexp));
      if (remove.removeFroms == null) {
        remover.remove(matcher);
      } else {
        for (RemoveFrom removeFrom : configToTarget(remove.removeFroms)) {
          remover.removeFrom(removeFrom, matcher);
        }
      }
    }
    return remover;
  }

}
