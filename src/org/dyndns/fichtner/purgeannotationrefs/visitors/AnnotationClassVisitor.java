package org.dyndns.fichtner.purgeannotationrefs.visitors;

import java.util.ArrayList;
import java.util.List;

import org.dyndns.fichtner.purgeannotationrefs.Util;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

/**
 * Class for removing annotations from the class.
 * 
 * @author Peter Fichtner
 */
public class AnnotationClassVisitor extends ClassAdapter implements
		FilteringVisitor {

	private final List<String> filtered = new ArrayList<String>();

	/**
	 * Creates a new instance delegating all calls to the passed visitor.
	 * 
	 * @param classVisitor delegate visitor
	 */
	public AnnotationClassVisitor(final ClassVisitor classVisitor) {
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
	public AnnotationVisitor visitAnnotation(final String arg0,
			final boolean arg1) {
		return isFiltered(Util.translate(arg0)) ? null : super.visitAnnotation(
				arg0, arg1);
	}

}
