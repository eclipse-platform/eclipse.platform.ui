/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core;
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
	
public class CVSStatus extends Status {

	/*** Status codes ***/
	public static final int SERVER_ERROR = -10; // XXX What should this number be?
	public static final int NO_SUCH_TAG = -11;
	public static final int CONFLICT = -12;
	public static final int ERROR_LINE = -14; // generic uninterpreted E line from the server
	public static final int TAG_ALREADY_EXISTS = -15;
	public static final int COMMITTING_SYNC_INFO_FAILED = -16;
	public static final int DOES_NOT_EXIST = -17;
	public static final int FOLDER_NEEDED_FOR_FILE_DELETIONS = -18;
	public static final int CASE_VARIANT_EXISTS = -19;
	public static final int UNSUPPORTED_SERVER_VERSION = -20;
	public static final int SERVER_IS_CVSNT = -21;
	
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
