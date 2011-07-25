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
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.internal.webapp.utils.JSonHelper;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ResultParser extends DefaultHandler {

	protected String id;
	protected String label;
	protected ArrayList items = new ArrayList(); //parser populates the items arrayList withe parsed data.

	public ResultParser(String label) {
		this(label, JSonHelper.ID);
	}
	
	public ResultParser(String label, String id) {
		this.label = label;
		this.id = id;
	}
	
	public void parse(URL url) 
		throws ParserConfigurationException, SAXException, IOException
	{
		parse(url.openStream());
	}
	
	public void parse(InputStream in) 
		throws ParserConfigurationException, SAXException, IOException
	{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(in, this);
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setIdentifier(String id) {
		this.id = id;
	}
	
	public ArrayList getItems()
	{
		return items;
	}
	
	public String toString()
	{
		return items.toString();
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

		buf.append(JSonHelper.NEWLINE + JSonHelper.SPACE);
		buf.append(JSonHelper.ITEMS);
		buf.append(JSonHelper.COLON);
		buf.append(JSonHelper.BEGIN_BRACKET);
		
		for (int i = 0; i < items.size(); i++) {
			
			if (i > 0)
				buf.append(JSonHelper.COMMA);
			
			ParseElement element = (ParseElement) items.get(i);
			buf.append(element.toJSON(1));
		}
		
		if (items.size() > 0)
			buf.append(JSonHelper.NEWLINE + JSonHelper.SPACE);
		
		buf.append(JSonHelper.END_BRACKET);
		buf.append(JSonHelper.NEWLINE);
		buf.append(JSonHelper.END_BRACE);
		
		return buf.toString();
	}
}
