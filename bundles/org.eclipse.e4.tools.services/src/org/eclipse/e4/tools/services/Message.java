package org.eclipse.e4.tools.services;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
	public enum ReferenceType {
		NONE, SOFT, WEAK
	}

	ReferenceType referenceType() default ReferenceType.SOFT;
	String contributorURI() default "";
}
