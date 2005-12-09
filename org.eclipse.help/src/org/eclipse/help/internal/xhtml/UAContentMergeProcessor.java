/***************************************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.util.ResourceLocator;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Handles content manipulation to resolve includes.
 * 
 */
public class UAContentMergeProcessor {


	public Document resolveIncludes(Document document, String locale) {

		NodeList includes = document.getElementsByTagNameNS("*", "include"); //$NON-NLS-1$ //$NON-NLS-2$
		Node[] nodes = getArray(includes);
		for (int i = 0; i < nodes.length; i++) {
			Element includeElement = (Element) nodes[i];
			UAInclude include = new UAInclude(includeElement);
			Element targetElement = findIncludeTarget(include, locale);
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




	/**
	 * Find the target Element pointed to by the path in the include. It is assumed that configId
	 * always points to an external config, and not the same config of the inlcude.
	 * 
	 * @param include
	 * @param path
	 * @return
	 */
	private Element findIncludeTarget(UAInclude include, String locale) {
		String path = include.getPath();
		int index = path.indexOf("/"); //$NON-NLS-1$
		if (index < 0)
			return null;
		String pluginID = path.substring(0, index);
		int lastIndex = path.lastIndexOf("/"); //$NON-NLS-1$
		String pluginRelativePath = path.substring(index + 1, lastIndex);
		String include_id = path.substring(lastIndex + 1, path.length());

		Bundle bundle = Platform.getBundle(pluginID);
		ArrayList pathPrefix = ResourceLocator.getPathPrefix(locale);
		URL flatFileURL = ResourceLocator.find(bundle, new Path(pluginRelativePath), pathPrefix);
		if (flatFileURL != null)
			try {
				InputStream inputStream = flatFileURL.openStream();
				UAContentParser parser = new UAContentParser(inputStream);
				Document dom = parser.getDocument();
				return DOMUtil.getElementById(dom, include_id, "*"); //$NON-NLS-1$
			} catch (IOException e) {
				return null;
			}
		return null;
	}



	/**
	 * Returns an array version of the passed NodeList. Used to work around DOM design issues.
	 */
	public static Node[] getArray(NodeList nodeList) {
		Node[] nodes = new Node[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++)
			nodes[i] = nodeList.item(i);
		return nodes;
	}



}
