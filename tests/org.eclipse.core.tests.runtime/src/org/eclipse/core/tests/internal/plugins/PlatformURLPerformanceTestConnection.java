/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Eclipse URL support
 * platform:platform/	maps to platform installation location
 */

import java.net.*;
import java.io.*;
import java.util.*;
import junit.framework.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.tests.harness.*;

public class PlatformURLPerformanceTestConnection extends PlatformURLConnection {

	// platform/ protocol
	public static final String TEST = "test";
	private static URL installURL;
public PlatformURLPerformanceTestConnection() {
	super(null);
}
public PlatformURLPerformanceTestConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	
	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1);

	if (!spec.startsWith(TEST+"/")) throw new IOException("Unsupported protocol variation "+url.toString());

	return spec.length()==TEST.length()+1 ? installURL : new URL(installURL,spec.substring(TEST.length()+1)); 
}
public static void startup(URL url) {
	
	// register connection type for platform:/plugin handling
	installURL = url;
	PlatformURLHandler.register(TEST, PlatformURLPerformanceTestConnection.class);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PlatformURLPerformanceTest("platformURLCompareTestLocal"));
//	suite.addTest(new PlatformURLPerformanceTest("platformURLCompareTestServer"));
	return suite;
}
}
