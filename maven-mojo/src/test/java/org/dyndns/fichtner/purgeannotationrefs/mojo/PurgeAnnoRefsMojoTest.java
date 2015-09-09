package org.dyndns.fichtner.purgeannotationrefs.mojo;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static org.dyndns.fichtner.purgeannotationrefs.mojo.FileUtilities.copy;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

public class PurgeAnnoRefsMojoTest {

	@Rule
	public MojoRule rule = new MojoRule();

	@Rule
	public TestResources resources = new TestResources();

	@Test
	public void projectA_removeAll() throws Exception {
		File targetDirectory = this.resources.getBasedir("project-A-removeAll");
		File pom = new File(targetDirectory, "pom.xml");
		assertTrue(pom.exists());

		MavenProject prj = prj(pom);
		File target = new File(
				build(targetDirectory, prj).getOutputDirectory(), "classes");

		copy(new File("../library/target/test-classes"), target);
		assertAllAnnotationsArePresent(target);

		PurgeAnnoRefsMojo mojo = (PurgeAnnoRefsMojo) this.rule.lookupMojo(
				"par", pom);

		this.rule.setVariableValueToObject(mojo, "project", prj);
		mojo.execute();

		Class<?> clazz = loadMyExampleClass(target);
		assertThat(clazz, hasNoAnno());
		assertThat(annotatedField(clazz), hasNoAnno());
		assertThat(intConstructor(clazz), hasNoAnno());
		assertThat(foobarMethod(clazz), hasNoAnno());
	}

	@Test
	public void projectB_RemoveOnlyFromField() throws Exception {
		File targetDirectory = this.resources
				.getBasedir("project-B-removeFieldOnly");
		File pom = new File(targetDirectory, "pom.xml");
		assertTrue(pom.exists());

		MavenProject prj = prj(pom);
		File target = new File(
				build(targetDirectory, prj).getOutputDirectory(), "classes");

		copy(new File("../library/target/test-classes"), target);
		assertAllAnnotationsArePresent(target);

		PurgeAnnoRefsMojo mojo = (PurgeAnnoRefsMojo) this.rule.lookupMojo(
				"par", pom);

		this.rule.setVariableValueToObject(mojo, "project", prj);
		mojo.execute();

		Class<?> clazz = loadMyExampleClass(target);
		assertThat(clazz, isAnnotated(TYPE));
		assertThat(annotatedField(clazz), hasNoAnno());
		assertThat(intConstructor(clazz), isAnnotated(CONSTRUCTOR));
		assertThat(foobarMethod(clazz), isAnnotated(METHOD));
	}

	@Test
	public void projectC_removeConstructorsAndMethods() throws Exception {
		File targetDirectory = this.resources
				.getBasedir("project-C-removeConstructorsAndMethods");
		File pom = new File(targetDirectory, "pom.xml");
		assertTrue(pom.exists());

		MavenProject prj = prj(pom);
		File target = new File(
				build(targetDirectory, prj).getOutputDirectory(), "classes");

		copy(new File("../library/target/test-classes"), target);
		assertAllAnnotationsArePresent(target);

		PurgeAnnoRefsMojo mojo = (PurgeAnnoRefsMojo) this.rule.lookupMojo(
				"par", pom);

		this.rule.setVariableValueToObject(mojo, "project", prj);
		mojo.execute();

		Class<?> clazz = loadMyExampleClass(target);
		assertThat(clazz, isAnnotated(TYPE));
		assertThat(annotatedField(clazz), isAnnotated(FIELD));
		assertThat(intConstructor(clazz), hasNoAnno());
		assertThat(foobarMethod(clazz), hasNoAnno());
	}

	private void assertAllAnnotationsArePresent(File target)
			throws MalformedURLException, ClassNotFoundException, IOException,
			NoSuchFieldException, NoSuchMethodException {
		Class<?> clazz = loadMyExampleClass(target);
		assertThat(clazz, isAnnotated(TYPE));
		assertThat(annotatedField(clazz), isAnnotated(FIELD));
		assertThat(foobarMethod(clazz), isAnnotated(METHOD));
		assertThat(intConstructor(clazz), isAnnotated(CONSTRUCTOR));
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
		return new TypeSafeMatcher<AnnotatedElement>() {
			public void describeTo(Description description) {
				description.appendText("an element without annotations");
			}

			@Override
			public boolean matchesSafely(AnnotatedElement annotatedElement) {
				return annotatedElement.getAnnotations().length == 0;
			}
		};
	}

	private Matcher<AnnotatedElement> isAnnotated(final ElementType type) {
		return new TypeSafeMatcher<AnnotatedElement>() {
			public void describeTo(Description description) {
				description.appendText("an element annotated with " + type);
			}

			@Override
			public boolean matchesSafely(AnnotatedElement annotatedElement) {
				return Arrays.toString(annotatedElement.getAnnotations())
						.equals(expected(type));
			}

			private String expected(final ElementType type) {
				return "[@org.dyndns.fichtner.purgeannotationrefs.testcode.MyAnno(value="
						+ type + ")]";
			}
		};
	}

	@SuppressWarnings("resource")
	private Class<?> loadMyExampleClass(File classpath)
			throws MalformedURLException, ClassNotFoundException, IOException {
		// do not close the classloader since annotations of the loaded class
		// cannot be accessed otherwise!
		return new URLClassLoader(new URL[] { classpath.toURI().toURL() })
				.loadClass("org.dyndns.fichtner.purgeannotationrefs.testcode.cuts.ExampleClass");
	}

	private MavenProject prj(File pom) {
		MavenProject prj = new MavenProject();
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

}
