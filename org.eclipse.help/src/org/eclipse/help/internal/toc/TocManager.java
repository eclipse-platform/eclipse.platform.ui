/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.help.IToc;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.Resources;

/**
 * Manages the navigation model. It keeps track of all the tables of contents.
 */
public class TocManager {

	/**
	 * Map of IToc[] by String
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
			build(BootLoader.getNL());
		} catch (Exception e) {
			HelpPlugin.logError("", e);
		}
	}

	/**
	 * Returns the list of TOC's available in the help system
	 */
	public IToc[] getTocs(String locale) {

		if (locale == null)
			return new IToc[0];

		IToc[] tocs = (IToc[]) tocsByLang.get(locale);
		if (tocs == null) {
			build(locale);
			tocs = (IToc[]) tocsByLang.get(locale);
			// one more sanity test...
			if (tocs == null)
				tocs = new IToc[0];
		}
		return tocs;
	}

	/**
	 * Returns the navigation model for specified toc
	 */
	public IToc getToc(String href, String locale) {
		if (href == null || href.equals(""))
			return null;
		IToc[] tocs = getTocs(locale);

		for (int i = 0; i < tocs.length; i++) {
			if (tocs[i].getHref().equals(href))
				return tocs[i];
		}
		return null;
	}

	/**
	 * Returns the list of contributing plugins
	 */
	public Collection getContributingPlugins() {
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
			tocs = new IToc[builtTocs.size()];
			int i = 0;
			for (Iterator it = builtTocs.iterator(); it.hasNext();) {
				tocs[i++] = (IToc) it.next();
			}
			Collection orderedTocs = orderTocs(builtTocs);
			tocs = new IToc[orderedTocs.size()];
			orderedTocs.toArray(tocs);
		} catch (Exception e) {
			tocs = new IToc[0];
			HelpPlugin.logError("", e);
		}
		tocsByLang.put(locale, tocs);
	}

	/**
	 * Orders the TOCs according to a product wide preference.
	 */
	private Collection orderTocs(Collection unorderedTocs) {
		ArrayList orderedHrefs = getPreferredTocOrder();
		ArrayList orderedTocs = new ArrayList(unorderedTocs.size());

		// add the tocs from the preferred order...
		for (Iterator it = orderedHrefs.iterator(); it.hasNext();) {
			String href = (String) it.next();
			IToc toc = getToc(unorderedTocs, href);
			if (toc != null)
				orderedTocs.add(toc);
		}
		// add the remaining tocs 
		for (Iterator it = unorderedTocs.iterator(); it.hasNext();) {
			IToc toc = (IToc) it.next();
			if (!orderedTocs.contains(toc))
				orderedTocs.add(toc);
		}
		return orderedTocs;
	}

	/**
	 * Reads product.ini to determine toc ordering.
	 * It works in current drivers, but will not
	 * if location/name of product.ini change.
	 * Return the list of href's.
	 */
	private ArrayList getPreferredTocOrder() {
		ArrayList orderedTocs = new ArrayList();
		try {
			Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
			String preferredTocs = pref.getString(HelpSystem.BASE_TOCS_KEY);
			if (preferredTocs != null) {
				StringTokenizer suggestdOrderedInfosets =
					new StringTokenizer(preferredTocs, " ;,");

				while (suggestdOrderedInfosets.hasMoreElements()) {
					orderedTocs.add(suggestdOrderedInfosets.nextElement());
				}
			}
		} catch (Exception e) {
			HelpPlugin.logError(Resources.getString("E039"), e);
		}
		return orderedTocs;
	}

	/**
	 * Returns the toc from a list of IToc by identifying it with its (unique) href.
	 */
	private IToc getToc(Collection list, String href) {
		for (Iterator it = list.iterator(); it.hasNext();) {
			IToc toc = (IToc) it.next();
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
		IExtensionPoint xpt =
			Platform.getPluginRegistry().getExtensionPoint(
				HelpPlugin.PLUGIN_ID,
				"toc");
		if (xpt == null)
			return contributedTocFiles;
		// get all extensions
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			contributingPlugins.add(
				extensions[i].getDeclaringPluginDescriptor());
			// add to TopicFiles declared in this extension
			IConfigurationElement[] configElements =
				extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++)
				if (configElements[j].getName().equals("toc")) {
					String pluginId =
						configElements[j]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getUniqueIdentifier();
					String href = configElements[j].getAttribute("file");
					boolean isPrimary =
						"true".equals(
							configElements[j].getAttribute("primary"));
					String extraDir =
						configElements[j].getAttribute("extradir");
					if (href != null) {
						contributedTocFiles.add(
							new TocFile(
								pluginId,
								href,
								isPrimary,
								locale,
								extraDir));
					}
				}
		}
		return contributedTocFiles;
	}
}
