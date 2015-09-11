package org.dyndns.fichtner.purgeannotationrefs.visitors;

import static org.dyndns.fichtner.purgeannotationrefs.Util.atLeastOneMatches;
import static org.dyndns.fichtner.purgeannotationrefs.Util.annotationRemover;
import static org.dyndns.fichtner.purgeannotationrefs.Util.typeToClassname;
import static org.objectweb.asm.Opcodes.ASM5;

import java.util.ArrayList;
import java.util.List;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Base class for removing annotations from methods/constructors.
 * 
 * @author Peter Fichtner
 */
public class DefaultAnnotationMethodVisitor extends ClassVisitor implements
		Filterable {

	private final List<Matcher<String>> filtered = new ArrayList<Matcher<String>>();

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public DefaultAnnotationMethodVisitor(ClassVisitor classVisitor) {
		super(ASM5, classVisitor);
	}

	/**
	 * Add an annotation that should be filtered.
	 * 
	 * @param matcher the annotation to filter
	 */

	public void addFiltered(Matcher<String> matcher) {
		this.filtered.add(matcher);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return new MethodVisitor(ASM5, this.cv.visitMethod(access, name, desc,
				signature, exceptions)) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				return matches(desc) ? annotationRemover() : super
						.visitAnnotation(desc, visible);
			}

		};
	}

	private boolean matches(String desc) {
		return atLeastOneMatches(this.filtered, typeToClassname(desc));
	}

}
