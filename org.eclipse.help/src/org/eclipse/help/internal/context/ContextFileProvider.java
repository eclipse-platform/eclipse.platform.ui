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
package org.eclipse.help.internal.context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.AbstractContextProvider;
import org.eclipse.help.Node;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.eclipse.help.internal.dynamic.NodeWriter;
import org.eclipse.help.internal.util.ResourceLocator;

/*
 * Provides context-sensitive help data to the help system, contributed from
 * context XML files. 
 */
public class ContextFileProvider extends AbstractContextProvider {

	private static final String EXTENSION_POINT_CONTEXTS = "org.eclipse.help.contexts"; //$NON-NLS-1$
	private static final String ELEMENT_CONTEXT = "context"; //$NON-NLS-1$
	private static final String ELEMENT_CONTEXTS = "contexts"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILE = "file"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PLUGIN = "plugin"; //$NON-NLS-1$
	
	// locale -> Map(pluginId -> Map(shortContextId -> Context)[])
	private Map pluginContextsByLocale;
	
	// pluginId -> ContextFile[]
	private Map descriptorsByPluginId;
	
	// locale -> Map(ContextFile -> Map(shortContextId -> Context))
	private Map contextFilesByLocale;
	
	private NodeReader reader;
	private NodeWriter writer;
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractContextProvider#getContext(java.lang.String, java.lang.String)
	 */
	public Node getContext(String contextId, String locale) {
		int index = contextId.lastIndexOf('.');
		String pluginId = contextId.substring(0, index);
		String shortContextId = contextId.substring(index + 1);
		
		if (pluginContextsByLocale == null) {
			pluginContextsByLocale = new HashMap();
		}
		Map pluginContexts = (Map)pluginContextsByLocale.get(locale);
		if (pluginContexts == null) {
			pluginContexts = new HashMap();
			pluginContextsByLocale.put(locale, pluginContexts);
		}
		Map[] contexts = (Map[])pluginContexts.get(pluginId);
		if (contexts == null) {
			contexts = getPluginContexts(pluginId, locale);
			pluginContexts.put(pluginId, contexts);
		}
		for (int i=0;i<contexts.length;++i) {
			Context context = (Context)contexts[i].get(shortContextId);
			if (context != null) {
				return context;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractContextProvider#getPlugins()
	 */
	public String[] getPlugins() {
		Map associations = getPluginAssociations();
		return (String[])associations.keySet().toArray(new String[associations.size()]);
	}

	/*
	 * Returns a mapping of plug-in IDs to arrays of context files that apply
	 * to that plug-in (pluginId -> ContextFile[]).
	 */
	private Map getPluginAssociations() {
		if (descriptorsByPluginId == null) {
			descriptorsByPluginId = new HashMap();
			IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_CONTEXTS);
			for (int i=0;i<elements.length;++i) {
				if (ELEMENT_CONTEXTS.equals(elements[i].getName())) {
					String declaringPluginId = elements[i].getDeclaringExtension().getContributor().getName();
					String file = elements[i].getAttribute(ATTRIBUTE_FILE);
					if (file != null) {
						String plugin = elements[i].getAttribute(ATTRIBUTE_PLUGIN);
						String targetPluginId = (plugin == null ? declaringPluginId : plugin);
						ContextFile descriptor = new ContextFile(declaringPluginId, file);
						ContextFile[] descriptors = (ContextFile[])descriptorsByPluginId.get(targetPluginId);
						if (descriptors == null) {
							descriptors = new ContextFile[] { descriptor };
						}
						else {
							ContextFile[] temp = new ContextFile[descriptors.length + 1];
							System.arraycopy(descriptors, 0, temp, 0, descriptors.length);
							temp[descriptors.length] = descriptor;
							descriptors = temp;
						}
						descriptorsByPluginId.put(targetPluginId, descriptors);
					}
					else {
						String msg = "Required attribute \"" + ATTRIBUTE_FILE + "\" missing from element \"" + ELEMENT_CONTEXTS + "\" of extension for extension point \"" + EXTENSION_POINT_CONTEXTS + "\" in plug-in \"" + declaringPluginId + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						HelpPlugin.logError(msg, null);
					}
				}
			}
		}
		return descriptorsByPluginId;
	}
	
	/*
	 * Returns the context definitions for the given plug-in and locale,
	 * as a mapping of short IDs to Context objects (shortContextId -> Context).
	 */
	private Map[] getPluginContexts(String pluginId, String locale) {
		List maps = new ArrayList();
		Map associations = getPluginAssociations();
		ContextFile[] descriptors = (ContextFile[])associations.get(pluginId);
		for (int i=0;i<descriptors.length;++i) {
			Map contexts = getContexts(descriptors[i], locale);
			if (contexts != null) {
				maps.add(contexts);
			}
		}
		return (Map[])maps.toArray(new Map[maps.size()]);
	}
	
	/*
	 * Returns the context definitions stored in the given file for the given
	 * locale (shortContextId -> Context).
	 */
	private Map getContexts(ContextFile descriptor, String locale) {
		if (contextFilesByLocale == null) {
			contextFilesByLocale = new HashMap();
		}
		Map contextsByDescriptor = (Map)contextFilesByLocale.get(locale);
		if (contextsByDescriptor == null) {
			contextsByDescriptor = new HashMap();
			contextFilesByLocale.put(locale, contextsByDescriptor);
		}
		Map contexts = (Map)contextsByDescriptor.get(descriptor);
		if (contexts == null) {
			contexts = loadContexts(descriptor, locale);
			if (contexts != null) {
				contextsByDescriptor.put(descriptor, contexts);
			}
		}
		return contexts;
	}
	
	/*
	 * Loads the given context file for the given locale, and returns its
	 * contents as a mapping from short context ids to Context objects
	 * (shortContextId -> Context).
	 */
	private Map loadContexts(ContextFile descriptor, String locale) {
		try {
			InputStream in = ResourceLocator.openFromPlugin(descriptor.getBundleId(), descriptor.getFile(), locale);
			if (reader == null) {
				reader = new NodeReader();
			}
			Node root = reader.read(in);
			Node[] children = root.getChildren();
			Map contexts = new HashMap();
			for (int i=0;i<children.length;++i) {
				if (ELEMENT_CONTEXT.equals(children[i].getName())) {
					Context context = children[i] instanceof Context ? (Context)children[i] : new Context(children[i]);
					normalizeDescription(context);
					String id = context.getId();
					if (id != null) {
						contexts.put(id, context);
					}
				}
			}
			return contexts;
		}
		catch (Throwable t) {
			String msg = "Error loading context-sensitive help file \"" + descriptor.getFile() + "\" in plug-in \"" + descriptor.getBundleId() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			HelpPlugin.logError(msg, t);
			return null;
		}
	}
	
	private void normalizeDescription(Context context) {
		Node[] children = context.getChildren();
		if (children.length > 0 && Context.ELEMENT_DESCRIPTION.equals(children[0].getName())) {
			Node description = children[0];
			Node[] descriptionChildren = description.getChildren();
			if (writer == null) {
				writer = new NodeWriter();
			}
			
			StringBuffer buf = new StringBuffer();
			for (int i=0;i<descriptionChildren.length;++i) {
				writer.write(descriptionChildren[i], buf, false, null, false);
			}
			context.setText(buf.toString());
		}
	}
}
