package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

		ClassLoader l = new URLClassLoader(new URL[]{feature.getSite().getURL()},null);
		bundle = ResourceBundle.getBundle("feature",Locale.getDefault(),l);
		
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
		}
	
		if (tag.equalsIgnoreCase(DESCRIPTION)){
			feature.setDescription(processInfo(attributes));
		}
		
		if (tag.equalsIgnoreCase(COPYRIGHT)){
			feature.setCopyright(processInfo(attributes));
		}
		
		if (tag.equalsIgnoreCase(LICENSE)){
			feature.setLicense(processInfo(attributes));
		}
	
		if (tag.equalsIgnoreCase(UPDATE)){
			feature.setUpdateInfo(processURLInfo(attributes));
		}		
		
		if (tag.equalsIgnoreCase(DISCOVERY)){
			feature.addDiscoveryInfo(processURLInfo(attributes));
		}		
		
		if (tag.equalsIgnoreCase(IMPORT)){
			processImport(attributes);
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
		// TODO: Re-implement
		//Assert.isTrue(id.equals(feature.getIdentifier().getIdentifier()));
		//Assert.isTrue(ver.equals(feature.getIdentifier().getVersion()));
		
		// Feature Label
		String label = UpdateManagerPlugin.getDefault().getDescriptor().getResourceString(attributes.getValue("label"),bundle);
		feature.setLabel(label);
		feature.setProvider(attributes.getValue("provider-name"));
		//feature.setImage
		//feature.setOS
		//feature.setWS
		//feature.setNL
	}
	
	/** 
	 * process the info
	 */
	private IInfo processInfo(Attributes attributes){
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerPlugin.getDefault().getDescriptor().getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(feature.getSite().getURL(),infoURL,null);
		Info inf = new Info(url);
		return inf;
	}
	
	/** 
	 * process the info
	 */
	private IInfo processURLInfo(Attributes attributes){
		String infoURL = attributes.getValue("url");
		infoURL = UpdateManagerPlugin.getDefault().getDescriptor().getResourceString(infoURL,bundle);
		URL url = UpdateManagerUtils.getURL(feature.getSite().getURL(),infoURL,null);
		String label = attributes.getValue("url");
		label = UpdateManagerPlugin.getDefault().getDescriptor().getResourceString(label,bundle);
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
	//	String name  = attributes.getValue("name");
	//	String label = attributes.getValue("label");
	//	ICategory category = new DefaultCategory(name,label);
	//	feature.addCategory(category);		
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

