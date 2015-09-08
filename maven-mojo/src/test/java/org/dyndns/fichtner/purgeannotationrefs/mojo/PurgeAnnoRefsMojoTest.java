package org.dyndns.fichtner.purgeannotationrefs.mojo;

import static org.dyndns.fichtner.purgeannotationrefs.mojo.FileUtilities.copy;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.junit.Rule;
import org.junit.Test;

public class PurgeAnnoRefsMojoTest {

	@Rule
	public MojoRule rule = new MojoRule();

	@Rule
	public TestResources resources = new TestResources();

	@Test
	public void testInvalidProject() throws Exception {
		File targetDirectory = this.resources.getBasedir("project-A-removeAll");
		File pom = new File(targetDirectory, "pom.xml");
		assertTrue(pom.exists());

		MavenProject prj = prj(pom);
		File target = new File(
				build(targetDirectory, prj).getOutputDirectory(), "classes");

		copy(new File("../library/target/test-classes"), target);

		assertThat(
				Arrays.toString(loadClass(target).getAnnotations()),
				is("[@org.dyndns.fichtner.purgeannotationrefs.testcode.MyAnno(value=TYPE)]"));
		assertThat(
				Arrays.toString(loadClass(target).getDeclaredField(
						"annotatedField").getAnnotations()),
				is("[@org.dyndns.fichtner.purgeannotationrefs.testcode.MyAnno(value=FIELD)]"));
		assertThat(
				Arrays.toString(loadClass(target).getDeclaredMethod("foobar",
						int.class).getAnnotations()),
				is("[@org.dyndns.fichtner.purgeannotationrefs.testcode.MyAnno(value=METHOD)]"));

		PurgeAnnoRefsMojo mojo = (PurgeAnnoRefsMojo) this.rule.lookupMojo(
				"par", pom);

		this.rule.setVariableValueToObject(mojo, "project", prj);
		mojo.execute();

		assertThat(Arrays.toString(loadClass(target).getAnnotations()),
				is("[]"));
		assertThat(
				Arrays.toString(loadClass(target).getDeclaredField(
						"annotatedField").getAnnotations()), is("[]"));
		assertThat(
				Arrays.toString(loadClass(target).getDeclaredMethod("foobar",
						int.class).getAnnotations()), is("[]"));

	}

	@SuppressWarnings("resource")
	private Class<?> loadClass(File classpath) throws MalformedURLException,
			ClassNotFoundException, IOException {
		// do not close the class loader since annotations of the loaded class
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
