package org.dyndns.fichtner.purgeannotationrefs.visitors;

import java.util.ArrayList;
import java.util.List;


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
public class AnnotationParameterVisitor extends ClassAdapter implements FilteringVisitor {

	private final List<String> filtered = new ArrayList<String>();

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
	 * @param anno the annotation to filter
	 */

	public void addFiltered(final String anno) {
		this.filtered.add(anno);
	}

	private boolean isFiltered(final String classname) {
		return this.filtered.contains(classname);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		return new MethodAdapter(this.cv.visitMethod(access, name, desc,
				signature, exceptions)) {
			@Override
			public AnnotationVisitor visitParameterAnnotation(int arg0,
					String name, boolean arg2) {
				return isFiltered(Util.translate(name)) ? null : super
						.visitParameterAnnotation(arg0, name, arg2);
			}

		};
	}

}
