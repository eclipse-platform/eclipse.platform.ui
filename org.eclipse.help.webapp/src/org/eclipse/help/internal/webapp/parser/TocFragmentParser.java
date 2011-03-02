/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.internal.webapp.utils.JSonHelper;
import org.eclipse.help.internal.webapp.utils.XMLHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TocFragmentParser extends ResultParser {

	private Properties properties;
	private String parentId = ""; //$NON-NLS-1$
	private int tagLevel = -1;
	private int level = 0;
	
	public TocFragmentParser() {
		super(JSonHelper.TITLE);
	}

	public void parse(URL tocURL, int level) 
		throws ParserConfigurationException, SAXException, IOException
	{
		parse(tocURL.openStream(), level);
	}
	
	public void parse(InputStream in, int level) 
		throws ParserConfigurationException, SAXException, IOException
	{
		this.level = level;
		super.parse(in);
	}

	public void startElement(String uri, 
			String lname, String name, Attributes attrs) {
		
		if (name.equalsIgnoreCase(XMLHelper.ELEMENT_NODE))
		{
			tagLevel++;
			
			if (tagLevel == level) {
				properties = new Properties();
				properties.put(JSonHelper.PROPERTY_NAME, JSonHelper.TOPIC);
				for (int i = 0; i < attrs.getLength(); i++) {
					String qname = attrs.getQName(i);
					String val = attrs.getValue(i);
					if (qname.equals(XMLHelper.ATTR_ID)) {
						if (parentId.length() > 0)
							val = parentId + '$' + val;
					}
					properties.put(qname, val);
				}
			} else if (parentId.length() <= 0) {
				for (int i = 0; i < attrs.getLength(); i++) {
					if (attrs.getQName(i).equals(XMLHelper.ATTR_ID))
						parentId = attrs.getValue(i);
				}
			}
		}
	}

	public void endElement(String uri, String lname, String name) {

		if (name.equalsIgnoreCase(XMLHelper.ELEMENT_NODE))
		{
			if (tagLevel == level && properties != null ) {
				
				properties.setProperty("type", "toc"); //$NON-NLS-1$ //$NON-NLS-2$
				
				ParseElement element = new ParseElement(properties);
				items.add(element);
				
				properties = null;
			}
			
			tagLevel--;
		}
	}
}
