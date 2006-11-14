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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.Node;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.dynamic.NodeHandler;
import org.eclipse.help.internal.dynamic.NodeProcessor;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.eclipse.help.internal.dynamic.ValidationHandler;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Parses content extension XML files into extension model elements.
 */
public class ContentExtensionFileParser extends DefaultHandler {

	private NodeReader reader;
	private NodeProcessor processor;
	private Map requiredAttributes;
	private Map deprecatedElements;
	
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
			if ("contentExtension".equals(node.getNodeName())) { //$NON-NLS-1$
				if (processor == null) {
					processor = new NodeProcessor(new NodeHandler[] {
						new ValidationHandler(getRequiredAttributes(), getDeprecatedElements())
					});
				}
				processor.process(node, '/' + bundle.getSymbolicName() + '/' + path);
		    	return node.getChildNodes();
			}
			else {
				String msg = "Required root element \"contentExtension\" missing from user assistance content extension file \"/" + bundle.getSymbolicName() + '/' + path + "\" (skipping)"; //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg);
				return new Node[0];
			}
		}
		else {
			throw new FileNotFoundException();
		}
    }

	private Map getRequiredAttributes() {
		if (requiredAttributes == null) {
			requiredAttributes = new HashMap();
			requiredAttributes.put(ContentExtension.NAME_CONTRIBUTION, new String[] { ContentExtension.ATTRIBUTE_CONTENT, ContentExtension.ATTRIBUTE_PATH });
			requiredAttributes.put(ContentExtension.NAME_CONTRIBUTION_LEGACY, new String[] { ContentExtension.ATTRIBUTE_CONTENT, ContentExtension.ATTRIBUTE_PATH });
			requiredAttributes.put(ContentExtension.NAME_REPLACEMENT, new String[] { ContentExtension.ATTRIBUTE_CONTENT, ContentExtension.ATTRIBUTE_PATH });
			requiredAttributes.put(ContentExtension.NAME_REPLACEMENT_LEGACY, new String[] { ContentExtension.ATTRIBUTE_CONTENT, ContentExtension.ATTRIBUTE_PATH });
		}
		return requiredAttributes;
	}

	private Map getDeprecatedElements() {
		if (deprecatedElements == null) {
			deprecatedElements = new HashMap();
			deprecatedElements.put(ContentExtension.NAME_CONTRIBUTION_LEGACY, ContentExtension.NAME_CONTRIBUTION);
			deprecatedElements.put(ContentExtension.NAME_REPLACEMENT_LEGACY, ContentExtension.NAME_REPLACEMENT);
		}
		return deprecatedElements;
	}
}
