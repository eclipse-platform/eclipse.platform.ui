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

import java.util.Properties;

import org.eclipse.help.internal.webapp.utils.JSonHelper;
import org.xml.sax.Attributes;

public class ExtensionParser extends ResultParser {

	private ParseElement element = null;
	private String currentTag;

	public ExtensionParser() {
		super(JSonHelper.TITLE);
	}
	
	public void startElement(String uri, String lname, String name, Attributes attrs) {
		
		currentTag = name;
		
		Properties properties = new Properties();
		properties.put(JSonHelper.PROPERTY_NAME, name);
		for (int i = 0; i < attrs.getLength(); i++) {
			String qname = attrs.getQName(i);
			String val = attrs.getValue(i);
			properties.put(qname, val);
		}
		
		ParseElement elem = new ParseElement(properties, element);
		if (element != null)
			element.addChild(elem);
		else
			items.add(elem);
		
		element = elem;
		
	}
	
	public void characters(char[] ch, int start, int length) {
		
		if (element != null) {
			
			Properties properties = element.getProps();
			if (properties != null) {
				
				String content = new String(ch, start, length);
				
				String existing = (String) properties.get(currentTag);
				if (existing == null)
					existing = ""; //$NON-NLS-1$
				
				content = content.replaceAll("[\\n\\t]", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				
				properties.put(currentTag, existing + content);
				element.updateParseElement(properties);
			}
		}
	}
	
	public void endElement(String uri, String lname, String name) {
		
		if (element != null) {
			element = element.getParent();
		}		
	}

}
