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
package org.eclipse.core.internal.resources;

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
	public static final ResourceStatus OK_STATUS = new ResourceStatus(IResourceStatus.OK, Policy.bind("ok")); //$NON-NLS-1$
	
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
			return "OK"; //$NON-NLS-1$
		case IStatus.ERROR :
			return "ERROR"; //$NON-NLS-1$
		case IStatus.INFO :
			return "INFO"; //$NON-NLS-1$
		case IStatus.WARNING :
			return "WARNING"; //$NON-NLS-1$
		default:
			return String.valueOf(getSeverity());
	}
}
// for debug only
public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("[type: "); //$NON-NLS-1$
	sb.append(getTypeName());
	sb.append("], [path: "); //$NON-NLS-1$
	sb.append(getPath());
	sb.append("], [message: "); //$NON-NLS-1$
	sb.append(getMessage());
	sb.append("], [plugin: "); //$NON-NLS-1$
	sb.append(getPlugin());
	sb.append("], [exception: "); //$NON-NLS-1$
	sb.append(getException());
	sb.append("]\n"); //$NON-NLS-1$
	return sb.toString();
}
}
