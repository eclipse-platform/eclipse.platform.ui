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
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.TocContribution;
import org.eclipse.help.internal.HelpPlugin;

/*
 * Provides toc data from toc XML files to the help system.
 */
public class TocFileProvider extends AbstractTocProvider {

	public static final String EXTENSION_POINT_ID_TOC = HelpPlugin.PLUGIN_ID + ".toc"; //$NON-NLS-1$
	public static final String ELEMENT_NAME_TOC = "toc"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_FILE = "file"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_PRIMARY = "primary"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_EXTRADIR = "extradir"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_CATEGORY = "category"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractTocProvider#getTocContributions(java.lang.String)
	 */
	public TocContribution[] getTocContributions(String locale) {
		List contributions = new ArrayList();
		TocFile[] tocFiles = getTocFiles(locale);
		TocFileParser parser = new TocFileParser();
		for (int i=0;i<tocFiles.length;++i) {
			try {
				TocContribution toc = parser.parse(tocFiles[i]);
				contributions.add(toc);
			}
			catch (Throwable t) {
				String msg = "Error reading toc file \"" + tocFiles[i].getFile() + "\" in extension specified in plug-in: " + tocFiles[i].getPluginId(); //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg, t);
			}
		}
		return (TocContribution[])contributions.toArray(new TocContribution[contributions.size()]);
	}

	/*
	 * Returns all available TocFiles for the given locale.
	 */
	private TocFile[] getTocFiles(String locale) {
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
				
				TocFile tocFile = new TocFile(pluginId, file, primary, locale, extradir, category);
				tocFiles.add(tocFile);
			}
		}
		return (TocFile[])tocFiles.toArray(new TocFile[tocFiles.size()]);
	}
}
