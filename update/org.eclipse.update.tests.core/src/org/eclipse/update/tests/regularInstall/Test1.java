package org.eclipse.update.tests.regularInstall;

import java.net.URL;
import junit.framework.TestCase;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;

public class Test1 extends TestCase {

	private static String SITE1  = "file://C:/temp/";
	private static String SITE2  = "http://9.26.150.182/UpdateManager2/";	
	private static String LOCAL1 = "file://C:/TMP/";

	/**
	 * Constructor for Test1
	 */
	public Test1(String arg0) {
		super(arg0);
	}
	

	private IFeature getFeature1(ISite site){
		VersionedIdentifier id = new VersionedIdentifier("org.eclipse.update.core.feature1","1.0.0");
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id,site);
		PluginEntry pluginEntry= new PluginEntry();
		VersionedIdentifier pluginEntryId = new VersionedIdentifier("org.eclipse.update.core.feature1.plugin1","1.1.1");
		pluginEntry.setIdentifier(pluginEntryId);
		pluginEntry.setContainer(remoteFeature);
		pluginEntry.setLabel("FIRST PLUGIN ENTRY");
		remoteFeature.addPluginEntry(pluginEntry);
		return remoteFeature;
	}	
	

	public void testExecute1() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SITE1));
		IFeature remoteFeature = getFeature1(remoteSite);
		ISite localSite = new FileSite(new URL(LOCAL1));
		localSite.install(remoteFeature,null);
	}


	private IFeature getFeature2(ISite site){
		VersionedIdentifier id = new VersionedIdentifier("org.eclipse.update.core.feature2","1.0.0");
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id,site);
		PluginEntry pluginEntry= new PluginEntry();
		VersionedIdentifier pluginEntryId = new VersionedIdentifier("org.eclipse.update.core.feature2.plugin2","2.2.2");
		pluginEntry.setIdentifier(pluginEntryId);
		pluginEntry.setContainer(remoteFeature);
		pluginEntry.setLabel("SECOND PLUGIN ENTRY");
		remoteFeature.addPluginEntry(pluginEntry);
		return remoteFeature;
	}	
	

	public void testExecute2() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SITE2));
		IFeature remoteFeature = getFeature2(remoteSite);
		ISite localSite = new FileSite(new URL(LOCAL1));
		localSite.install(remoteFeature,null);
	}
	public static void main(String[] args){
		junit.textui.TestRunner.run(Test1.class);
	}	

}

