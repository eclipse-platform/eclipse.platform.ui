package org.eclipse.e4.core.services.nls;

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
