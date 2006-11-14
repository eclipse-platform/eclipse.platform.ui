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

import java.io.FileNotFoundException;
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
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.dynamic.ExtensionHandler;
import org.eclipse.help.internal.dynamic.IncludeHandler;
import org.eclipse.help.internal.dynamic.NodeHandler;
import org.eclipse.help.internal.dynamic.NodeProcessor;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.eclipse.help.internal.dynamic.NodeWriter;
import org.eclipse.help.internal.dynamic.ValidationHandler;
import org.eclipse.help.internal.toc.HrefUtil;
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
	
	private NodeProcessor processor;
	private NodeReader reader;
	private NodeWriter writer;
	private Map requiredAttributes;
	
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
			// load the file
			InputStream in = ResourceLocator.openFromPlugin(descriptor.getBundleId(), descriptor.getFile(), locale);
	    	if (in != null) {
				if (reader == null) {
					reader = new NodeReader();
					reader.setIgnoreWhitespaceNodes(true);
				}
				Node root = reader.read(in);
				if ("contexts".equals(root.getNodeName())) { //$NON-NLS-1$
					// process dynamic content
					if (processor == null) {
						processor = new NodeProcessor(new NodeHandler[] {
							new ValidationHandler(getRequiredAttributes()),
							new NormalizeHandler(),
							new IncludeHandler(reader, locale),
							new ExtensionHandler(reader, locale)
						});
					}
					processor.process(root, '/' + descriptor.getBundleId() + '/' + descriptor.getFile());
					
					// build map
					Node[] children = root.getChildNodes();
					Map contexts = new HashMap();
					for (int i=0;i<children.length;++i) {
						if (ELEMENT_CONTEXT.equals(children[i].getNodeName())) {
							Context context = children[i] instanceof Context ? (Context)children[i] : new Context(children[i]);
							String id = context.getId();
							if (id != null) {
								contexts.put(id, context);
							}
						}
					}
					return contexts;
				}
				else {
					String msg = "Required root element \"contexts\" missing from context-sensitive help file \"/" + descriptor.getBundleId() + '/' + descriptor.getFile() + "\" (skipping)"; //$NON-NLS-1$ //$NON-NLS-2$
					HelpPlugin.logError(msg);
				}
	    	}
	    	else {
	    		throw new FileNotFoundException();
	    	}
		}
		catch (Throwable t) {
			String msg = "Error reading context-sensitive help file /\"" + descriptor.getBundleId() + '/' + descriptor.getFile() + "\" (skipping file)"; //$NON-NLS-1$ //$NON-NLS-2$
			HelpPlugin.logError(msg, t);
		}
		return null;
	}
	
	private Map getRequiredAttributes() {
		if (requiredAttributes == null) {
			requiredAttributes = new HashMap();
			requiredAttributes.put(Context.NAME, new String[] { Context.ATTRIBUTE_ID });
			requiredAttributes.put(Topic.NAME, new String[] { Topic.ATTRIBUTE_LABEL, Topic.ATTRIBUTE_HREF });
			requiredAttributes.put("anchor", new String[] { "id" }); //$NON-NLS-1$ //$NON-NLS-2$
			requiredAttributes.put("include", new String[] { "path" }); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return requiredAttributes;
	}
	
	/*
	 * Handler that normalizes:
	 * 1. Descriptions - any child elements like bold tags are serialized and inserted into the
	 *    text node under the description element.
	 * 2. Related topic hrefs - convert from relative (e.g. "path/file.html") to absolute hrefs
	 *    (e.g. "/plugin.id/path/file.html"). 
	 */
	private class NormalizeHandler extends NodeHandler {
		public short handle(Node node, String id) {
			if (Context.NAME.equals(node.getNodeName())) {
				Context context = node instanceof Context ? (Context)node : new Context(node);
				Node[] children = context.getChildNodes();
				if (children.length > 0 && Context.ELEMENT_DESCRIPTION.equals(children[0].getNodeName())) {
					Node description = children[0];
					Node[] descriptionChildren = description.getChildNodes();
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
			else if (Topic.NAME.equals(node.getNodeName())) {
				String href = node.getAttribute(Topic.ATTRIBUTE_HREF);
				if (href != null) {
					int index = id.indexOf('/', 1);
					if (index != -1) {
						String pluginId = id.substring(1, index);
						node.setAttribute(Topic.ATTRIBUTE_HREF, HrefUtil.normalizeHref(pluginId, href));
					}
				}
			}
			// give other handlers an opportunity to process
			return UNHANDLED;
		}
	}
}
