package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
	
/**
 * @version 	1.0
 * @author
 */
public class CVSStatus extends Status {

	/*** Status codes ***/
	public static final int SERVER_ERROR = -10; // XXX What should this number be?
	public static final int NO_SUCH_TAG = -11;
	public static final int CONFLICT = -12;
	public static final int ERROR_LINE = -14; // generic uninterpreted E line from the server
	public static final int TAG_ALREADY_EXISTS = -15;
	public static final int DELETION_FAILED = -16;
	public static final int DOES_NOT_EXISTS = -17;
	
	// Path for resource related status
	private IPath path;
	
	public CVSStatus(int severity, int code, String message, Throwable t) {
		super(severity, CVSProviderPlugin.ID, code, message, t);
	}
	
	public CVSStatus(int severity, int code, String message) {
		this(severity, code, message, null);
	}
	
	public CVSStatus(int severity, IPath path, String message, Throwable t) {
		this(severity, message);
		this.path = path;
	}
	
	public CVSStatus(int severity, String message) {
		this(severity, severity, message, null);
	}
	
	public IPath getPath() {
		return path;
	}
}
