package org.eclipse.update.tests.api;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.core.DefaultPackagedFeature;
import org.eclipse.update.internal.core.FileSite;
import org.eclipse.update.internal.core.PluginEntry;
import org.eclipse.update.internal.core.URLSite;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestPluginContainerAPI extends UpdateManagerTestCase {

	private AbstractSite site;
	private AbstractFeature feature;

	/**
	 * the Site to test
	 */
	private AbstractSite getSite() {
		if (site == null) {
			try {
				site = new FileSite(new URL(SOURCE_FILE_SITE));
			} catch (java.net.MalformedURLException e) {
				fail("Wrong source site URL");
			}
		}
		return site;
	}

	/**
	 * the feature to test
	 */
	private AbstractFeature getFeature() {
		if (feature == null) {
			ISite site = getSite();
			VersionedIdentifier id =
				new VersionedIdentifier("org.eclipse.update.core.feature1", "1.0.0");
			feature = new DefaultPackagedFeature(id, site);
		}
		return feature;
	}

	/**
	 * Test the getFeatures()
	 */
	public TestPluginContainerAPI(String arg0) {
		super(arg0);
	}

	public void testAbstractFeature() {
		PluginEntry pluginEntry = new PluginEntry("id","ver");
		AbstractFeature _feature = getFeature();
		_feature.addPluginEntry(pluginEntry);
		assertEquals(_feature.getPluginEntryCount(), 1);
		assertEquals(_feature.getPluginEntries()[0], pluginEntry);

	}

	public void testAbstactSite() {
		PluginEntry pluginEntry = new PluginEntry("id","ver");
		AbstractSite _site = getSite();
		_site.addPluginEntry(pluginEntry);
		assertEquals(_site.getPluginEntryCount(), 1);
		assertEquals(_site.getPluginEntries()[0], pluginEntry);

	}

}

