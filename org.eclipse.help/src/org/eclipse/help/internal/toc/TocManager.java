/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

	/**
	 * Map of ITocNavNode[] by String
	 */
	private Map tocsByLang;

	private Collection contributingPlugins;

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
		if (contributingPlugins == null) {
			getContributedTocFiles(Locale.getDefault().toString());
		}
		return contributingPlugins;
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
	 * Reads product.ini to determine toc ordering. It works in current drivers,
	 * but will not if location/name of product.ini change. Return the list of
	 * href's.
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
		contributingPlugins = new HashSet();
		Collection contributedTocFiles = new ArrayList();
		// find extension point
		IExtensionPoint xpt = Platform.getExtensionRegistry()
				.getExtensionPoint(HelpPlugin.PLUGIN_ID, TOC_XP_NAME);
		if (xpt == null)
			return contributedTocFiles;
		// get all extensions
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			contributingPlugins.add(extensions[i].getNamespace());
			// add to TopicFiles declared in this extension
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++)
				if (configElements[j].getName().equals(TOC_XP_NAME)) {
					String pluginId = configElements[j].getNamespace();
					String href = configElements[j].getAttribute("file"); //$NON-NLS-1$
					boolean isPrimary = "true".equals( //$NON-NLS-1$
							configElements[j].getAttribute("primary")); //$NON-NLS-1$
					String extraDir = configElements[j]
							.getAttribute("extradir"); //$NON-NLS-1$
					if (href != null) {
						contributedTocFiles.add(new TocFile(pluginId, href,
								isPrimary, locale, extraDir));
					}
				}
		}
		return contributedTocFiles;
	}
}