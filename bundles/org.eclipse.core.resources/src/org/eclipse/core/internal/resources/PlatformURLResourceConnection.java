package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Platform URL support
 * platform:/resource/<path>/<resource>  maps to resource in current workspace
 */

import java.net.*;
import java.io.*;
import java.util.*;
import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class PlatformURLResourceConnection extends PlatformURLConnection {

	// resource/ protocol
	public static final String RESOURCE = "resource";
	public static final String RESOURCE_URL_STRING = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + RESOURCE + "/";
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
		throw new IOException(Policy.bind("url.badVariant", url.toString()));
	int count = spec.segmentCount();
	// if there is only one segment then we are talking about the workspace root.
	if (count == 1) 
		return rootURL;
	// if there are two segments then the second is a project name.
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(spec.segment(1));
	if (!project.exists()) {
		String message = Policy.bind("url.couldNotResolve", project.getName(), url.toExternalForm());
		throw new IOException(message);
	}
	IPath result = null;
	if (count == 2)
		result = project.getLocation();
	else {
		spec = spec.removeFirstSegments(2);
		result = project.getFile(spec).getLocation();
	}	
	return new URL("file", "", result.toString());
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
		rootURL = new URL("file:" + root.toString());
	} catch (MalformedURLException e) {
		// should never happen but if it does, the resource URL cannot be supported.
		return;
	}
	PlatformURLHandler.register(RESOURCE, PlatformURLResourceConnection.class);
}
}