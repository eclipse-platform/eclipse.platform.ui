/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation - 122967 [Help] Remote help system
 *******************************************************************************/
package org.eclipse.help.internal.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.IndexContribution;
import org.eclipse.help.Node;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IndexFileParser extends DefaultHandler {

	private NodeReader reader;
	
    public IndexContribution parse(IndexFile indexFile) throws IOException, SAXException, ParserConfigurationException {
    	if (reader == null) {
    		reader = new NodeReader();
    		reader.setIgnoreWhitespaceNodes(true);
    	}
    	InputStream in = indexFile.getInputStream();
    	if (in != null) {
	    	Node node = reader.read(indexFile.getInputStream());
	    	IndexContribution contribution = new IndexContribution();
	    	contribution.setId('/' + indexFile.getPluginId() + '/' + indexFile.getFile());
	    	contribution.setIndex(node);
	    	contribution.setLocale(indexFile.getLocale());
	    	return contribution;
    	}
    	else {
    		throw new FileNotFoundException();
    	}
    }
}
