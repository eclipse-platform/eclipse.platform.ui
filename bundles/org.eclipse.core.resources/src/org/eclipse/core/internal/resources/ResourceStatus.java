package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
/**
 * 
 */
public class ResourceStatus extends Status implements IResourceStatus {
	IPath path;
	
	/** Singleton status indicating success */
	public static final ResourceStatus OK_STATUS = new ResourceStatus(IResourceStatus.OK, Policy.bind("ok"));
	
public ResourceStatus(int type, int code, IPath path, String message, Throwable exception) {
	super(type, ResourcesPlugin.PI_RESOURCES, code, message, exception);
	this.path = path;
}
public ResourceStatus(int code, String message) {
	this(getSeverity(code), code, null, message, null);
}
public ResourceStatus(int code, IPath path, String message) {
	this(getSeverity(code), code, path, message, null);
}
public ResourceStatus(int code, IPath path, String message, Throwable exception) {
	this(getSeverity(code), code, path, message, exception);
}
/**
 * @see IResourceStatus#getPath
 */
public IPath getPath() {
	return path;
}
protected static int getSeverity(int code) {
	return code == 0 ? 0 : 1 << (code % 100 / 33);
}
// for debug only
private String getTypeName() {
	switch (getSeverity()) {
		case IStatus.OK :
			return "OK";
		case IStatus.ERROR :
			return "ERROR";
		case IStatus.INFO :
			return "INFO";
		case IStatus.WARNING :
			return "WARNING";
		default:
			return String.valueOf(getSeverity());
	}
}
// for debug only
public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("[type: ");
	sb.append(getTypeName());
	sb.append("], [path: ");
	sb.append(getPath());
	sb.append("], [message: ");
	sb.append(getMessage());
	sb.append("], [plugin: ");
	sb.append(getPlugin());
	sb.append("], [exception: ");
	sb.append(getException());
	sb.append("]\n");
	return sb.toString();
}
}
