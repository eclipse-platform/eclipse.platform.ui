package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;




/**
 * Parse the feature.xml
 */



public class FeatureParser extends DefaultHandler {


	private SAXParser parser;
	//private InputStream featureStream;
	private Feature feature;
	private static final String FEATURE				= "feature";	
	private static final String DESCRIPTION		= "description";
	private static final String COPYRIGHT			= "copyright";	
	private static final String LICENSE 				= "license";	
	private static final String UPDATE					= "update";
	private static final String DISCOVERY			= "discovery";
	private static final String IMPORT					= "import";	
	private static final String PLUGIN					= "plugin";	
	private static final String DATA						= "data";	
	private static final String GROUP					= "group";			
	
	private String text;
	private ResourceBundle	bundle;
		


	/**
	 * Constructor for DefaultFeatureParser
	 */
	public FeatureParser(InputStream featureStream,IFeature feature) throws IOException,SAXException, CoreException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);
		
		Assert.isTrue(feature instanceof Feature);
		this.feature = (Feature)feature;

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Start parsing:"+feature.getURL().toExternalForm());
		}

		bundle = ((Feature)feature).getResourceBundle();
		
		parser.parse(new InputSource(featureStream));
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
		
		try {
		String tag = localName.trim();
	
		if (tag.equalsIgnoreCase(FEATURE)){
			processFeature(attributes);
			return;
		}
	
		if (tag.equalsIgnoreCase(DESCRIPTION)){
			feature.setDescription(processInfo(attributes));
			return;
		}
		
		if (tag.equalsIgnoreCase(COPYRIGHT)){
			feature.setCopyright(processInfo(attributes));
			return;
		}
		
		if (tag.equalsIgnoreCase(LICENSE)){
			feature.setLicense(processInfo(attributes));
			return;
		}
	
		if (tag.equalsIgnoreCase(UPDATE)){
			feature.setUpdateInfo(processURLInfo(attributes));
			return;
		}		
		
		if (tag.equalsIgnoreCase(DISCOVERY)){
			feature.addDiscoveryInfo(processURLInfo(attributes));
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


		} catch (MalformedURLException e){
			throw new SAXException("error processing URL. Check the validity of the URLs.",e);
		} catch (Exception e){
			throw new SAXException("error creating temporary feature on the local file system.",e);
		}

	}
	
	
	/** 
	 * process the Feature info
	 */
	private void processFeature(Attributes attributes) throws MalformedURLException, IOException, CoreException {
		// if the type doesn't exist ask the site for default type
		String id = attributes.getValue("id");
		String ver= attributes.getValue("version");
		VersionedIdentifier versionedId = new VersionedIdentifier(id, ver);
		feature.setIdentifier(versionedId);
		
		// Feature Label
		String label = UpdateManagerUtils.getResourceString(attributes.getValue("label"),bundle);
		feature.setLabel(label);
		
		String provider = attributes.getValue("provider-name");
		feature.setProvider(provider);
		
		//image
		URL imageURL = UpdateManagerUtils.getURL(feature.getRootURL(),attributes.getValue("image"),null);
		feature.setImage(imageURL); 
		
		//feature.setOS
		String os = attributes.getValue("os");
		feature.setOS(os);
		
		//feature.setWS
		String ws = attributes.getValue("ws");
		feature.setWS(ws);
		
		//feature.setNL
		String nl = attributes.getValue("nl");
		feature.setNL(nl);
		
		//feature.setApplication
		String application = attributes.getValue("application"); 
		feature.setApplication(application);		
		
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End process Feature tag: id:"+id+" ver:"+ver+" label:"+label+" provider:"+provider);
			UpdateManagerPlugin.getPlugin().debug("End process Feature tag: image:"+imageURL);
			UpdateManagerPlugin.getPlugin().debug("End process Feature tag: ws:"+ws+" os:"+os+" nl:"+nl+" application:"+application); 
		}
	}
	
	/** 
	 * process the info
	 */
	private IInfo processInfo(Attributes attributes) throws MalformedURLException, IOException, CoreException {
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(feature.getRootURL(),infoURL,null);
		Info inf = new Info(url);

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Processed Info: url:"+infoURL);
		}
		
		return inf;
	}
	
	/** 
	 * process the URL info
	 */
	private IInfo processURLInfo(Attributes attributes) throws MalformedURLException, IOException, CoreException {
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(feature.getRootURL(),infoURL,null);
		String label = attributes.getValue("label");
		label = UpdateManagerUtils.getResourceString(label,bundle);
		IInfo inf = new Info(label,url);
		

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Processed URLInfo: url:"+infoURL+" label:"+label);
		}

		
		return inf;
	}

	
	/** 
	 * process the Archive info
	 */
	private void processImport(Attributes attributes){
		String id  = attributes.getValue("plugin");
		String ver = attributes.getValue("version");
		String match = attributes.getValue("match");
		
		int rule = IImport.RULE_NONE;
		if (match==null || match.trim().equals("")){
				rule = IImport.RULE_COMPATIBLE;
		} else {
		if (match.trim().equalsIgnoreCase("compatible")) 
			rule = IImport.RULE_COMPATIBLE;
			else if (match.trim().equalsIgnoreCase("perfect")) 
				rule = IImport.RULE_PERFECT;
				else	if (match.trim().equalsIgnoreCase("equivalent"))
					rule = IImport.RULE_EQUIVALENT ;
					else if (match.trim().equalsIgnoreCase("greaterOrHigher"))
						rule = IImport.RULE_GRATER_OR_EQUAL; 
		}
		
		feature.addImport(new DefaultImport(id,ver,rule));

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Processed require: id:"+id+" ver:"+ver);
			UpdateManagerPlugin.getPlugin().debug("Processed require: match:"+match+" ->:"+rule);			
		}
		
	}	
	
	/** 
	 * process the Plugin info
	 */
	private void processPlugin(Attributes attributes) {
		String id  = attributes.getValue("id");
		String ver = attributes.getValue("version");
		PluginEntry pluginEntry = new PluginEntry(id,ver);
		
		String fragment = attributes.getValue("fragment");
		pluginEntry.setFragment(fragment!=null && fragment.trim().equalsIgnoreCase("true"));

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
		int download_size = -1;
		String download = attributes.getValue("download-size");
		if (download!=null && !download.trim().equals("")){
		try {
			download_size = Integer.valueOf(download).intValue();
		} catch (NumberFormatException e){
			String pluginId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,pluginId,IStatus.OK,"Invalid download Size:"+download+"  for plugin:"+id+"  version:"+ver,e);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}
		}
		pluginEntry.setDownloadSize(download_size);
		
		
		// install size	
		int install_size = -1;
		String install = attributes.getValue("install-size");
		if (install!=null && !install.trim().equals("")){
			try{
				install_size = Integer.valueOf(install).intValue();
			} catch (NumberFormatException e){
			String pluginId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,pluginId,IStatus.OK,"Invalid install size:"+install+"  for plugin:"+id+"  version:"+ver,e);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
			}
		}
		pluginEntry.setInstallSize(install_size);				

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Processed Plugin: id:"+id+" ver:"+ver+" fragment:"+fragment);
			UpdateManagerPlugin.getPlugin().debug("Processed Plugin: os:"+os+" ws:"+ws+" nl:"+nl);			
			UpdateManagerPlugin.getPlugin().debug("Processed Plugin: download size:"+download_size+" install size:"+install_size);
		}

	
		feature.addPluginEntry(pluginEntry);
	}

	/** 
	 * process the Data info
	 */
	private void processData(Attributes attributes) {
		String id  = attributes.getValue("id");
		DataEntry dataEntry = new DataEntry(id);
		
		// download size
		int download_size = -1;
		String download = attributes.getValue("download-size");
		if (download!=null && !download.trim().equals("")){
		try {
			download_size = Integer.valueOf(download).intValue();
		} catch (NumberFormatException e){
			String pluginId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,pluginId,IStatus.OK,"Invalid download Size:"+download+"  for plugin:"+id,e);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		}
		}
		dataEntry.setDownloadSize(download_size);
		
		
		// install size	
		int install_size = -1;
		String install = attributes.getValue("install-size");
		if (install!=null && !install.trim().equals("")){
			try{
				install_size = Integer.valueOf(install).intValue();
			} catch (NumberFormatException e){
			String pluginId = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,pluginId,IStatus.OK,"Invalid install size:"+install+"  for plugin:"+id,e);
			UpdateManagerPlugin.getPlugin().getLog().log(status);
			}
		}
		dataEntry.setInstallSize(install_size);				

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Processed Data: id:"+id);
			UpdateManagerPlugin.getPlugin().debug("Processed Data: download size:"+download_size+" install size:"+install_size);
		}

	
		feature.addDataEntry(dataEntry);
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
	public void endElement(String uri, String localName, String qName)
		throws SAXException {

		if (text!= null) {

			String tag = localName.trim();

			if (tag.equalsIgnoreCase(DESCRIPTION)) {
				((Info)feature.getDescription()).setText(text);
				
				// DEBUG:		
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
					UpdateManagerPlugin.getPlugin().debug("Found Description Text");
				}
			}


			if (tag.equalsIgnoreCase(COPYRIGHT)) {
				((Info)feature.getCopyright()).setText(text);

				// DEBUG:		
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
					UpdateManagerPlugin.getPlugin().debug("Found Copyright Text");
				}
				
			}


			if (tag.equalsIgnoreCase(LICENSE)) {
				((Info)feature.getLicense()).setText(text);

				// DEBUG:		
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
					UpdateManagerPlugin.getPlugin().debug("Found License Text");
				}
				
			}
			// clean the text
			text = null;
		}

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End Element:"+uri+":"+localName+":"+qName);
		}
	}


	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		text = new String(ch,start,length).trim();
	}


}


