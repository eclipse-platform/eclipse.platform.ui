/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.api;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestPluginContainerAPI extends UpdateManagerTestCase {

	private Site site;
	private DefaultFeature feature;

	/**
	 * the Site to test
	 */
	private Site getSite() throws CoreException, MalformedURLException {
		if (site == null) {

			site = (Site)SiteManager.getSite(new URL(SOURCE_FILE_SITE,"testAPI/"), null);

		}
		return site;
	}

	/**
	 * the feature to test
	 */
	private DefaultFeature getFeature() throws MalformedURLException, CoreException {
		if (feature == null) {
			ISite site = getSite();
			URL id = UpdateManagerUtils.getURL(site.getURL(),"org.eclipse.update.core.feature1_1.0.0.jar",null);						
			feature = new DefaultFeature(site);
			feature.setURL(id);	
		}
		return feature;
	}

	/**
	 * Test the getFeatures()
	 */
	public TestPluginContainerAPI(String arg0) throws CoreException {
		super(arg0);
	}

	public void testAbstractFeature() throws CoreException, MalformedURLException {
		PluginEntry pluginEntry = new PluginEntry();
		pluginEntry.setVersionedIdentifier(new VersionedIdentifier("id", "6"));
		Feature _feature = getFeature();
		((DefaultFeature)_feature).addPluginEntry(pluginEntry);
		assertEquals(_feature.getPluginEntryCount(), 1);
		assertEquals(_feature.getPluginEntries()[0], pluginEntry);

	}

	public void testAbstactSite() throws CoreException, MalformedURLException {
		PluginEntry pluginEntry = new PluginEntry();
		pluginEntry.setVersionedIdentifier(new VersionedIdentifier("id", "6"));
		Site _site = getSite();
		_site.addPluginEntry(pluginEntry);
		assertEquals(_site.getPluginEntryCount(), 1);
		assertEquals(_site.getPluginEntries()[0], pluginEntry);

	}

}
