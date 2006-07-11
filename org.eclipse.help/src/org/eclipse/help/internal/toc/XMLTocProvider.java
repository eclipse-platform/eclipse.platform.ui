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
package org.eclipse.help.internal.toc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITocProvider;
import org.eclipse.help.internal.HelpPlugin;

/*
 * The ITocProvider that reads the toc XML files and provides the content to
 * the help system. Referenced from extension in plugin.xml.
 */
public class XMLTocProvider implements ITocProvider {

	public static final String EXTENSION_POINT_ID_TOC = HelpPlugin.PLUGIN_ID + ".toc"; //$NON-NLS-1$
	public static final String ELEMENT_NAME_TOC = "toc"; //$NON-NLS-1$
	public static final String ELEMENT_NAME_INDEX = "index"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_FILE = "file"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_PRIMARY = "primary"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_EXTRADIR = "extradir"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_CATEGORY = "category"; //$NON-NLS-1$
	
	private static XMLTocProvider instance;
	private Map contributingPlugins2IndexPaths;
	
	/*
	 * Returns the instance if one was created already. If not, returns null.
	 * This is because it's a singleton but instantiated from plugin.xml.
	 */
	public static XMLTocProvider getInstance() {
		return instance;
	}
	
	/*
	 * Constructs a new instance. Check if there's an instance created
	 * before creating a new one.
	 */
	public XMLTocProvider() {
		instance = this;
	}

	/*
	 * Returns the ids of all the plugins that contribute indexes. Must load
	 * tocs before calling this.
	 */
	public Collection getContributingPlugins() {
		return contributingPlugins2IndexPaths.keySet();
	}

	/*
	 * Returns the path to the index for the given plugin. Must load tocs before
	 * calling this.
	 */
	public String getIndexPath(String pluginId) {
		return (String)contributingPlugins2IndexPaths.get(pluginId);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.help.ITocProvider#getTocContributions(java.lang.String)
	 */
	public ITocContribution[] getTocContributions(String locale) {
		List tocFiles = getTocFiles(locale);
		TocBuilder builder = new TocBuilder();
		builder.build(tocFiles);
		List builtTocs = builder.getBuiltTocs();
		List contributions = new ArrayList();
		Iterator iter = builtTocs.iterator();
		while (iter.hasNext()) {
			Toc toc = (Toc)iter.next();
			contributions.add(new XMLTocContribution(toc));
		}
		return (ITocContribution[])contributions.toArray(new ITocContribution[contributions.size()]);
	}
	
	/*
	 * Returns all TocFiles for the given locale. Also populates the
	 * contributingPlugins2IndexPaths map.
	 */
	private List getTocFiles(String locale) {
		contributingPlugins2IndexPaths = new HashMap();
		List tocFiles = new ArrayList();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_POINT_ID_TOC);
		for (int i=0;i<elements.length;++i) {
			IConfigurationElement elem = elements[i];
			String pluginId;
			try {
				pluginId = elem.getNamespaceIdentifier();
			}
			catch (InvalidRegistryObjectException e) {
				// no longer valid; skip it
				continue;
			}

			if (elem.getName().equals(ELEMENT_NAME_TOC)) {
				String file = elem.getAttribute(ATTRIBUTE_NAME_FILE);
				boolean primary = Boolean.toString(true).equals(elem.getAttribute(ATTRIBUTE_NAME_PRIMARY));
				String extradir = elem.getAttribute(ATTRIBUTE_NAME_EXTRADIR);
				String category = elem.getAttribute(ATTRIBUTE_NAME_CATEGORY);
				
				if (file == null) {
					// log and skip
					String msg = ELEMENT_NAME_TOC + " element for extension point " + EXTENSION_POINT_ID_TOC + " must specify a " + ATTRIBUTE_NAME_FILE + " attribute"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					try {
						msg += " (declared from plug-in " + elem.getNamespaceIdentifier() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					catch (InvalidRegistryObjectException e) {
						// skip the declaring plugin part
					}
					HelpPlugin.logError(msg, null);
					continue;
				}
				
				if(!contributingPlugins2IndexPaths.containsKey(pluginId)) {
					contributingPlugins2IndexPaths.put(pluginId, null);
				}

				TocFile tocFile = new TocFile(pluginId, file, primary, locale, extradir, category);
				tocFiles.add(tocFile);
			}
			else if (elem.getName().equals(ELEMENT_NAME_INDEX)) {
				// add to index paths declared in this extension
				String path = elem.getAttribute("path"); //$NON-NLS-1$
				if (path == null || path.length()==0) {
					continue;
				}
				// override entry map entry with new one, only one index path per plugin allowed
				contributingPlugins2IndexPaths.put(pluginId, path);
			} 
		}
		return tocFiles;
	}
	
	/*
	 * An ITocContribution from a Toc created from a toc XML file.
	 */
	private static class XMLTocContribution implements ITocContribution {
		
		private String categoryId;
		private String id;
		private String locale;
		private Toc toc;
		
		public XMLTocContribution(Toc toc) {
			this.toc = toc;
			id = toc.getHref();
			categoryId = toc.getTocFile().getCategoryId();
			locale = toc.getTocFile().getLocale();
		}
		
		public String getCategoryId() {
			return categoryId;
		}
		
		public String getId() {
			return id;
		}
		
		public String getLocale() {
			return locale;
		}
		
		public IToc getToc() {
			return toc;
		}
	}
}
