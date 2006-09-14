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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.remote.RemoteExtensionProvider;
import org.eclipse.help.internal.base.remote.RemoteHelp;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UAExtensionProcessor {

	private static final String EXTENSION_POINT_ID_CONTENT_EXTENSION = HelpPlugin.PLUGIN_ID + ".contentExtension"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_CONTENT_EXTENSION = "contentExtension"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_TOPIC_EXTENSION = "topicExtension"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_TOPIC_REPLACE = "topicReplace"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_FILE = "file"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_CONTENT = "content"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_PATH = "path"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_ANCHOR = "anchor"; //$NON-NLS-1$
	
	private List topicExtensions;
	private Map topicExtensionsByTargetHref;
	private List topicReplaces;
	private Map topicReplacesByTargetHref;
	
	/*
	 * Constructs a new extension processor.
	 */
	public UAExtensionProcessor() {
		RemoteHelp.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				clearCache();
			}
		});
	}
	
	public Collection getTopicExtensions() {
		if (topicExtensions == null) {
			discoverExtensions();
		}
		return topicExtensions;
	}
	
	public Collection getTopicReplaces() {
		if (topicReplaces == null) {
			discoverExtensions();
		}
		return topicReplaces;
	}
	
	public void resolveExtensions(Document doc, String href) {
		List topicExtensions = getTopicExtensionsFor(href);
		List topicReplaces = getTopicReplacesFor(href);
		Iterator iter = topicExtensions.iterator();
		while (iter.hasNext()) {
			UATopicExtension extension = (UATopicExtension)iter.next();
			resolveTopicExtension(doc, extension);
		}
		iter = topicReplaces.iterator();
		while (iter.hasNext()) {
			UATopicReplace replace = (UATopicReplace)iter.next();
			resolveTopicReplace(doc, replace);
		}
	}
	
	private void resolveTopicExtension(Document doc, UATopicExtension ext) {
		Element targetAnchor = DOMUtil.getElementById(doc, ext.getTargetAnchorId(), ATTRIBUTE_NAME_ANCHOR);
		if (targetAnchor != null) {
			Node parent = targetAnchor.getParentNode();
			Element[] contentElements = getContentElements(ext.getContentHref(), ext.getContentElementId());
			for (int i=0;i<contentElements.length;++i) {
				Node node = doc.importNode(contentElements[i], true);
				parent.insertBefore(node, targetAnchor);
			}
		}
		else {
			String msg = "Unable to locate anchor " + ext.getTargetAnchorId() + " in help document " + ext.getTargetHref() + " while processing topicExtension for " + ext.getContentHref(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			HelpPlugin.logError(msg, null);
		}
	}

	private void resolveTopicReplace(Document doc, UATopicReplace replace) {
		Element targetElement = doc.getElementById(replace.getTargetElementId());
		if (targetElement != null) {
			Node parent = targetElement.getParentNode();
			Element[] contentElements = getContentElements(replace.getContentHref(), replace.getContentElementId());
			for (int i=0;i<contentElements.length;++i) {
				Node node = doc.importNode(contentElements[i], true);
				parent.insertBefore(node, targetElement);
			}
			parent.removeChild(targetElement);
		}
		else {
			String msg = "Unable to locate element with id " + replace.getTargetElementId() + " in help document " + replace.getTargetHref() + " while processing topicReplace for " + replace.getContentHref(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			HelpPlugin.logError(msg, null);
		}
	}

	private Element[] getContentElements(String href, String elementId) {
		InputStream in = HelpSystem.getHelpContent(href);
		if (in != null) {
			UAContentParser parser = new UAContentParser(in);
			Document doc = parser.getDocument();
			if (doc != null) {
				if (elementId != null) {
					// id specified, only get that element
					return new Element[] { doc.getElementById(elementId) };
				}
				else {
					// no id specified, use the whole body
					Element extensionBody = DOMUtil.getBodyElement(doc);
					return DOMUtil.getElementsByTagName(extensionBody, "*"); //$NON-NLS-1$
				}
			}
		}
		else {
			String msg = "Unable to locate help document " + href + " for contentExtension"; //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
		}
		return new Element[0];
	}
	
	private List getTopicExtensionsFor(String href) {
		if (topicExtensionsByTargetHref == null) {
			discoverExtensions();
		}
		List extensions = (List)topicExtensionsByTargetHref.get(href);
		if (extensions != null) {
			return extensions;
		}
		return Collections.EMPTY_LIST;
	}
	
	private List getTopicReplacesFor(String href) {
		if (topicReplacesByTargetHref == null) {
			discoverExtensions();
		}
		List replaces = (List)topicReplacesByTargetHref.get(href);
		if (replaces != null) {
			return replaces;
		}
		return Collections.EMPTY_LIST;
	}
	
	private void discoverExtensions() {
		topicExtensions = new ArrayList();
		topicExtensionsByTargetHref = new HashMap();
		topicReplaces = new ArrayList();
		topicReplacesByTargetHref = new HashMap();
		discoverLocalExtensions();
		discoverRemoteExtensions();
	}
	
	private void discoverLocalExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_POINT_ID_CONTENT_EXTENSION);
		for (int i=0;i<elements.length;++i) {
			String pluginId = elements[i].getContributor().getName();
			String file = elements[i].getAttribute(ATTRIBUTE_NAME_FILE);
			if (elements[i].getName().equals(ELEMENT_NAME_CONTENT_EXTENSION)) {
				if (file != null) {
					List extensions = parseExtensions(pluginId, file);
					Iterator iter = extensions.iterator();
					while (iter.hasNext()) {
						addExtension(iter.next());
					}
				}
				else {
					String msg = "Required attribute " + ATTRIBUTE_NAME_FILE + " missing from " + ELEMENT_NAME_CONTENT_EXTENSION + " element for extension point " + EXTENSION_POINT_ID_CONTENT_EXTENSION + " in plug-in " + elements[i].getContributor().getName(); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
					HelpPlugin.logError(msg, null);
				}
			}
		}
	}
	
	private void discoverRemoteExtensions() {
		RemoteExtensionProvider provider = new RemoteExtensionProvider();
		List extensions = provider.getExtensions();
		Iterator iter = extensions.iterator();
		while (iter.hasNext()) {
			addExtension(iter.next());
		}
	}
	
	private void addExtension(Object extension) {
		if (extension instanceof UATopicExtension) {
			UATopicExtension ext = (UATopicExtension)extension;
			topicExtensions.add(ext);
			List list = (List)topicExtensionsByTargetHref.get(ext.getTargetHref());
			if (list != null) {
				list.add(ext);
			}
			else {
				list = new ArrayList();
				list.add(ext);
				topicExtensionsByTargetHref.put(ext.getTargetHref(), list);
			}
		}
		else if (extension instanceof UATopicReplace) {
			UATopicReplace replace = (UATopicReplace)extension;
			topicReplaces.add(replace);
			List list = (List)topicReplacesByTargetHref.get(replace.getTargetHref());
			if (list != null) {
				list.add(replace);
			}
			else {
				list = new ArrayList();
				list.add(replace);
				topicReplacesByTargetHref.put(replace.getTargetHref(), list);
			}
		}

	}

	private List parseExtensions(String pluginId, String file) {
		Bundle bundle = Platform.getBundle(pluginId);
		URL url = bundle.getEntry(file);
		if (url != null) {
			try {
				InputStream in = url.openStream();
				UAContentParser parser = new UAContentParser(in);
				Document doc = parser.getDocument();
				Element[] topicExtensionElements = DOMUtil.getElementsByTagName(doc, ELEMENT_NAME_TOPIC_EXTENSION);
				List list = new ArrayList(topicExtensionElements.length);
				for (int i=0;i<topicExtensionElements.length;++i) {
					UATopicExtension ext = parseTopicExtension(pluginId, topicExtensionElements[i]);
					if (ext != null) {
						list.add(ext);
					}
				}
				Element[] topicReplaceElements = DOMUtil.getElementsByTagName(doc, ELEMENT_NAME_TOPIC_REPLACE);
				for (int i=0;i<topicReplaceElements.length;++i) {
					UATopicReplace replace = parseTopicReplace(pluginId, topicReplaceElements[i]);
					if (replace != null) {
						list.add(replace);
					}
				}
				return list;
			}
			catch (IOException e) {
				String msg = "Error reading topic extension file " + file + " in plug-in " + pluginId; //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg, null);
			}
		}
		else {
			String msg = "Unable to find topic extension file " + file + " in plug-in " + pluginId; //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, null);
		}
		return Collections.EMPTY_LIST;
	}
	
	private UATopicExtension parseTopicExtension(String pluginId, Element elem) {
		String content = elem.getAttribute(ATTRIBUTE_NAME_CONTENT);
		String path = elem.getAttribute(ATTRIBUTE_NAME_PATH);
		if (content == null) {
			String msg = "Required attribute " + ATTRIBUTE_NAME_CONTENT + " missing from " + ELEMENT_NAME_TOPIC_EXTENSION + " element for extension point " + EXTENSION_POINT_ID_CONTENT_EXTENSION + " in plug-in " + pluginId; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			HelpPlugin.logError(msg, null);
			return null;
		}
		if (path == null) {
			String msg = "Required attribute " + ATTRIBUTE_NAME_PATH + " missing from " + ELEMENT_NAME_TOPIC_EXTENSION + " element for extension point " + EXTENSION_POINT_ID_CONTENT_EXTENSION + " in plug-in " + pluginId; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			HelpPlugin.logError(msg, null);
			return null;
		}
		int lastSlashIndex = path.lastIndexOf('/');
		if (lastSlashIndex > 0) {
			String targetHref = '/' + path.substring(0, lastSlashIndex);
			String targetAnchorId = path.substring(lastSlashIndex + 1);
			String contentHref = '/' + pluginId + '/' + content;
			String contentElementId = null;
			return new UATopicExtension(targetHref, targetAnchorId, contentHref, contentElementId);
		}
		return null;
	}

	private UATopicReplace parseTopicReplace(String pluginId, Element elem) {
		String content = elem.getAttribute(ATTRIBUTE_NAME_CONTENT);
		String path = elem.getAttribute(ATTRIBUTE_NAME_PATH);
		if (content == null) {
			String msg = "Required attribute " + ATTRIBUTE_NAME_CONTENT + " missing from " + ELEMENT_NAME_TOPIC_REPLACE + " element for extension point " + EXTENSION_POINT_ID_CONTENT_EXTENSION + " in plug-in " + pluginId; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			HelpPlugin.logError(msg, null);
			return null;
		}
		if (path == null) {
			String msg = "Required attribute " + ATTRIBUTE_NAME_PATH + " missing from " + ELEMENT_NAME_TOPIC_REPLACE + " element for extension point " + EXTENSION_POINT_ID_CONTENT_EXTENSION + " in plug-in " + pluginId; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			HelpPlugin.logError(msg, null);
			return null;
		}
		int lastSlashIndex = path.lastIndexOf('/');
		if (lastSlashIndex > 0) {
			String targetHref = '/' + path.substring(0, lastSlashIndex);
			String targetElementId = path.substring(lastSlashIndex + 1);
			int numberSignIndex = content.indexOf('#');
			String contentHref = '/' + pluginId + '/' + ((numberSignIndex > 0) ? content.substring(0, numberSignIndex) : content);
			String contentElementId = (numberSignIndex > 0) ? content.substring(numberSignIndex + 1) : null;
			return new UATopicReplace(targetHref, targetElementId, contentHref, contentElementId);
		}
		return null;
	}
	
	/*
	 * Clears all cached content, forcing it to query for everything
	 * again next time around.
	 */
	private void clearCache() {
		topicExtensions = null;
		topicExtensionsByTargetHref = null;
		topicReplaces = null;
		topicReplacesByTargetHref = null;
	}
}
