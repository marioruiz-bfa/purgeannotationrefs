package org.dyndns.fichtner.purgeannotationrefs;

import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.CONSTRUCTORS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.FIELDS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.METHODS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.PARAMETERS;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.TYPES;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import org.dyndns.fichtner.purgeannotationrefs.optimizer.ClassOptimizer;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationClassVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationConstructorVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationFieldVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationMethodVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.AnnotationParameterVisitor;
import org.dyndns.fichtner.purgeannotationrefs.visitors.Filterable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Class for removing annotation references from classes (and their methods,
 * field, statements, ...)
 * 
 * @author Peter Fichtner
 */
public class AnnotationReferenceRemover implements ClassOptimizer {

	private static class Config {

		private final EnumMap<RemoveFrom, List<Matcher<String>>> map = new EnumMap<RemoveFrom, List<Matcher<String>>>(
				RemoveFrom.class);

		public void addFiltered(RemoveFrom removeFrom, Matcher<String> matcher) {
			List<Matcher<String>> data = this.map.get(removeFrom);
			if (data == null) {
				this.map.put(removeFrom,
						data = new ArrayList<Matcher<String>>());
			}
			data.add(matcher);
		}

		public List<Matcher<String>> getFiltered(RemoveFrom removeFrom) {
			List<Matcher<String>> matchers = this.map.get(removeFrom);
			return matchers == null ? Collections.<Matcher<String>> emptyList()
					: matchers;
		}

	}

	private final Config config = new Config();

	/**
	 * Int/Enum-wrapper for the Asm RewriteMode.
	 * 
	 * @author Peter Fichtner
	 */
	public static enum RewriteMode {
		/**
		 * @see ClassReader.EXPAND_FRAMES
		 */
		EXPAND_FRAMES(ClassReader.EXPAND_FRAMES), /**
		 * @see ClassReader.SKIP_CODE
		 */
		SKIP_CODE(ClassReader.SKIP_CODE), /**
		 * @see ClassReader.SKIP_DEBUG
		 */
		SKIP_DEBUG(ClassReader.SKIP_DEBUG), /**
		 * @see ClassReader.SKIP_FRAMES
		 */
		SKIP_FRAMES(ClassReader.SKIP_FRAMES);

		private final int value;

		private RewriteMode(int value) {
			this.value = value;
		}

		/**
		 * Return Asm's int value.
		 * 
		 * @return Asm's int value
		 */
		public int getValue() {
			return this.value;
		}

	}

	private Set<RewriteMode> rewriteMode = Collections.emptySet();

	/**
	 * Creates a new instance for the passed class (.class-file/bytecode). This
	 * class must be readable.
	 * 
	 * @param classfile path to the classfile
	 */
	public AnnotationReferenceRemover() {
		super();
	}

	/**
	 * Remove the passed annotation from <b>all</b> elements (class/methods/...)
	 * (if present).
	 * 
	 * @param matcher the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover remove(Matcher<String> matcher) {
		for (RemoveFrom removeFrom : RemoveFrom.values()) {
			removeFrom(removeFrom, matcher);
		}
		return this;
	}

	/**
	 * Remove the passed annotation from the passed elements (if present).
	 * 
	 * @param removeFrom remove from which elements
	 * @param matcher the annotation that should be removed
	 * @return this instance
	 */
	public AnnotationReferenceRemover removeFrom(RemoveFrom removeFrom,
			Matcher<String> matcher) {
		this.config.addFiltered(removeFrom, matcher);
		return this;
	}

	/**
	 * Set the rewrite mode.
	 * 
	 * @param rewriteMode the rewrite mode to set
	 */
	public void setRewriteMode(Set<RewriteMode> rewriteMode) {
		this.rewriteMode = rewriteMode;
	}

	private static int toInt(Iterable<RewriteMode> rewriteModes) {
		int value = 0;
		for (RewriteMode rewriteMode : rewriteModes) {
			value |= rewriteMode.value;
		}
		return value;
	}

	/**
	 * Writes the class back (replaces the existing class).
	 * 
	 * @param inputStream the stream to read from
	 * @param outputStream the stream to write to
	 * 
	 * @param classfile the classfile to read and write
	 * 
	 * @throws IOException on write errors
	 */
	public void optimize(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		new ClassReader(inputStream).accept(createVisitors(classWriter),
				toInt(this.rewriteMode));
		outputStream.write(classWriter.toByteArray());
	}

	private AnnotationParameterVisitor createVisitors(ClassWriter classWriter) {
		return createParameterVisitor(createFieldVisitor(createMethodVisitor(createConstructorVisitor(createClassVisitor(classWriter)))));
	}

	private AnnotationParameterVisitor createParameterVisitor(
			ClassVisitor classVisitor) {
		return configure(new AnnotationParameterVisitor(classVisitor),
				PARAMETERS);
	}

	private AnnotationFieldVisitor createFieldVisitor(ClassVisitor classVisitor) {
		return configure(new AnnotationFieldVisitor(classVisitor), FIELDS);
	}

	private AnnotationMethodVisitor createMethodVisitor(
			ClassVisitor classVisitor) {
		return configure(new AnnotationMethodVisitor(classVisitor), METHODS);
	}

	private AnnotationConstructorVisitor createConstructorVisitor(
			ClassVisitor classVisitor) {
		return configure(new AnnotationConstructorVisitor(classVisitor),
				CONSTRUCTORS);
	}

	private AnnotationClassVisitor createClassVisitor(ClassVisitor classVisitor) {
		return configure(new AnnotationClassVisitor(classVisitor), TYPES);
	}

	private <T extends Filterable> T configure(T filteringVisitor,
			RemoveFrom removeFrom) {
		for (Matcher<String> matcher : this.config.getFiltered(removeFrom)) {
			filteringVisitor.addFiltered(matcher);
		}
		return filteringVisitor;
	}

}
