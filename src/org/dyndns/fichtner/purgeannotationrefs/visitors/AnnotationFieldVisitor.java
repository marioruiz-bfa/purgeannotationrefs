package org.dyndns.fichtner.purgeannotationrefs.visitors;

import static org.dyndns.fichtner.purgeannotationrefs.Util.atLeastOneMatches;

import java.util.ArrayList;
import java.util.List;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.dyndns.fichtner.purgeannotationrefs.Util;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

/**
 * Class for removing annotations from the fields.
 * 
 * @author Peter Fichtner
 */
public class AnnotationFieldVisitor extends ClassAdapter implements
		FilteringVisitor {

	private final List<Matcher> filtered = new ArrayList<Matcher>();

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public AnnotationFieldVisitor(final ClassVisitor classVisitor) {
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
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return new FieldAdapter(this.cv.visitField(access, name, desc,
				signature, value)) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				return atLeastOneMatches(AnnotationFieldVisitor.this.filtered,
						Util.translate(desc)) ? null : super.visitAnnotation(
						desc, visible);
			}
		};
	}
}
