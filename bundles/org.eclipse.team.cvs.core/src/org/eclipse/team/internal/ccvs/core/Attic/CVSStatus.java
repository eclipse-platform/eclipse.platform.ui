package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;

public class CVSStatus extends Status{
	
	IPath path;
	
	public CVSStatus(
		int type,
		int code,
		IPath path,
		String message,
		Throwable exception) {
		super(type, CVSProviderPlugin.ID, code, message, exception);
		this.path = path;
	}
	
	public CVSStatus(int code, String message) {
		this(code, code, null, message, null);
	}
	
	public CVSStatus(int code, IPath path, String message) {
		this(code, code, path, message, null);
	}
	
	public CVSStatus(int code, IPath path, String message, Throwable exception) {
		this(code, code, path, message, exception);
	}
	
	/**
	 * @see IResourceStatus#getPath
	 */
	public IPath getPath() {
		return path;
	}
	
	protected static int getSeverity(int code) {
		return code;
	}
}