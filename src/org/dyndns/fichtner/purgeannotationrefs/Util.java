package org.dyndns.fichtner.purgeannotationrefs;

import org.objectweb.asm.Type;

/**
 * Class holding static utility methods.
 * 
 * @author Peter Fichtner
 */
public final class Util {

	/**
	 * Translate the passed Asm type into java classname.
	 * 
	 * @param type the type to tranlsate
	 * @return java classname
	 */
	public static String translate(final String type) {
		return Type.getType(type).getInternalName().replace('/', '.');
	}

	/**
	 * Returns <code>true</code> if the passed name is a method.
	 * 
	 * @param name name to check
	 * @return <code>true</code> if the passed name is a method
	 */
	public static boolean isMethod(final String name) {
		return !"<init>".equals(name) && !"<clinit>".equals(name);
	}

	/**
	 * Returns <code>true</code> if the passed name is a constructor.
	 * 
	 * @param name name to check
	 * @return <code>true</code> if the passed name is a constructor
	 */
	public static boolean isConstructor(final String name) {
		return "<init>".equals(name);
	}

	private Util() {
		throw new IllegalStateException();
	}

}
