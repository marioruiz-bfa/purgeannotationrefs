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
 * Class for removing annotations from method and constructor parameters.
 * 
 * @author Peter Fichtner
 */
public class AnnotationParameterVisitor extends ClassAdapter implements
		FilteringVisitor {

	private final List<Matcher> filtered = new ArrayList<Matcher>();

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public AnnotationParameterVisitor(ClassVisitor classVisitor) {
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
			public AnnotationVisitor visitParameterAnnotation(int parameter,
					String desc, boolean visible) {
				return atLeastOneMatches(
						AnnotationParameterVisitor.this.filtered,
						Util.translate(desc)) ? null : super
						.visitParameterAnnotation(parameter, desc, visible);
			}

		};
	}

}
