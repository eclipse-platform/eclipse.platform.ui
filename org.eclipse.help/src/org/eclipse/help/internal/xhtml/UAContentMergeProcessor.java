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
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
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

	protected static final String CONTENT_EXTENSION = "org.eclipse.help.contentExtension"; //$NON-NLS-1$

	protected static IExtensionRegistry registry;
	protected static IConfigurationElement[] contentExtensionElements;
	static {
		registry = Platform.getExtensionRegistry();
		contentExtensionElements = getContentExtensions();
	}

	private Hashtable unresolvedConfigExt = new Hashtable();


	private String pluginID = null;
	private String file = null;
	private Document document = null;
	private String locale = null;


	protected UAContentMergeProcessor(String pluginID, String file, Document document, String locale) {
		this.pluginID = pluginID;
		this.file = file;
		this.document = document;
		this.locale = locale;

	}


	public Document resolveIncludes() {

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




	public static Node[] getArray(NodeList nodeList) {
		Node[] nodes = new Node[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++)
			nodes[i] = nodeList.item(i);
		return nodes;
	}



	protected static IConfigurationElement[] getContentExtensions() {
		IConfigurationElement[] contentExtensionElements = registry
				.getConfigurationElementsFor(CONTENT_EXTENSION);
		return contentExtensionElements;
	}


	public Document resolveContentExtensions() {
		for (int i = 0; i < contentExtensionElements.length; i++)
			resolveContentExtension(contentExtensionElements[i]);
		return document;
	}


	private void resolveContentExtension(IConfigurationElement contentExtElement) {
		Document contentExtensionDom = loadContentExtension(contentExtElement);
		if (contentExtensionDom == null)
			return;
		resolveContentExtension(contentExtensionDom, contentExtElement);
	}


	private void resolveContentExtension(Document contentExtensionDom, IConfigurationElement contentExtElement) {
		Bundle bundle = BundleUtil.getBundleFromConfigurationElement(contentExtElement);
		Element[] topicExtensions = DOMUtil.getElementsByTagName(contentExtensionDom, "topicExtension"); //$NON-NLS-1$
		if (topicExtensions != null) {
			for (int i = 0; i < topicExtensions.length; i++)
				doResolveContentExtension(topicExtensions[i], bundle);
		}

		Element[] topicReplaces = DOMUtil.getElementsByTagName(contentExtensionDom, "topicReplace"); //$NON-NLS-1$
		if (topicReplaces != null) {
			for (int i = 0; i < topicReplaces.length; i++)
				doResolveContentReplace(topicReplaces[i], bundle);
		}
	}

	private void doResolveContentExtension(Element topicExtension, Bundle bundle) {
		UATopicExtension topicExtensionModel = new UATopicExtension(topicExtension, bundle);
		boolean isExtensionToCurrentPage = resolveTopicExtension(topicExtensionModel);
		if (isExtensionToCurrentPage) {
			if (topicExtension.hasAttribute("failed")) { //$NON-NLS-1$
				if (!unresolvedConfigExt.containsKey(topicExtension))
					unresolvedConfigExt.put(topicExtension, bundle);
			} else {
				unresolvedConfigExt.remove(topicExtension);
				tryResolvingExtensions();
			}
		}
	}


	private void tryResolvingExtensions() {
		Enumeration keys = unresolvedConfigExt.keys();
		while (keys.hasMoreElements()) {
			Element topicExtensionElement = (Element) keys.nextElement();
			doResolveContentExtension(topicExtensionElement, (Bundle) unresolvedConfigExt
					.get(topicExtensionElement));
		}
	}


	/**
	 * Insert the topic extension content into the target page if the target page happens to be this
	 * page.
	 * 
	 * @param extensionContent
	 * @return
	 */
	private boolean resolveTopicExtension(UATopicExtension topicExtension) {

		Element anchorElement = findAnchor(topicExtension, locale);
		if (anchorElement == null) {
			if (topicExtension.getElement().hasAttribute("failed")) //$NON-NLS-1$
				return true;
			else
				return false;
		}
		Document topicExtensionDom = topicExtension.getDocument();
		if (topicExtensionDom == null)
			return false;
		Element extensionBody = DOMUtil.getBodyElement(topicExtensionDom);
		Element[] children = DOMUtil.getElementsByTagName(extensionBody, "*"); //$NON-NLS-1$
		for (int i = 0; i < children.length; i++) {
			Node targetNode = document.importNode(children[i], true);
			anchorElement.getParentNode().insertBefore(targetNode, anchorElement);
		}
		return true;
	}


	private Element findAnchor(UATopicExtension topicExtension, String locale) {
		String path = topicExtension.getPath();
		int index = path.indexOf("/"); //$NON-NLS-1$
		if (index < 0)
			return null;
		String pluginID = path.substring(0, index);
		int lastIndex = path.lastIndexOf("/"); //$NON-NLS-1$
		String pluginRelativePath = path.substring(index + 1, lastIndex);
		String anchor_id = path.substring(lastIndex + 1, path.length());

		if (this.pluginID.equals(pluginID) && this.file.equals(pluginRelativePath)) {
			Element anchor = DOMUtil.getElementById(document, anchor_id, "*"); //$NON-NLS-1$ 
			if (anchor == null)
				topicExtension.getElement().setAttribute("failed", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			return anchor;
		}
		return null;
	}


	protected Document loadContentExtension(IConfigurationElement cfgElement) {
		String content = cfgElement.getAttribute("file"); //$NON-NLS-1$
		content = BundleUtil.getResourceLocation(content, cfgElement);
		Document document = new UAContentParser(content).getDocument();
		return document;
	}


	private void doResolveContentReplace(Element topicReplace, Bundle bundle) {
		UATopicExtension topicReplaceModel = new UATopicExtension(topicReplace, bundle);
		boolean isExtensionToCurrentPage = resolveTopicReplace(topicReplaceModel);
		if (isExtensionToCurrentPage) {
			if (topicReplace.hasAttribute("failed")) { //$NON-NLS-1$
				if (!unresolvedConfigExt.containsKey(topicReplace))
					unresolvedConfigExt.put(topicReplace, bundle);
			} else {
				unresolvedConfigExt.remove(topicReplace);
				// tryResolvingExtensions();
			}
		}
	}



	private boolean resolveTopicReplace(UATopicExtension topicReplace) {

		Element replaceElement = findReplaceElementById(topicReplace, locale);
		if (replaceElement == null) {
			if (topicReplace.getElement().hasAttribute("failed")) //$NON-NLS-1$
				return true;
			else
				return false;
		}
		Document topicExtensionDom = topicReplace.getDocument();
		if (topicExtensionDom == null)
			return false;
		Element extensionBody = DOMUtil.getBodyElement(topicExtensionDom);
		Element[] children = DOMUtil.getElementsByTagName(extensionBody, "*"); //$NON-NLS-1$
		for (int i = 0; i < children.length; i++) {
			Node targetNode = document.importNode(children[i], true);
			replaceElement.getParentNode().insertBefore(targetNode, replaceElement);
		}
		return true;
	}

	private Element findReplaceElementById(UATopicExtension topicReplace, String locale) {
		String path = topicReplace.getPath();
		int index = path.indexOf("/"); //$NON-NLS-1$
		if (index < 0)
			return null;
		String pluginID = path.substring(0, index);
		int lastIndex = path.lastIndexOf("/"); //$NON-NLS-1$
		String pluginRelativePath = path.substring(index + 1, lastIndex);
		String element_id = path.substring(lastIndex + 1, path.length());

		if (this.pluginID.equals(pluginID) && this.file.equals(pluginRelativePath)) {
			Element elementToReplace = DOMUtil.getElementById(document, element_id, "*"); //$NON-NLS-1$ 
			if (elementToReplace == null)
				topicReplace.getElement().setAttribute("failed", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			return elementToReplace;
		}
		return null;
	}



}
