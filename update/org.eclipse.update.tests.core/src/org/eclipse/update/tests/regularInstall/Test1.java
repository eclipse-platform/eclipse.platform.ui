package org.eclipse.update.tests.regularInstall;

import java.io.File;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;

public class Test1 extends TestCase {

	private static String SOURCE_FILE_SITE;
	private static String SOURCE_HTTP_SITE;	
	private static String TARGET_FILE_SITE;

	/**
	 * Constructor for Test1
	 */
	public Test1(String arg0) {
		super(arg0);
	}
	
	
	protected void setUp(){
		
		String home = System.getProperty("user.home");
				
		// get bundle variables
		ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.update.tests.regularInstall.Resources");
		SOURCE_FILE_SITE = "file:///"+home+(String)bundle.getObject("SOURCE_FILE_SITE");
		SOURCE_HTTP_SITE = "http://"+(String)bundle.getObject("SOURCE_HTTP_SITE");
		TARGET_FILE_SITE = "file:///"+home+(String)bundle.getObject("TARGET_FILE_SITE");
		
		//cleanup target
		File target= new File(home+(String)bundle.getObject("TARGET_FILE_SITE"));

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
		
		ISite remoteSite = new URLSite(new URL(SOURCE_FILE_SITE));
		IFeature remoteFeature = getFeature1(remoteSite);
		ISite localSite = new FileSite(new URL(TARGET_FILE_SITE));
		localSite.install(remoteFeature,null);
		
		// verify
		String site = localSite.getURL().getHost()+":"+localSite.getURL().getPath();
		//String file
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
		
		ISite remoteSite = new URLSite(new URL(SOURCE_HTTP_SITE));
		IFeature remoteFeature = getFeature2(remoteSite);
		ISite localSite = new FileSite(new URL(TARGET_FILE_SITE));
		localSite.install(remoteFeature,null);
	}
	public static void main(String[] args){
		junit.textui.TestRunner.run(Test1.class);
	}	

}

