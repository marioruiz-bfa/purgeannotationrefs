package org.dyndns.fichtner.purgeannotationrefs.testcode;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.METHODS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;

@Target({ TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE })
@Retention(RUNTIME)
public @interface MyAnno {

	@MyAnno(METHODS)
	RemoveFrom value();

}
