package org.eclipse.team.internal.ui.target;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TargetSiteContentHandler extends DefaultHandler implements ContentHandler {
	private StringBuffer buffer;
	private List propList;
	private String target;

	/**
	 * Constructor for TargetSiteContentHandler.
	 */
	public TargetSiteContentHandler() {
		super();
		propList=new LinkedList();
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (buffer!=null) 
			buffer.append(ch, start, length); 
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(String, String, String)
	 */
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (qName.equals("site")) {
			Properties props=new Properties();
			byte[] bytes=buffer.toString().getBytes();
			InputStream iStream=new ByteArrayInputStream(bytes);
			try {
				props.load(iStream);
				props.setProperty("target", target);
				propList.add(props);
			} catch (IOException e) {
				//TODO: log an error.
			}
		}
	}
	
	/**
	 * Retrieve the property list that has been built up.
	 * @return Properties[]
	 */
	Properties[] getProperties() {
		return (Properties[]) propList.toArray(new Properties[propList.size()]);
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals("site")) {
			buffer = new StringBuffer();
			target=atts.getValue("target");
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		;
	}

}
