package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IInfo;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;




public class DefaultFeatureParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream featureStream;
	private AbstractFeature feature;
	private static final String FEATURE			= "feature";	
	private static final String DESCRIPTION		= "description";
	private static final String COPYRIGHT		= "copyright";	
	private static final String LICENSE 		= "license";	
	private static final String UPDATE			 = "update";
	private static final String DISCOVERY		 = "discovery";
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

		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("Start parsing:"+feature.getURL().toExternalForm());
		}


		try {
			ClassLoader l = new URLClassLoader(new URL[]{((AbstractFeature)feature).getRootURL()},null);
			bundle = ResourceBundle.getBundle("feature",Locale.getDefault(),l);
		} catch (MissingResourceException e){
			//ok, there is no bundle, keep it as null
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
				UpdateManagerPlugin.getPlugin().debug(e.getLocalizedMessage()+":"+((AbstractFeature)feature).getRootURL().toExternalForm());
			}
		}
		
		parser.parse(new InputSource(this.featureStream));
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
		// if the type doesn't exist ask the site for default type
		String id = attributes.getValue("id");
		String ver= attributes.getValue("version");
		// TODO:
		//Assert.isTrue(id.equals(feature.getIdentifier().getIdentifier()),"The feature identifier declared in the Site is different from the one found in the feature");
		//Assert.isTrue(ver.equals(feature.getIdentifier().getVersion()),"The feature version declared in the Site is different from the one found in the feature");
		
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
		
		// DEBUG:		
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
			UpdateManagerPlugin.getPlugin().debug("End process Feature tag: id:"+id+" ver:"+ver+" label:"+label+" provider:"+provider);
			UpdateManagerPlugin.getPlugin().debug("End process Feature tag: image:"+imageURL);
			UpdateManagerPlugin.getPlugin().debug("End process Feature tag: ws:"+ws+" os:"+os+" nl:"+nl); 
		}
	}
	
	/** 
	 * process the info
	 */
	private IInfo processInfo(Attributes attributes){
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
	 * process the info
	 */
	private IInfo processURLInfo(Attributes attributes){
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
		//TODO:
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
			try{
				download_size = Integer.valueOf(download).intValue();
			} catch (NumberFormatException e){
				//FIXME:
				e.printStackTrace();
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
				//FIXME:
				e.printStackTrace();
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

