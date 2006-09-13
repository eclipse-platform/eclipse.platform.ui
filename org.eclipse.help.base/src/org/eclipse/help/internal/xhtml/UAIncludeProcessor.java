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
package org.eclipse.help.internal.xhtml;

import java.io.InputStream;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.internal.HelpPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UAIncludeProcessor {

	public Document resolveIncludes(Document document) {
		NodeList includes = document.getElementsByTagNameNS("*", "include"); //$NON-NLS-1$ //$NON-NLS-2$
		Node[] nodes = getArray(includes);
		for (int i = 0; i < nodes.length; i++) {
			Element includeElement = (Element) nodes[i];
			UAInclude include = new UAInclude(includeElement);
			Element targetElement = findIncludeTarget(include);
			if (targetElement == null) {
				String message = "Could not resolve following include:  "; //$NON-NLS-1$;
				HelpPlugin.logWarning(message);
				return null;
			}
			Node targetNode = document.importNode(targetElement, true);
			includeElement.getParentNode().replaceChild(targetNode, includeElement);
		}
		return document;
	}
	
	private Element findIncludeTarget(UAInclude include) {
		String path = include.getPath();
		int index = path.lastIndexOf('/');
		if (index >= 0) {
			String href = '/' + path.substring(0, index);
			String includeId = path.substring(index + 1);
			InputStream in = HelpSystem.getHelpContent(href);
			if (in != null) {
				UAContentParser parser = new UAContentParser(in);
				Document dom = parser.getDocument();
				return DOMUtil.getElementById(dom, includeId, "*"); //$NON-NLS-1$
			}
		}
		return null;
	}

	private static Node[] getArray(NodeList nodeList) {
		Node[] nodes = new Node[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++)
			nodes[i] = nodeList.item(i);
		return nodes;
	}
}
