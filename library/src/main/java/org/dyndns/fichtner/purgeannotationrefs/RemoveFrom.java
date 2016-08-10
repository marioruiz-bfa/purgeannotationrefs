package org.dyndns.fichtner.purgeannotationrefs;

import java.lang.annotation.ElementType;

/**
 * Specify the elements the annotations should be removed from. At the moment
 * supported types are:
 * <ol>
 * <li>{@link RemoveFrom#TYPES}</li>
 * <li>{@link RemoveFrom#FIELDS}</li>
 * <li>{@link RemoveFrom#CONSTRUCTORS}</li>
 * <li>{@link RemoveFrom#METHODS}</li>
 * <li>{@link RemoveFrom#PARAMETERS}</li>
 * </ol>
 * Local variables are not supported since they are not retained in java
 * bytecode (class files), see <a href=
 * "https://docs.oracle.com/javase/specs/jls/se6/html/interfaces.html#9.6.1.2"
 * >JLS 9.6.1.2</a>
 * 
 * @author Peter Fichtner
 */
public enum RemoveFrom {

	/**
	 * @see ElementType#TYPE
	 */
	TYPES, /**
	 * @see ElementType#FIELD
	 */
	FIELDS, /**
	 * @see ElementType#CONSTRUCTOR
	 */
	CONSTRUCTORS, /**
	 * @see ElementType#METHOD
	 */
	METHODS, /**
	 * @see ElementType#PARAMETER
	 */
	PARAMETERS;

}
