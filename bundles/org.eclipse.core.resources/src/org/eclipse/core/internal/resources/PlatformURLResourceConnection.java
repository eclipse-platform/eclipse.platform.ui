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

import java.net.*;
import java.io.*;
import java.util.*;
import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Platform URL support
 * platform:/resource/<path>/<resource>  maps to resource in current workspace
 */
public class PlatformURLResourceConnection extends PlatformURLConnection {

	// resource/ protocol
	public static final String RESOURCE = "resource"; //$NON-NLS-1$
	public static final String RESOURCE_URL_STRING = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + RESOURCE + "/"; //$NON-NLS-1$ //$NON-NLS-2$
	private static URL rootURL;

public PlatformURLResourceConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return false; // don't cache, workspace is local
}
protected URL resolve() throws IOException {
	IPath spec = new Path(url.getFile().trim()).makeRelative();
	if (!spec.segment(0).equals(RESOURCE)) 
		throw new IOException(Policy.bind("url.badVariant", url.toString())); //$NON-NLS-1$
	int count = spec.segmentCount();
	// if there is only one segment then we are talking about the workspace root.
	if (count == 1) 
		return rootURL;
	// if there are two segments then the second is a project name.
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(spec.segment(1));
	if (!project.exists()) {
		String message = Policy.bind("url.couldNotResolve", project.getName(), url.toExternalForm()); //$NON-NLS-1$
		throw new IOException(message);
	}
	IPath result = null;
	if (count == 2)
		result = project.getLocation();
	else {
		spec = spec.removeFirstSegments(2);
		result = project.getFile(spec).getLocation();
	}	
	return new URL("file", "", result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
}

/**
 * This method is called during resource plugin startup() initialization.
 * @param url URL to the root of the current workspace.
 */
public static void startup(IPath root) {
	// register connection type for platform:/resource/ handling
	if (rootURL != null) 
		return;
	try {
		rootURL = new URL("file:" + root.toString()); //$NON-NLS-1$
	} catch (MalformedURLException e) {
		// should never happen but if it does, the resource URL cannot be supported.
		return;
	}
	PlatformURLHandler.register(RESOURCE, PlatformURLResourceConnection.class);
}
}