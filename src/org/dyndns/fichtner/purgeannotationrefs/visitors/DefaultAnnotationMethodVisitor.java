package org.dyndns.fichtner.purgeannotationrefs.visitors;

import static org.dyndns.fichtner.purgeannotationrefs.Util.atLeastOneMatches;

import java.util.ArrayList;
import java.util.List;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Util;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * Base class for removing annotations from methods/constructors.
 * 
 * @author Peter Fichtner
 */
public class DefaultAnnotationMethodVisitor extends ClassAdapter implements
		FilteringVisitor {

	private final List<Matcher> filtered = new ArrayList<Matcher>();

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public DefaultAnnotationMethodVisitor(final ClassVisitor classVisitor) {
		super(classVisitor);
	}

	/**
	 * Add an annotation that should be filtered.
	 * 
	 * @param matcher the annotation to filter
	 */

	public void addFiltered(final Matcher matcher) {
		this.filtered.add(matcher);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		return new MethodAdapter(this.cv.visitMethod(access, name, desc,
				signature, exceptions)) {
			@Override
			public AnnotationVisitor visitAnnotation(final String desc,
					final boolean visible) {
				return atLeastOneMatches(
						DefaultAnnotationMethodVisitor.this.filtered,
						Util.translate(desc)) ? null : super.visitAnnotation(
						desc, visible);
			}
		};
	}

}
