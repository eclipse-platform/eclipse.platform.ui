
package org.eclipse.update.internal.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
/**
 * 
 */
public interface JarVerification {
	
	
	public static final int JAR_NOT_SIGNED = 0;
	public static final int JAR_CORRUPTED = 1;
	public static final int JAR_INTEGRITY_VERIFIED = 2;
	public static final int JAR_SOURCE_VERIFIED = 3;
	public static final int UNKNOWN_ERROR = 4;
	public static final int VERIFICATION_CANCELLED = 5;

	public static final int ASK_USER = -1;
	public static final int CANCEL_INSTALL = 0;
	public static final int ERROR_INSTALL = 1;
	public static final int OK_TO_INSTALL = 2;

}
