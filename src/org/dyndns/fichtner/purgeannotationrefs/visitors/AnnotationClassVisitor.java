package org.dyndns.fichtner.purgeannotationrefs.visitors;

import static org.dyndns.fichtner.purgeannotationrefs.Util.atLeastOneMatches;

import java.util.ArrayList;
import java.util.List;

import org.dyndns.fichtner.purgeannotationrefs.Matcher;
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

	private final List<Matcher> filtered = new ArrayList<Matcher>();

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
	 * @param matcher the annotation to filter
	 */
	public void addFiltered(final Matcher matcher) {
		this.filtered.add(matcher);
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		return atLeastOneMatches(this.filtered, Util.translate(desc)) ? null
				: super.visitAnnotation(desc, visible);
	}

}
