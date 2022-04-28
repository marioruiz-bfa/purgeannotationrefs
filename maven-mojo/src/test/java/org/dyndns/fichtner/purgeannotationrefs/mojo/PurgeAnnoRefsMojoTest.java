package org.dyndns.fichtner.purgeannotationrefs.mojo;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;
import org.dyndns.fichtner.purgeannotationrefs.mojo.test.GradleMojoTestCase;
import org.dyndns.fichtner.purgeannotationrefs.mojo.test.TestResources;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.*;
import static org.dyndns.fichtner.purgeannotationrefs.mojo.FileUtilities.copy;
import static org.hamcrest.MatcherAssert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
public class PurgeAnnoRefsMojoTest extends GradleMojoTestCase {

  private static final String EXAMPLE_CLASS = "org.dyndns.fichtner.purgeannotationrefs.testcode.cuts.ExampleClass";
  @RegisterExtension
  public static TestResources resources = new TestResources();

  @BeforeAll
  void setup() throws Exception {
    super.setUp();
  }

  @Test
  public void projectA_removeAll() throws Exception {
    File target = prepare("project-A-removeAll");
    Class<?> clazz = loadClass(target, EXAMPLE_CLASS);
    assertThat(clazz, hasNoAnno());
    assertThat(annotatedField(clazz), hasNoAnno());
    assertThat(intConstructor(clazz), hasNoAnno());
    assertThat(foobarMethod(clazz), hasNoAnno());
  }

  @Test
  public void projectB_RemoveOnlyFromField() throws Exception {
    File target = prepare("project-B-removeFieldOnly");
    Class<?> clazz = loadClass(target, EXAMPLE_CLASS);
    assertThat(clazz, isAnnotated(TYPES));
    assertThat(annotatedField(clazz), hasNoAnno());
    assertThat(intConstructor(clazz), isAnnotated(CONSTRUCTORS));
    assertThat(foobarMethod(clazz), isAnnotated(METHODS));
  }

  @Test
  public void projectC_removeConstructorsAndMethods() throws Exception {
    File target = prepare("project-C-removeConstructorsAndMethods");
    Class<?> clazz = loadClass(target, EXAMPLE_CLASS);
    assertThat(clazz, isAnnotated(TYPES));
    assertThat(annotatedField(clazz), isAnnotated(FIELDS));
    assertThat(intConstructor(clazz), hasNoAnno());
    assertThat(foobarMethod(clazz), hasNoAnno());
  }

  private File prepare(String dir) throws
      Exception {
    File targetDirectory = resources.getBasedir(dir);
    File pom = new File(targetDirectory, "pom.xml");
    assertTrue(pom.exists());

    MavenProject prj = prj(pom);
    File target = new File(
        build(targetDirectory, prj).getOutputDirectory(), "classes");

    copy(new File("../library/target/test-classes"), target);
    assertAllAnnotationsArePresent(target);

    PurgeAnnoRefsMojo mojo = (PurgeAnnoRefsMojo) lookupConfiguredMojo(prj, "par");

    this.setVariableValueToObject(mojo, "project", prj);
    mojo.execute();
    return target;
  }

  private void assertAllAnnotationsArePresent(File target)
      throws ClassNotFoundException, IOException,
      NoSuchFieldException, NoSuchMethodException {
    Class<?> clazz = loadClass(target, EXAMPLE_CLASS);
    assertThat(clazz, isAnnotated(TYPES));
    assertThat(annotatedField(clazz), isAnnotated(FIELDS));
    assertThat(foobarMethod(clazz), isAnnotated(METHODS));
    assertThat(intConstructor(clazz), isAnnotated(CONSTRUCTORS));
  }

  private Field annotatedField(Class<?> clazz) throws NoSuchFieldException {
    return clazz.getDeclaredField("annotatedField");
  }

  private Constructor<?> intConstructor(Class<?> clazz)
      throws NoSuchMethodException {
    return clazz.getDeclaredConstructor(int.class);
  }

  private Method foobarMethod(Class<?> clazz) throws NoSuchMethodException {
    return clazz.getDeclaredMethod("foobar", int.class);
  }

  private Matcher<AnnotatedElement> hasNoAnno() {
    return new TypeSafeMatcher<>() {
      public void describeTo(Description description) {
        description.appendText("an element without annotations");
      }

      @Override
      public boolean matchesSafely(AnnotatedElement annotatedElement) {
        return annotatedElement.getAnnotations().length == 0;
      }
    };
  }

  private Matcher<AnnotatedElement> isAnnotated(final RemoveFrom removeFrom) {
    return new TypeSafeMatcher<>() {
      public void describeTo(Description description) {
        description.appendText("an element annotated with "
            + removeFrom);
      }

      @Override
      public boolean matchesSafely(AnnotatedElement annotatedElement) {
        return Arrays.toString(annotatedElement.getAnnotations())
            .equals(expected(removeFrom));
      }

      private String expected(final RemoveFrom removeFrom) {
        return "[@org.dyndns.fichtner.purgeannotationrefs.testcode.MyAnno(value="
            + removeFrom + ")]";
      }
    };
  }

  @SuppressWarnings("resource")
  private Class<?> loadClass(File classpath, String classname)
      throws ClassNotFoundException, IOException {
    // do not close the classloader since annotations of the loaded class
    // cannot be accessed otherwise!
    return new URLClassLoader(new URL[]{classpath.toURI().toURL()})
        .loadClass(classname);
  }

  private MavenProject prj(File pom) {

    MavenProject prj;
    try {
      prj = readMavenProject(pom);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    prj.setFile(pom);
    prj.setVersion("1");
    prj.setArtifact(new ProjectArtifact(prj));
    return prj;
  }

  private Build build(File targetDirectory, MavenProject prj) {
    Build build = prj.getBuild();
    build.setDirectory(new File(targetDirectory, "target")
        .getAbsolutePath());
    build.setOutputDirectory(new File(targetDirectory, "target")
        .getAbsolutePath());
    build.setFinalName("target/output.jar");
    return build;
  }

  protected MavenProject readMavenProject(File basedir)
      throws Exception {
    File pom = new File(basedir, "pom.xml");
    MavenExecutionRequest request = new DefaultMavenExecutionRequest();
    request.setBaseDirectory(basedir);
    ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
    configuration.setRepositorySession(new DefaultRepositorySystemSession());
    MavenProject project = lookup(ProjectBuilder.class).build(pom, configuration).getProject();
    assertNotNull(project);
    return project;
  }

}
