package org.dyndns.fichtner.purgeannotationrefs.visitors;

import java.util.ArrayList;
import java.util.List;


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
public class AnnotationFieldVisitor extends ClassAdapter {

	private final List<String> filtered = new ArrayList<String>();

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
	 * @param anno the annotation to filter
	 * @return this instance
	 */
	public AnnotationFieldVisitor addFiltered(final String anno) {
		this.filtered.add(anno);
		return this;
	}

	private boolean isFiltered(final String classname) {
		return this.filtered.contains(classname);
	}

	@Override
	public FieldVisitor visitField(int arg0, String arg1, String arg2,
			String arg3, Object arg4) {
		return new FieldAdapter(this.cv
				.visitField(arg0, arg1, arg2, arg3, arg4)) {
			@Override
			public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
				return isFiltered(Util.translate(arg0)) ? null : super
						.visitAnnotation(arg0, arg1);
			}
		};
	}
}
