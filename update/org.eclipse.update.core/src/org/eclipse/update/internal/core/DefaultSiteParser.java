package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.update.core.*;
import org.xml.sax.*;
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

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Start parsing:"+site.getURL().toExternalForm());
		}
		
		try {
			ClassLoader l = new URLClassLoader(new URL[]{site.getURL()},null);
			bundle = ResourceBundle.getBundle("site",Locale.getDefault(),l);
		} catch (MissingResourceException e){
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage()+":"+site.getURL().toExternalForm());
			}
		}		
		parser.parse(new InputSource(this.siteStream));
	}
	
	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName,String qName, Attributes attributes)
		throws SAXException {

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Start Element: uri:"+uri+" local Name:"+localName+" qName:"+qName);
		}

		String tag = localName.trim();
	
		if (tag.equalsIgnoreCase(SITE)){
			processSite(attributes);
			return;
		}
	
		if (tag.equalsIgnoreCase(FEATURE)){
			processFeature(attributes);
			return;
		}
		
	
		if (tag.equalsIgnoreCase(ARCHIVE)){
			processArchive(attributes);
			return;
		}		

		if (tag.equalsIgnoreCase(CATEGORY_DEF)){
			processCategoryDef(attributes);
			return;
		}

	}
	
	
	/** 
	 * process the Site info
	 */
	private void processSite(Attributes attributes){
		//
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(site.getURL(),infoURL,DEFAULT_INFO_URL);
		site.setInfoURL(url);
		
		// process the Site....if the site has a different type
		// throw an exception so the new parser can be used...
		// TODO:
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End process Site tag: infoURL:"+infoURL);
		}
		
		
	}
	
	/** 
	 * process the Feature info
	 */
	private void processFeature(Attributes attributes) {
		// if the type doesn';t exist ask teh site for default type
		String id = attributes.getValue("id");
		String ver = attributes.getValue("version");
		VersionedIdentifier versionedId = new VersionedIdentifier(id, ver);

		// the type of the feature
		String type = attributes.getValue("type");
		IFeature feature = null;
		if (type == null || type.equals("")) {
			feature = new DefaultPackagedFeature(versionedId, site);
		} else {
			Assert.isTrue(false,"Not implemented Yet... do not use 'type' in the feature tag of site.xml");
			//FIXME: manages creation of feature...
		}

		// url
		String defaultString = "features/"+feature.getIdentifier().toString()+".jar";
		URL url = UpdateManagerUtils.getURL(site.getURL(),attributes.getValue("url"),defaultString);
		((AbstractFeature) feature).setURL(url);
		
		// add the feature
		site.addFeature(feature);
		
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End Processing Feature Tag: id:"+id+" ver:"+ver+" type:"+type+" url"+url.toExternalForm());
		}
		
	}
	
	
	/** 
	 * process the Archive info
	 */
	private void processArchive(Attributes attributes){
		String id = attributes.getValue("id");
		String urlString = attributes.getValue("url");
		URL url = UpdateManagerUtils.getURL(site.getURL(),urlString,null);
		site.addArchive(new Info(id,url));
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End processing Archive: id:"+id+" ver:"+urlString);
		}
		
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
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End processing CategoryDef: name:"+name+" label:"+label);
		}
		
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
	public void endElement (String uri, String localName, String qName)
		throws SAXException {
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End Element:"+uri+":"+localName+":"+qName);
		}
		
	}

}

