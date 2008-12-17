/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.xml.engine;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.core.impl.engine.CSSErrorHandlerImpl;
import org.akrogen.tkui.css.core.resources.CSSCoreResources;
import org.akrogen.tkui.css.xml.engine.CSSXMLEngineImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class CSSXMLEngineImplTest {

	public static void main(String[] args) {

		// Instanciate XML CSS Engine
		CSSEngine engine = new CSSXMLEngineImpl();
		engine.setErrorHandler(CSSErrorHandlerImpl.INSTANCE);

		try {

			engine.parseStyleSheet(CSSCoreResources.getHTMLFont());

			// Load XML into DOM
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(CSSXMLEngineImplTest.class
					.getResourceAsStream("test.xhtml"));

			// Apply Styles
			engine.applyStyles(document.getDocumentElement(), true);

			StringWriter writer = new StringWriter();
			OutputFormat outputFormat = new OutputFormat();
			outputFormat.setOmitXMLDeclaration(true);
			XMLSerializer serializer = new XMLSerializer(writer, outputFormat);
			serializer.serialize(document);

			System.out.println(writer.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
