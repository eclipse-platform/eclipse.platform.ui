package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.Assert;
import org.eclipse.update.core.ICategory;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.UpdateManagerPlugin;
import org.eclipse.update.core.VersionedIdentifier;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;




public class DefaultSiteParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream siteStream;
	private AbstractSite site;
	private static final String SITE 			= "site";
	private static final String FEATURE			= "feature";	
	private static final String ARCHIVE 		= "archive";	
	private static final String CATEGORY_DEF 	= "category-def";
	private static final String CATEGORY		= "category";	
	
	private static final String DEFAULT_INFO_URL= "index.html";
	
	private ResourceBundle bundle;
				
	

	/**
	 * Constructor for DefaultSiteParser
	 */
	public DefaultSiteParser(InputStream siteStream,ISite site) throws IOException,SAXException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);
		
		this.siteStream = siteStream;
		Assert.isTrue(site instanceof AbstractSite);
		this.site = (AbstractSite)site;
		
		try {
			ClassLoader l = new URLClassLoader(new URL[]{site.getURL()},null);
			bundle = ResourceBundle.getBundle("site",Locale.getDefault(),l);
		} catch (MissingResourceException e){
			//ok, there is no bundle, keep it as null
		}		
		parser.parse(new InputSource(this.siteStream));
	}
	
	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName,String qName, Attributes attributes)
		throws SAXException {

		String tag = localName.trim();
	
		if (tag.equalsIgnoreCase(SITE)){
			processSite(attributes);
		}
	
		if (tag.equalsIgnoreCase(FEATURE)){
			processFeature(attributes);
		}
		
	
		if (tag.equalsIgnoreCase(ARCHIVE)){
			processArchive(attributes);
		}		

		if (tag.equalsIgnoreCase(CATEGORY_DEF)){
			processCategoryDef(attributes);
		}

	}
	
	
	/** 
	 * process the Site info
	 */
	private void processSite(Attributes attributes){
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(site.getURL(),infoURL,DEFAULT_INFO_URL);
		site.setInfoURL(url);
	}
	
	/** 
	 * process the Feature info
	 */
	private void processFeature(Attributes attributes) {
		// if the type doesn';t exist ask teh site for default type
		String id = attributes.getValue("id");
		String ver = attributes.getValue("version");
		VersionedIdentifier versionedId = new VersionedIdentifier(id, ver);

		String type = attributes.getValue("type");
		IFeature feature = null;
		if (type == null || type.equals("")) {
			feature = new DefaultPackagedFeature(versionedId, site);
		} else {
			//FIXME: manages creation of feature...
		}

		// url
		String defaultString = "features/"+feature.getIdentifier().toString()+".jar";
		URL url = UpdateManagerUtils.getURL(site.getInfoURL(),attributes.getValue("url"),defaultString);
		((AbstractFeature) feature).setURL(url);
		
		// add the feature
		site.addFeature(feature);
	}
	
	
	/** 
	 * process the Archive info
	 */
	private void processArchive(Attributes attributes){
	}	
	
	/** 
	 * process the Category Def info
	 */
	private void processCategoryDef(Attributes attributes){
		String name  = attributes.getValue("name");
		String label = attributes.getValue("label");
		label = UpdateManagerUtils.getResourceString(label,bundle);
		ICategory category = new DefaultCategory(name,label);
		site.addCategory(category);		
	}

	/**
	 * @see DefaultHandler#error(SAXParseException)
	 */
	public void error(SAXParseException arg0) throws SAXException {
		super.error(arg0);
	}

	/**
	 * @see DefaultHandler#endElement(String, String, String)
	 */
	public void endElement(String arg0, String arg1, String arg2)
		throws SAXException {
		super.endElement(arg0, arg1, arg2);
	}

}

