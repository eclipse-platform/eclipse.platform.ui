/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.model.*;

/**
 * Manages the navigation model. It keeps track of all the tables of contents.
 */
public class TocManager {
	public static final String TOC_XP_NAME = "toc"; //$NON-NLS-1$
	public static final String TOC_ELEMENT_NAME = "toc"; //$NON-NLS-1$
	public static final String INDEX_ELEMENT_NAME = "index"; //$NON-NLS-1$

	/**
	 * Map of ITocNavNode[] by String
	 */
	private Map tocsByLang;

	/**
	 * Map of plugin ID (String) to index path (String)
	 */
	private Map contributingPlugins2IndexPaths;

	/**
	 * HelpNavigationManager constructor.
	 */
	public TocManager() {
		super();
		try {
			tocsByLang = new HashMap();
			// build TOCs for machine locale at startup
			// Note: this can be removed, and build on first invocation...
			build(Platform.getNL());
		} catch (Exception e) {
			HelpPlugin.logError("", e); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the list of TOC's available in the help system
	 */
	public ITocElement[] getTocs(String locale) {

		if (locale == null)
			return new ITocElement[0];

		ITocElement[] tocs = (ITocElement[]) tocsByLang.get(locale);
		if (tocs == null) {
			synchronized (this) {
				if (tocs == null) {
					build(locale);
				}
			}
			tocs = (ITocElement[]) tocsByLang.get(locale);
			// one more sanity test...
			if (tocs == null)
				tocs = new ITocElement[0];
		}
		return tocs;
	}

	/**
	 * Returns the navigation model for specified toc
	 */
	public ITocElement getToc(String href, String locale) {
		if (href == null || href.equals("")) //$NON-NLS-1$
			return null;
		ITocElement[] tocs = getTocs(locale);

		for (int i = 0; i < tocs.length; i++) {
			if (tocs[i].getHref().equals(href))
				return tocs[i];
		}
		return null;
	}

	/**
	 * Returns the list of contributing Bundle IDs
	 */
	public Collection getContributingPlugins() {
		if (contributingPlugins2IndexPaths == null) {
			getContributedTocFiles(Locale.getDefault().toString());
		}
		return contributingPlugins2IndexPaths.keySet();
	}

	/**
	 * Returns the index path for a given plugin ID
	 * @return String or null
	 */
	public String getIndexPath(String pluginId) {
		if (contributingPlugins2IndexPaths == null) {
			getContributedTocFiles(Locale.getDefault().toString());
		}
		return (String) contributingPlugins2IndexPaths.get(pluginId);
	}

	/**
	 * Builds the toc from the contribution files
	 */
	private void build(String locale) {
		IToc[] tocs;
		try {
			Collection contributedTocFiles = getContributedTocFiles(locale);
			TocBuilder builder = new TocBuilder();
			builder.build(contributedTocFiles);
			Collection builtTocs = builder.getBuiltTocs();
			tocs = new ITocElement[builtTocs.size()];
			int i = 0;
			for (Iterator it = builtTocs.iterator(); it.hasNext();) {
				tocs[i++] = (ITocElement) it.next();
			}
			List orderedTocs = orderTocs(builtTocs);
			tocs = new ITocElement[orderedTocs.size()];
			orderedTocs.toArray(tocs);
		} catch (Exception e) {
			tocs = new IToc[0];
			HelpPlugin.logError("", e); //$NON-NLS-1$
		}
		tocsByLang.put(locale, tocs);
	}

	/**
	 * Orders the TOCs according to a product wide preference.
	 */
	private List orderTocs(Collection unorderedTocs) {
		ArrayList orderedHrefs = getPreferredTocOrder();
		ArrayList orderedTocs = new ArrayList(unorderedTocs.size());

		// add the tocs from the preferred order...
		for (Iterator it = orderedHrefs.iterator(); it.hasNext();) {
			String href = (String) it.next();
			ITocElement toc = getToc(unorderedTocs, href);
			if (toc != null)
				orderedTocs.add(toc);
		}
		// add the remaining tocs
		for (Iterator it = unorderedTocs.iterator(); it.hasNext();) {
			ITocElement toc = (ITocElement) it.next();
			if (!orderedTocs.contains(toc))
				orderedTocs.add(toc);
		}
		return orderedTocs;
	}

	/**
	 * Reads preferences to determine toc ordering.
	 * @return the list of TOC href's.
	 */
	private ArrayList getPreferredTocOrder() {
		ArrayList orderedTocs = new ArrayList();
		try {
			Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
			String preferredTocs = pref.getString(HelpPlugin.BASE_TOCS_KEY);
			if (preferredTocs != null) {
				StringTokenizer suggestdOrderedInfosets = new StringTokenizer(
						preferredTocs, " ;,"); //$NON-NLS-1$

				while (suggestdOrderedInfosets.hasMoreElements()) {
					orderedTocs.add(suggestdOrderedInfosets.nextElement());
				}
			}
		} catch (Exception e) {
			HelpPlugin.logError(
					"Problems occurred reading plug-in preferences.", e); //$NON-NLS-1$
		}
		return orderedTocs;
	}

	/**
	 * Reads preferences to determine TOCs to be ignored.
	 * @return the list of TOC href's.
	 */
	private Collection getIgnoredTocs() {
		HashSet ignored = new HashSet();
		try {
			Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
			String preferredTocs = pref.getString(HelpPlugin.IGNORED_TOCS_KEY);
			if (preferredTocs != null) {
				StringTokenizer suggestdOrderedInfosets = new StringTokenizer(
						preferredTocs, " ;,"); //$NON-NLS-1$

				while (suggestdOrderedInfosets.hasMoreElements()) {
					ignored.add(suggestdOrderedInfosets.nextElement());
				}
			}
		} catch (Exception e) {
			HelpPlugin.logError(
					"Problems occurred reading plug-in preferences.", e); //$NON-NLS-1$
		}
		return ignored;
	}
	/**
	 * Returns the toc from a list of IToc by identifying it with its (unique)
	 * href.
	 */
	private ITocElement getToc(Collection list, String href) {
		for (Iterator it = list.iterator(); it.hasNext();) {
			ITocElement toc = (ITocElement) it.next();
			if (toc.getHref().equals(href))
				return toc;
		}
		return null;
	}

	/**
	 * Returns a collection of TocFile that were not processed.
	 */
	protected Collection getContributedTocFiles(String locale) {
		contributingPlugins2IndexPaths = new HashMap();
		Collection contributedTocFiles = new ArrayList();
		Collection ignored = getIgnoredTocs();
		// find extension point
		IExtensionPoint xpt = Platform.getExtensionRegistry()
				.getExtensionPoint(HelpPlugin.PLUGIN_ID, TOC_XP_NAME);
		if (xpt == null)
			return contributedTocFiles;
		// get all extensions
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			String pluginId = extensions[i].getNamespace();
			if(!contributingPlugins2IndexPaths.containsKey(pluginId))
				contributingPlugins2IndexPaths.put(pluginId, null);
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++){
				if (configElements[j].getName().equals(TOC_ELEMENT_NAME)) {
					// add to TocFiles declared in this extension
					String href = configElements[j].getAttribute("file"); //$NON-NLS-1$
					if (href == null
							|| ignored.contains("/" + pluginId + "/" + href)) { //$NON-NLS-1$ //$NON-NLS-2$
						continue;
					}
					boolean isPrimary = "true".equals( //$NON-NLS-1$
							configElements[j].getAttribute("primary")); //$NON-NLS-1$
					String extraDir = configElements[j]
							.getAttribute("extradir"); //$NON-NLS-1$
					contributedTocFiles.add(new TocFile(pluginId, href,
							isPrimary, locale, extraDir));
				} else 	if (configElements[j].getName().equals(INDEX_ELEMENT_NAME)) {
					// add to index paths declared in this extension
					String path = configElements[j].getAttribute("path"); //$NON-NLS-1$
					if (path == null
							|| path.length()==0) {
						continue;
					}
					// override entry map entry with new one, only one index path per plugin allowed
					contributingPlugins2IndexPaths.put(pluginId, path);
				} 
			}
		}
		return contributedTocFiles;
	}
}
