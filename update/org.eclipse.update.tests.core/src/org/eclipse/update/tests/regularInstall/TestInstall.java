package org.eclipse.update.tests.regularInstall;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestInstall extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestInstall(String arg0) {
		super(arg0);
	}
	
	
	private IFeature getFeature1(ISite site){
		VersionedIdentifier id = new VersionedIdentifier("org.eclipse.update.core.tests.feature1","1.0.4");
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id,site);
		//VersionedIdentifier pluginEntryId = new VersionedIdentifier("org.eclipse.update.core.feature1.plugin1","1.1.1");
		//PluginEntry pluginEntry= new PluginEntry(pluginEntryId);		
		//pluginEntry.setContainer(remoteFeature);
		//pluginEntry.setLabel("FIRST PLUGIN ENTRY");
		//remoteFeature.addPluginEntry(pluginEntry);
		
		// url
		
		String defaultString = "features/"+id.toString()+".jar";
		URL url = UpdateManagerUtils.getURL(site.getURL(),null,defaultString);
		((AbstractFeature) remoteFeature).setURL(url);
		
		
		
		return remoteFeature;
	}	
	

	public void testFileSite() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SOURCE_FILE_SITE));
		IFeature remoteFeature = getFeature1(remoteSite);
		ISite localSite = new FileSite(new URL(TARGET_FILE_SITE));
		localSite.install(remoteFeature,null);
		
		// verify
		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry",(entries!=null && entries.length!=0));
		String pluginName= entries[0].getIdentifier().toString();
		File pluginFile = new File(site,AbstractSite.DEFAULT_PLUGIN_PATH+pluginName);
		assertTrue("plugin files not installed locally",pluginFile.exists());

		File featureFile = new File(site,FileSite.INSTALL_FEATURE_PATH+remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally",featureFile.exists());
		//cleanup
		removeFromFileSystem(pluginFile);

	}


	private IFeature getFeature2(ISite site){
		VersionedIdentifier id = new VersionedIdentifier("org.eclipse.update.core.tests.feature1","1.0.4");
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id,site);
		//VersionedIdentifier pluginEntryId = new VersionedIdentifier("org.eclipse.update.core.feature2.plugin2","2.2.2");
		//PluginEntry pluginEntry= new PluginEntry(pluginEntryId);		
		//pluginEntry.setContainer(remoteFeature);
		//pluginEntry.setLabel("SECOND PLUGIN ENTRY");
		//remoteFeature.addPluginEntry(pluginEntry);
		
		// url
		String defaultString = "features/features2.jar";
		URL url = UpdateManagerUtils.getURL(site.getURL(),null,defaultString);
		((AbstractFeature) remoteFeature).setURL(url);
		
		return remoteFeature;
	}	
	

	public void testHTTPSite() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SOURCE_HTTP_SITE));
		IFeature remoteFeature = getFeature2(remoteSite);
		ISite localSite = new FileSite(new URL(TARGET_FILE_SITE));
		localSite.install(remoteFeature,null);

		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry",(entries!=null && entries.length!=0));
		String pluginName= entries[0].getIdentifier().toString();
		File pluginFile = new File(site,AbstractSite.DEFAULT_PLUGIN_PATH+pluginName);
		assertTrue("feature info not installed locally",pluginFile.exists());

		File featureFile = new File(site,FileSite.INSTALL_FEATURE_PATH+remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally",featureFile.exists());

		//cleanup
		removeFromFileSystem(pluginFile);
	}
}

