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
import org.eclipse.update.core.IInfo;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.UpdateManagerPlugin;
import org.eclipse.update.core.VersionedIdentifier;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;




public class DefaultFeatureParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream featureStream;
	private AbstractFeature feature;
	private static final String FEATURE			= "feature";	
	private static final String DESCRIPTION		= "description";
	private static final String COPYRIGHT		= "copyright";	
	private static final String LICENSE 		= "license";	
	private static final String UPDATE			 	= "update";
	private static final String DISCOVERY		 	= "discovery";
	private static final String IMPORT			= "import";	
	private static final String PLUGIN			= "plugin";	
	
	private String text;
	private ResourceBundle	bundle;
		

	/**
	 * Constructor for DefaultFeatureParser
	 */
	public DefaultFeatureParser(InputStream featureStream,IFeature feature) throws IOException,SAXException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);
		
		this.featureStream = featureStream;
		Assert.isTrue(feature instanceof AbstractFeature);
		this.feature = (AbstractFeature)feature;

		try {
			ClassLoader l = new URLClassLoader(new URL[]{feature.getURL()},null);
			bundle = ResourceBundle.getBundle("feature",Locale.getDefault(),l);
		} catch (MissingResourceException e){
			//ok, there is no bundle, keep it as null
		}
		
		parser.parse(new InputSource(this.featureStream));
	}
	
	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName,String qName, Attributes attributes)
		throws SAXException {

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

	}
	
	
	/** 
	 * process the Feature info
	 */
	private void processFeature(Attributes attributes){
		// if the type doesn';t exist ask the site for default type
		String id = attributes.getValue("id");
		String ver= attributes.getValue("version");
		// TODO:
		//Assert.isTrue(id.equals(feature.getIdentifier().getIdentifier()),"The feature identifier declared in the Site is different from the one found in the feature");
		//Assert.isTrue(ver.equals(feature.getIdentifier().getVersion()),"The feature version declared in the Site is different from the one found in the feature");
		
		// Feature Label
		String label = UpdateManagerUtils.getResourceString(attributes.getValue("label"),bundle);
		feature.setLabel(label);
		
		feature.setProvider(attributes.getValue("provider-name"));
		
		//image
		URL imageURL = UpdateManagerUtils.getURL(feature.getURL(),attributes.getValue("image"),null);
		feature.setImage(imageURL); 
		//feature.setOS
		//feature.setWS
		//feature.setNL
	}
	
	/** 
	 * process the info
	 */
	private IInfo processInfo(Attributes attributes){
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(feature.getURL(),infoURL,null);
		Info inf = new Info(url);
		return inf;
	}
	
	/** 
	 * process the info
	 */
	private IInfo processURLInfo(Attributes attributes){
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerUtils.getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(feature.getSite().getURL(),infoURL,null);
		String label = attributes.getValue("url");
		label = UpdateManagerUtils.getResourceString(label,bundle);
		IInfo inf = new Info(label,url);
		return inf;
	}
	
	
	
	
	/** 
	 * process the Archive info
	 */
	private void processImport(Attributes attributes){
	}	
	
	/** 
	 * process the Category Def info
	 */
	private void processPlugin(Attributes attributes){
		String id  = attributes.getValue("id");
		String ver = attributes.getValue("version");
		PluginEntry pluginEntry = new PluginEntry(id,ver);
		
		String fragment = attributes.getValue("fragment");
		pluginEntry.setFragment(fragment.trim().equalsIgnoreCase("true"));
		//os
		//ws
		//nl
		
		int download_size = 0;
		String download = attributes.getValue("download-size");
		if (download==null || download.trim().equals("")){
			download_size = -1;
		} else {
			try{
				download_size = Integer.valueOf(download).intValue();
			} catch (NumberFormatException e){
				//FIXME:
				e.printStackTrace();
			}
		}
			
		int install_size = 0;
		String install = attributes.getValue("install-size");
		if (install==null || install.trim().equals("")){
			install_size = -1;
		} else {
			try{
				install_size = Integer.valueOf(install).intValue();
			} catch (NumberFormatException e){
				//FIXME:
				e.printStackTrace();
			}
		}				
	
		feature.addPluginEntry(pluginEntry);
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
			}

			if (tag.equalsIgnoreCase(COPYRIGHT)) {
				((Info)feature.getCopyright()).setText(text);
			}

			if (tag.equalsIgnoreCase(LICENSE)) {
				((Info)feature.getLicense()).setText(text);
			}
			// clean the text
			text = null;
		}

	}

	/**
	 * Gets the feature
	 * @return Returns a IFeature
	 */
	public IFeature getFeature() {
		return feature;
	}
	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		text = new String(ch,start,length);
	}

}

