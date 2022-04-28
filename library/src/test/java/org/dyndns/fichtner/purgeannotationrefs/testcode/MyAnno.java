package org.dyndns.fichtner.purgeannotationrefs.testcode;

import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.dyndns.fichtner.purgeannotationrefs.RemoveFrom.METHODS;

@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
@Retention(RUNTIME)
public @interface MyAnno {

  @MyAnno(METHODS)
  RemoveFrom value();

}
