package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

import java.net.URL;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.IWritable;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.internal.core.Writer;
import org.eclipse.update.tests.UpdateManagerTestCase;
import sun.awt.UpdateClient;

public class TestSiteGeneration extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestSiteGeneration(String arg0) {
		super(arg0);
	}
	
	
	public void testGenerate() throws Exception {
		// get a Site.xml
		// generate in another place
		// create a site on it
		// check with the first one
		
		// DO NOT INCLUDE IN TESTS YET
		return;
		
		// get site.xml
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		
		// generate
		ISite tempSite = SiteManager.getTempSite();
		new File(tempSite.getURL().getFile()).mkdirs();
		File file = new File(tempSite.getURL().getFile()+"site.xml");
		PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
		Writer writer = new Writer();
		writer.writeSite((IWritable)remoteSite,fileWriter); 
		fileWriter.close();
		
		//get the local Site again
		URL newURL = new URL(tempSite.getURL().getProtocol(),tempSite.getURL().getHost(),tempSite.getURL().getFile());
		ISite compareSite = SiteManager.getSite(newURL);
		
		// compare
		String remoteURLAsString = UpdateManagerUtils.getURLAsString(remoteSite.getURL(),remoteSite.getInfoURL());
		String compareURLAsString = UpdateManagerUtils.getURLAsString(compareSite.getURL(),compareSite.getInfoURL());
		assertEquals(remoteURLAsString, compareURLAsString);

		remoteURLAsString = UpdateManagerUtils.getURLAsString(remoteSite.getURL(),remoteSite.getFeatureReferences()[0].getURL());
		compareURLAsString = UpdateManagerUtils.getURLAsString(compareSite.getURL(),compareSite.getFeatureReferences()[0].getURL());
		assertEquals(remoteURLAsString,compareURLAsString);
		
		assertEquals(remoteSite.getCategories()[0].getLabel(),compareSite.getCategories()[0].getLabel());
		
		// cleanup
		UpdateManagerUtils.removeFromFileSystem(file);	
	
	}
	
	
}

