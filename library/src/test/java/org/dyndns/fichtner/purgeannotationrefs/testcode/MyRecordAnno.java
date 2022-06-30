package org.dyndns.fichtner.purgeannotationrefs.testcode;

import org.dyndns.fichtner.purgeannotationrefs.RemoveFrom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.RECORD_COMPONENT)
public @interface MyRecordAnno {

  RemoveFrom value() default RemoveFrom.RECORD_COMPONENTS;
}
