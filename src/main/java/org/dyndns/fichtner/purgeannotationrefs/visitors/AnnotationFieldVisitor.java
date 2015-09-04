package org.dyndns.fichtner.purgeannotationrefs.visitors;

import static org.dyndns.fichtner.purgeannotationrefs.Util.atLeastOneMatches;
import static org.dyndns.fichtner.purgeannotationrefs.Util.typeToClassname;
import static org.objectweb.asm.Opcodes.ASM5;

import java.util.ArrayList;
import java.util.List;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

/**
 * Class for removing annotations from the fields.
 * 
 * @author Peter Fichtner
 */
public class AnnotationFieldVisitor extends ClassVisitor implements
		FilteringVisitor {

	private final List<Matcher<String>> filtered = new ArrayList<Matcher<String>>();

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public AnnotationFieldVisitor(final ClassVisitor classVisitor) {
		super(ASM5, classVisitor);
	}

	/**
	 * Add an annotation that should be filtered.
	 * 
	 * @param matcher the annotation to filter
	 */
	public void addFiltered(final Matcher<String> matcher) {
		this.filtered.add(matcher);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return new FieldVisitor(ASM5, this.cv.visitField(access, name, desc,
				signature, value)) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				return atLeastOneMatches(AnnotationFieldVisitor.this.filtered,
						typeToClassname(desc)) ? null : super.visitAnnotation(
						desc, visible);
			}
		};
	}
}
