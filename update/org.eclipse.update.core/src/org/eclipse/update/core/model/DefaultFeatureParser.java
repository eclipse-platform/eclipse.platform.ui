package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse default feature.xml
 */

public class DefaultFeatureParser extends DefaultHandler {

	private SAXParser parser;
	private FeatureModelFactory factory;
	private FeatureModel feature;		
	private String text;
	
	public static boolean DEBUG = false;
	
	private static final String FEATURE = "feature";
	private static final String HANDLER = "install-handler";
	private static final String DESCRIPTION = "description";
	private static final String COPYRIGHT = "copyright";	
	private static final String LICENSE = "license";
	private static final String URL = "url";
	private static final String UPDATE = "update";
	private static final String DISCOVERY = "discovery";
	private static final String REQUIRES = "requires";
	private static final String IMPORT = "import";	
	private static final String PLUGIN = "plugin";	
	private static final String DATA = "data";	
	private static final String GROUP = "group";	

	/**
	 * Constructor for DefaultFeatureParser
	 */
	public DefaultFeatureParser(FeatureModelFactory factory) {
		super();
		this.parser = new SAXParser();
		this.parser.setContentHandler(this);
		this.factory = factory;
	
		if (DEBUG)
			debug("Created");
	}
	
	/**
	 * @since 2.0
	 */
	public FeatureModel parse(InputStream in) throws SAXException, IOException {
		parser.parse(new InputSource(in));
		return feature;
	}
	
	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName,String qName, Attributes attributes) {
	
		if (DEBUG)
			debug("Start Element: uri:"+uri+" local Name:"+localName+" qName:"+qName);
		
		String tag = localName.trim();
	
		if (tag.equalsIgnoreCase(FEATURE)){
			processFeature(attributes);
			return;
		}
	
		if (tag.equalsIgnoreCase(DESCRIPTION)){
			if (feature != null)
				feature.setDescriptionModel(processInfo(attributes));
			return;
		}
		
		if (tag.equalsIgnoreCase(COPYRIGHT)){
			if (feature != null)
				feature.setCopyrightModel(processInfo(attributes));
			return;
		}
		
		if (tag.equalsIgnoreCase(LICENSE)){
			if (feature != null)
				feature.setLicenseModel(processInfo(attributes));
			return;
		}
	
		if (tag.equalsIgnoreCase(UPDATE)){
			if (feature != null)
				feature.setUpdateSiteEntryModel(processURLInfo(attributes));
			return;
		}		
		
		if (tag.equalsIgnoreCase(DISCOVERY)){
			if (feature != null)
				feature.addDiscoverySiteEntryModel(processURLInfo(attributes));
			return;
		}		
		
		if (tag.equalsIgnoreCase(IMPORT)){
			processImport(attributes);
			return;
		}
		
		if (tag.equalsIgnoreCase(PLUGIN)){
			processPlugin(attributes);
		}

		if (tag.equalsIgnoreCase(DATA)){
			processData(attributes);
		}
	}	
	
	/** 
	 * process feature info
	 */
	private void processFeature(Attributes attributes) {
		
		// create feature model
		feature = factory.createFeatureModel();
		
		// identifier and version
		String id = attributes.getValue("id");
		String ver= attributes.getValue("version");
		feature.setFeatureIdentifier(id);
		feature.setFeatureVersion(ver);
		
		// label
		String label = attributes.getValue("label");
		feature.setLabel(label);
		
		// provider
		String provider = attributes.getValue("provider-name");
		feature.setProvider(provider);
		
		//image
		String imageURL = attributes.getValue("image");
		feature.setImageURLString(imageURL); 
		
		// OS
		String os = attributes.getValue("os");
		feature.setOS(os);
		
		// WS
		String ws = attributes.getValue("ws");
		feature.setWS(ws);
		
		// NL
		String nl = attributes.getValue("nl");
		feature.setNL(nl);
		
		// application
		String application = attributes.getValue("application"); 
		feature.setApplication(application);		
		
		if (DEBUG){
			debug("End process DefaultFeature tag: id:"+id+" ver:"+ver+" label:"+label+" provider:"+provider);
			debug("End process DefaultFeature tag: image:"+imageURL);
			debug("End process DefaultFeature tag: ws:"+ws+" os:"+os+" nl:"+nl+" application:"+application); 
		}
	}
	
	/** 
	 * process URL info with element text
	 */
	private URLEntryModel processInfo(Attributes attributes) {
		URLEntryModel inf = factory.createURLEntryModel();
		String infoURL = attributes.getValue("url");
		inf.setURLString(infoURL);
		
		if (DEBUG)
			debug("Processed Info: url:"+infoURL);
		
		return inf;
	}
	
	/** 
	 * process URL info with label attribute
	 */
	private URLEntryModel processURLInfo(Attributes attributes) {
		URLEntryModel inf = factory.createURLEntryModel();
		String infoURL = attributes.getValue("url");
		String label = attributes.getValue("label");
		inf.setURLString(infoURL);
		inf.setAnnotation(label);
				
		if (DEBUG)
			debug("Processed URLInfo: url:"+infoURL+" label:"+label);
		
		return inf;
	}

	
	/** 
	 * process import info
	 */
	private void processImport(Attributes attributes){
		ImportModel imp = factory.createImportModel();
		String id  = attributes.getValue("plugin");
		String ver = attributes.getValue("version");
		String match = attributes.getValue("match");
		imp.setPluginIdentifier(id);
		imp.setPluginVersion(ver);
		imp.setMatchingRuleName(match);
		
		if (feature != null) {
			feature.addImportModel(imp);	
			if (DEBUG){
				debug("Processed require: id:"+id+" ver:"+ver);
				debug("Processed require: match:"+match);			
			}
		}		
	}	
	
	/** 
	 * process plugin entry info
	 */
	private void processPlugin(Attributes attributes) {
		PluginEntryModel pluginEntry = factory.createPluginEntryModel();
		String id  = attributes.getValue("id");
		String ver = attributes.getValue("version");
		pluginEntry.setPluginIdentifier(id);
		pluginEntry.setPluginVersion(ver);
		
		String fragment = attributes.getValue("fragment");
		pluginEntry.isFragment(fragment!=null && fragment.trim().equalsIgnoreCase("true"));

		//feature.setOS
		String os = attributes.getValue("os");
		pluginEntry.setOS(os);
		
		//feature.setWS
		String ws = attributes.getValue("ws");
		pluginEntry.setWS(ws);
		
		//feature.setNL
		String nl = attributes.getValue("nl");
		pluginEntry.setNL(nl);
				
		// download size
		long download_size = ContentEntryModel.UNKNOWN_SIZE;
		String download = attributes.getValue("download-size");
		if (download!=null && !download.trim().equals("")){
			try {
				download_size = Long.valueOf(download).longValue();
			} catch (NumberFormatException e){
				// use UNKNOWN_SIZE
			}
		}
		pluginEntry.setDownloadSize(download_size);
				
		// install size	
		long install_size = ContentEntryModel.UNKNOWN_SIZE;
		String install = attributes.getValue("install-size");
		if (install!=null && !install.trim().equals("")){
			try{
				install_size = Long.valueOf(install).longValue();
			} catch (NumberFormatException e){
				// use UNKNOWN_SIZE
			}
		}
		pluginEntry.setInstallSize(install_size);				

		if (feature != null) {
			feature.addPluginEntryModel(pluginEntry);
			if (DEBUG){
				debug("Processed Plugin: id:"+id+" ver:"+ver+" fragment:"+fragment);
				debug("Processed Plugin: os:"+os+" ws:"+ws+" nl:"+nl);			
				debug("Processed Plugin: download size:"+download_size+" install size:"+install_size);
			}	
		}
	}

	/** 
	 * process non-plug-in entry info
	 */
	private void processData(Attributes attributes) {
		NonPluginEntryModel dataEntry = factory.createNonPluginEntryModel();
		String id  = attributes.getValue("id");
		dataEntry.setIdentifier(id);
						
		// download size
		long download_size = ContentEntryModel.UNKNOWN_SIZE;
		String download = attributes.getValue("download-size");
		if (download!=null && !download.trim().equals("")){
			try {
				download_size = Long.valueOf(download).longValue();
			} catch (NumberFormatException e){
				// use UNKNOWN_SIZE
			}
		}
		dataEntry.setDownloadSize(download_size);
				
		// install size	
		long install_size = ContentEntryModel.UNKNOWN_SIZE;
		String install = attributes.getValue("install-size");
		if (install!=null && !install.trim().equals("")){
			try{
				install_size = Long.valueOf(install).longValue();
			} catch (NumberFormatException e){
				// use UNKNOWN_SIZE
			}
		}
		dataEntry.setInstallSize(install_size);		

		if (feature != null) {
			feature.addNonPluginEntryModel(dataEntry);	
			if (DEBUG){
				debug("Processed Data: id:"+id);
				debug("Processed Data: download size:"+download_size+" install size:"+install_size);
			}
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
	public void endElement(String uri, String localName, String qName) {

		if (text!= null) {

			String tag = localName.trim();

			if (tag.equalsIgnoreCase(DESCRIPTION) && feature != null) {
				feature.getDescriptionModel().setAnnotation(text);
						
				if (DEBUG)
					debug("Found Description Text");
			}

			if (tag.equalsIgnoreCase(COPYRIGHT) && feature != null) {
				feature.getCopyrightModel().setAnnotation(text);

				if (DEBUG)
					debug("Found Copyright Text");
				
			}

			if (tag.equalsIgnoreCase(LICENSE) && feature != null) {
				feature.getLicenseModel().setAnnotation(text);
	
				if (DEBUG)
					debug("Found License Text");				
			}
			
			// clean the text
			text = null;
		}
		
		if (DEBUG)
			debug("End Element:"+uri+":"+localName+":"+qName);
	}

	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {
		text = new String(ch,start,length).trim();
	}
	
	private void debug(String s) {
		System.out.println("DefaultFeatureParser: "+s);
	}
}


