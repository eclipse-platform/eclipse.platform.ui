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
package org.eclipse.update.tests.regularRemove;
import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.configuration.IConfiguredSiteChangedListener;
import org.eclipse.update.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestRemove extends UpdateManagerTestCase {

	public class Listener implements IConfiguredSiteChangedListener {

		public boolean notified = false;

		public void featureInstalled(IFeature feature) {
			notified = true;
			System.out.println("Notified DefaultFeature Installed");
		}

		public void featureRemoved(IFeature feature) {
		}
		public void featureConfigured(IFeature feature) {
		};
		public void featureUnconfigured(IFeature feature) {
		};

		public boolean isNotified() {
			return notified;
		}
	}

	/**
	 * Constructor for Test1
	 */
	public TestRemove(String arg0) {
		super(arg0);
	}

	private IFeature getFeature1(ISite site)
		throws MalformedURLException, CoreException {
		//URL url = UpdateManagerUtils.getURL(site.getURL(), "features/org.eclipse.update.core.tests.feature1_1.0.4.jar", null);
		SiteFeatureReference ref = new SiteFeatureReference();
		ref.setSite(site);
		ref.setURLString("features/org.eclipse.update.core.tests.feature1_1.0.4.jar");
		ref.setType(ISite.DEFAULT_PACKAGED_FEATURE_TYPE);
		ref.resolve(site.getURL(), null);
		return ref.getFeature(null);
	}

	public void testRemoveFeature() throws Exception {

		// install feature
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE,null);
		IFeature remoteFeature = getFeature1(remoteSite);
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
		IFeatureReference ref = localSite.install(remoteFeature, null, null);

		// then remove it
		assertNotNull("Feature is null",ref.getFeature(null));
		String featureRef = ref.getFeature(null).getVersionedIdentifier().toString();
		localSite.remove(ref.getFeature(null), null);

		// verify
		String site = TARGET_FILE_SITE.getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));
		String pluginName = entries[0].getVersionedIdentifier().toString();

		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin files installed locally", !pluginFile.exists());

		File featureFile =
			new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + featureRef);
		assertTrue(
			"feature info installed locally:" + featureFile,
			!featureFile.exists());

	}

}
