package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
/**
 * 
 */
public class ResourceStatus extends Status {
	
	private IPath path;


	public ResourceStatus(
		int type,
		int code,
		IPath path,
		String message,
		Throwable exception) {
		super(type, CVSProviderPlugin.ID, code, message, exception);
		this.path = path;
	}
	public ResourceStatus(int code, String message) {
		this(getSeverity(code), code, null, message, null);
	}
	public ResourceStatus(int code, IPath path, String message) {
		this(getSeverity(code), code, path, message, null);
	}
	public ResourceStatus(
		int code,
		IPath path,
		String message,
		Throwable exception) {
		this(getSeverity(code), code, path, message, exception);
	}
	public IPath getPath() {
		return path;
	}
	protected static int getSeverity(int code) {
		return code;
	}
}