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

public class NavParser extends ResultParser {

	private ParseElement element = null;

	public NavParser() {
		super(JSonHelper.TITLE);
	}
	
	public void startElement(String uri, String lname, String name, Attributes attrs) {
		
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
	
	public void endElement(String uri, String lname, String name) {
		
		if (element != null) {
			element = element.getParent();
		}
	}
	
	public String toJSON() {
		
		StringBuffer buf = new StringBuffer();
		
		buf.append(JSonHelper.BEGIN_BRACE);
		buf.append(JSonHelper.NEWLINE + JSonHelper.SPACE);
		
		buf.append(JSonHelper.IDENTIFIER);
		buf.append(JSonHelper.COLON);
		buf.append(JSonHelper.getQuotes(id));
		buf.append(JSonHelper.COMMA);

		buf.append(JSonHelper.NEWLINE + JSonHelper.SPACE);
		buf.append(JSonHelper.LABEL);
		buf.append(JSonHelper.COLON);
		buf.append(JSonHelper.getQuotes(label));
		buf.append(JSonHelper.COMMA);
		
		ParseElement elem = (ParseElement) items.get(0);
		if (elem != null)
			buf.append(elem.toJSON());
		
		buf.append(JSonHelper.END_BRACE);
		
		return buf.toString();
	}

}
