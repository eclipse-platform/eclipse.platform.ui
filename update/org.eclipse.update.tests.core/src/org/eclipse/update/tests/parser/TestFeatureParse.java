package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.core.model.DefaultFeatureParser;
import org.eclipse.update.core.model.FeatureModel;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;
import org.xml.sax.SAXParseException;

public class TestFeatureParse extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestFeatureParse(String arg0) {
		super(arg0);
	}
	
	
	public void testParse() throws Exception {
	
		String xmlFile = "xmls/feature_1.0.0/";
			ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
			URL url = UpdateManagerUtils.getURL(remoteSite.getURL(),xmlFile,null);
			
			SiteFeatureReference ref = new SiteFeatureReference();
			ref.setSite(remoteSite);
			ref.setURL(url);
			IFeature feature = ref.getFeature();
			
			String prov = feature.getProvider();
			assertEquals("Object Technology International",prov);
			
	}
	
	public void testParseValid1() throws Exception {

		try {
			URL remoteURL =
				new URL(SOURCE_FILE_SITE + "parsertests/feature1.xml");
			DefaultFeatureParser parser =
				new DefaultFeatureParser();
			parser.init(new FeatureExecutableFactory());
			URL resolvedURL = URLEncoder.encode(remoteURL);
			FeatureModel remoteFeature = parser.parse(resolvedURL.openStream());
			remoteFeature.resolve(remoteURL, null);

		} catch (SAXParseException e) {
			fail("Exception should NOT be thrown");
		}
	}
	
	public void testParseValid1bis() throws Exception {

		try {
			URL remoteURL =
				new URL(SOURCE_FILE_SITE + "parsertests/feature1bis.xml");
			DefaultFeatureParser parser =
				new DefaultFeatureParser();
			parser.init(new FeatureExecutableFactory());
			URL resolvedURL = URLEncoder.encode(remoteURL);
			FeatureModel remoteFeature = parser.parse(resolvedURL.openStream());
			remoteFeature.resolve(remoteURL, null);

			
		} catch (SAXParseException e) {
			fail("Exception should NOT be thrown");
		}
	}	
	
	public void testParseValid2() throws Exception {

		try {
			URL remoteURL =
				new URL(SOURCE_FILE_SITE + "parsertests/feature2.xml");
			DefaultFeatureParser parser =
				new DefaultFeatureParser();
			parser.init(new FeatureExecutableFactory());
			URL resolvedURL = URLEncoder.encode(remoteURL);
			FeatureModel remoteFeature = parser.parse(resolvedURL.openStream());
			remoteFeature.resolve(remoteURL, null);


		} catch (SAXParseException e) {
			fail("Exception should not be thrown"+e.getMessage());			
		}
	}		

	public void testParseValid3() throws Exception {

			try {
				URL remoteURL =
					new URL(SOURCE_FILE_SITE + "parsertests/feature3.xml");
				DefaultFeatureParser parser =
					new DefaultFeatureParser();
				parser.init(new FeatureExecutableFactory());
				URL resolvedURL = URLEncoder.encode(remoteURL);
				FeatureModel remoteFeature = parser.parse(resolvedURL.openStream());
				remoteFeature.resolve(remoteURL, null);
				
				String copyrightString = remoteFeature.getCopyrightModel().getURL().getFile();
				boolean resolved = copyrightString.indexOf(SiteManager.getOS())!=-1;
				assertTrue("Copyright URL not resolved:"+copyrightString,resolved);
			} catch (SAXParseException e) {
				fail("Exception should not be thrown"+e.getMessage());			
			}
		}			
	
}

