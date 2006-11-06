/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.Node;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Parses content extension XML files into extension model elements.
 */
public class ContentExtensionFileParser extends DefaultHandler {

	private NodeReader reader;

	/*
	 * Parses the specified content extension XML file into model elements.
	 */
    public Node[] parse(Bundle bundle, String path) throws IOException, SAXException, ParserConfigurationException {
    	if (reader == null) {
    		reader = new NodeReader();
    		reader.setIgnoreWhitespaceNodes(true);
    	}
		URL url = bundle.getEntry(path);
		if (url != null) {
			InputStream in = url.openStream();
	    	Node node = reader.read(in);
	    	return node.getChildren();
		}
		return new Node[0];
    }
}
