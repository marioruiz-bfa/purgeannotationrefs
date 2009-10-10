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
 * Base class for removing annotations from methods/constructors.
 * 
 * @author Peter Fichtner
 */
public class DefaultAnnotationMethodVisitor extends ClassAdapter {

	private final List<String> filtered = new ArrayList<String>();

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
	 * @param anno the annotation to filter
	 * @return this instance
	 */

	public DefaultAnnotationMethodVisitor addFiltered(final String anno) {
		this.filtered.add(anno);
		return this;
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
			public AnnotationVisitor visitAnnotation(final String name,
					final boolean arg1) {
				return isFiltered(Util.translate(name)) ? null : super
						.visitAnnotation(name, arg1);
			}
		};
	}

}
