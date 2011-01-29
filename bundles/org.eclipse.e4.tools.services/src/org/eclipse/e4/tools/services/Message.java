package org.eclipse.e4.tools.services;

public @interface Message {
	public enum ReferenceType {
		NONE, SOFT, WEAK
	}

	ReferenceType referenceType() default ReferenceType.SOFT;
}
