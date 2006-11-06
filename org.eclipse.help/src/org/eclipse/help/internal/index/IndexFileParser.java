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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.IndexContribution;
import org.eclipse.help.Node;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.dynamic.NodeHandler;
import org.eclipse.help.internal.dynamic.NodeProcessor;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.eclipse.help.internal.toc.HrefUtil;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IndexFileParser extends DefaultHandler {

	private NodeReader reader;
	private NodeProcessor processor;
	
    public IndexContribution parse(IndexFile indexFile) throws IOException, SAXException, ParserConfigurationException {
    	if (reader == null) {
    		reader = new NodeReader();
    		reader.setIgnoreWhitespaceNodes(true);
    	}
    	Node node = reader.read(indexFile.getInputStream());
    	if (processor == null) {
    		processor = new NodeProcessor(new NodeHandler[] {
    			new NormalizeHandler()
    		});
    	}
    	processor.process(node, indexFile.getPluginId());
    	IndexContribution contribution = new IndexContribution();
    	contribution.setId('/' + indexFile.getPluginId() + '/' + indexFile.getFile());
    	contribution.setIndex(node);
    	contribution.setLocale(indexFile.getLocale());
    	return contribution;
    }
    
	/*
	 * Normalizes topic hrefs, by prepending the plug-in id to form an href.
	 * e.g. "path/myfile.html" -> "/my.plugin/path/myfile.html"
	 */
	private class NormalizeHandler extends NodeHandler {
		public short handle(Node node, String id) {
			if (Topic.NAME.equals(node.getName())) {
				String href = node.getAttribute(Topic.ATTRIBUTE_HREF);
				if (href != null) {
					node.setAttribute(Topic.ATTRIBUTE_HREF, HrefUtil.normalizeHref(id, href));
				}
				return HANDLED_CONTINUE;
			}
			return UNHANDLED;
		}
	}
}
