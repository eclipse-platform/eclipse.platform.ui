package org.eclipse.update.tests.regularInstall;

import java.io.File;
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
		VersionedIdentifier id = new VersionedIdentifier("org.eclipse.update.core.tests.feature1","1.0.0");
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id,site);
		VersionedIdentifier pluginEntryId = new VersionedIdentifier("org.eclipse.update.core.feature1.plugin1","1.1.1");
		PluginEntry pluginEntry= new PluginEntry(pluginEntryId);		
		pluginEntry.setContainer(remoteFeature);
		pluginEntry.setLabel("FIRST PLUGIN ENTRY");
		remoteFeature.addPluginEntry(pluginEntry);
		return remoteFeature;
	}	
	

	public void testFileSite() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SOURCE_FILE_SITE));
		IFeature remoteFeature = getFeature1(remoteSite);
		ISite localSite = new FileSite(new URL(TARGET_FILE_SITE));
		localSite.install(remoteFeature,null);
		
		// verify
		String site = localSite.getURL().getFile();
		String pluginName= remoteFeature.getPluginEntries()[0].getIdentifier().toString();
		File pluginFile = new File(site,"plugins/"+pluginName);
		assertTrue(pluginFile.exists());

		//cleanup
		removeFromFileSystem(pluginFile);

	}


	private IFeature getFeature2(ISite site){
		VersionedIdentifier id = new VersionedIdentifier("org.eclipse.update.core.tests.feature2","1.0.0");
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id,site);
		VersionedIdentifier pluginEntryId = new VersionedIdentifier("org.eclipse.update.core.feature2.plugin2","2.2.2");
		PluginEntry pluginEntry= new PluginEntry(pluginEntryId);		
		pluginEntry.setContainer(remoteFeature);
		pluginEntry.setLabel("SECOND PLUGIN ENTRY");
		remoteFeature.addPluginEntry(pluginEntry);
		return remoteFeature;
	}	
	

	public void testHTTPSite() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SOURCE_HTTP_SITE));
		IFeature remoteFeature = getFeature2(remoteSite);
		ISite localSite = new FileSite(new URL(TARGET_FILE_SITE));
		localSite.install(remoteFeature,null);

		String site = localSite.getURL().getFile();
		String pluginName= remoteFeature.getPluginEntries()[0].getIdentifier().toString();
		File pluginFile = new File(site,"plugins/"+pluginName);
		assertTrue(pluginFile.exists());

		//cleanup
		removeFromFileSystem(pluginFile);
	}
}

