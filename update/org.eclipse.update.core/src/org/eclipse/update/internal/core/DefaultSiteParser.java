package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.VersionedIdentifier;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



public class DefaultSiteParser extends DefaultHandler {

	private SAXParser parser;
	private InputStream siteStream;
	private ISite site;
	private List features = new ArrayList(0);
	/**
	 * Constructor for DefaultSiteParser
	 */
	public DefaultSiteParser(InputStream siteStream,ISite site) throws IOException,SAXException {
		super();
		parser = new SAXParser();
		parser.setContentHandler(this);
		
		this.siteStream = siteStream;
		this.site = site;
		
		parser.parse(new InputSource(this.siteStream));
	}
	
	/**
	 * Gets the features
	 * @return Returns a IFeature[]
	 */
	public List getFeatures() {
		return features;
	}

	
	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(
		String arg0,
		String arg1,
		String arg2,
		Attributes arg3)
		throws SAXException {
		super.startElement(arg0, arg1, arg2, arg3);
		
		if (arg1.trim().equalsIgnoreCase("feature")){
			// if the type doesn';t exist ask teh site for default type
			String id = arg3.getValue("id");
			String ver= arg3.getValue("version");
			VersionedIdentifier versionedId = new VersionedIdentifier(id,ver);
			DefaultPackagedFeature feature = new DefaultPackagedFeature(versionedId,site);
			feature.setLabel(arg3.getValue("name"));
			features.add(feature);
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
	public void endElement(String arg0, String arg1, String arg2)
		throws SAXException {
		super.endElement(arg0, arg1, arg2);
	}

}

