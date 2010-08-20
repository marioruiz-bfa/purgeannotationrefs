package org.dyndns.fichtner.purgeannotationrefs;

import java.util.Collection;

import org.objectweb.asm.Type;

/**
 * Class holding static utility methods.
 * 
 * @author Peter Fichtner
 */
public final class Util {

	private Util() {
		throw new IllegalStateException();
	}

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

	/**
	 * Returns <code>true</code> if the passed String donates a class.
	 * 
	 * @param arg the file to check
	 * @return <code>true</code> if the passed String donates a class
	 */
	@SuppressWarnings("nls")
	public static boolean isClass(final String arg) {
		return arg.toLowerCase().endsWith(".class");
	}

	/**
	 * Returns <code>true</code> if the passed String donates a zipfile.
	 * 
	 * @param arg the file to check
	 * @return <code>true</code> if the passed String donates a zipfile
	 */
	@SuppressWarnings("nls")
	public static boolean isZip(final String arg) {
		final String file = arg.toLowerCase();
		return file.endsWith(".jar") || file.endsWith(".ear")
				|| file.endsWith(".zip") || file.endsWith(".war");
	}

	/**
	 * Checks if one of the matches matches the passed String.
	 * 
	 * @param matchers List of matches
	 * @param string the String to check
	 * @return <code>true</code> if one of the matcher matches
	 */
	public static boolean matches(final Collection<Matcher> matchers,
			final String string) {
		for (final Matcher matcher : matchers) {
			if (matcher.matches(string)) {
				return true;
			}
		}
		return false;
	}

}
