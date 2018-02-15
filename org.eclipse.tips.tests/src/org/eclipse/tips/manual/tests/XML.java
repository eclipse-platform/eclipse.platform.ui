/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.manual.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XML {

	public static void main(String[] args)
			throws ParserConfigurationException, TransformerException, SAXException, IOException {
		String pExpression = "<with variable=\"activeWorkbenchWindow.activePerspective\"><equals value=\"org.eclipse.jdt.ui.JavaPerspective\"></equals></with>";
		pExpression = "<enablement>" + pExpression + "</enablement>";
		pExpression = "<?xml version=\"1.0\"?>" + pExpression;
		System.out.println(pExpression);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(pExpression.getBytes()));
		Element element2 = (Element) doc.getElementsByTagName("enablement").item(0);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(element2);
		StreamResult result = new StreamResult(System.out);
		transformer.transform(source, result);
	}
}
