/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple;

import java.io.*;
import java.net.URL;
import junit.framework.TestCase;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.spi.RegistryStrategy;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;

public class BaseExtensionRegistryRun extends TestCase {

	// The imaging device registry
	protected IExtensionRegistry simpleRegistry;
	protected Object masterToken = new Object();
	protected Object userToken = new Object();
	
	// Path to the XML files
	private final static String xmlPath = "Plugin_Testing/registry/testSimple/"; //$NON-NLS-1$

	public BaseExtensionRegistryRun() {
		super();
	}

	public BaseExtensionRegistryRun(String name) {
		super(name);
	}
	
	protected URL getXML(String fileName) {
		return RuntimeTestsPlugin.getContext().getBundle().getEntry(xmlPath + fileName);
	}

	/**
	 * Create the "imaging device" registry
	 */
	protected void setUp() throws Exception {
		// create the imaging device registry
		simpleRegistry = startRegistry();
	}

	/**
	 * Properly dispose of the extension registry
	 */
	protected void tearDown() throws Exception {
		stopRegistry();
	}

	/**
	 * @return - open extension registry
	 */
	protected IExtensionRegistry startRegistry() {
		return startRegistry(this.getClass().getName());
	}
	
	/**
	 * @return - open extension registry
	 */
	protected IExtensionRegistry startRegistry(String subDir) {
		// use plugin's metadata directory to save cache data
		Bundle theBundle = RuntimeTestsPlugin.getContext().getBundle();
		IPath userDataPath = Platform.getStateLocation(theBundle);
		userDataPath = userDataPath.append(subDir);

		File[] registryLocations = new File[] {new File(userDataPath.toOSString())};
		boolean[] readOnly = new boolean[] {false};
		RegistryStrategy registryStrategy = new RegistryStrategy(registryLocations, readOnly);
		return RegistryFactory.createRegistry(registryStrategy, masterToken, userToken);
	}
	
	/**
	 * Stops the extension registry.
	 */
	protected void stopRegistry() {
		assertNotNull(simpleRegistry);
		simpleRegistry.stop(masterToken);
	}

	protected void processXMLContribution(IContributor nonBundleContributor, URL url) {
		processXMLContribution(nonBundleContributor, url, false);
	}
	
	protected void processXMLContribution(IContributor nonBundleContributor, URL url, boolean persist) {
		try {
			InputStream is = url.openStream();
			simpleRegistry.addContribution(is, nonBundleContributor, persist, url.getFile(), null, persist ? masterToken : userToken);
		} catch (IOException eFile) {
			fail(eFile.getMessage());
			return;
		}
	}
	
	protected String qualifiedName(String namespace, String simpleName) {
		return namespace + "." + simpleName; //$NON-NLS-1$
	}
	
}
