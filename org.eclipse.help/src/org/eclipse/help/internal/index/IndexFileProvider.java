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
package org.eclipse.help.internal.index;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.AbstractIndexProvider;
import org.eclipse.help.IndexContribution;
import org.eclipse.help.internal.HelpPlugin;

/*
 * Provides index data from index XML files to the help system.
 */
public class IndexFileProvider extends AbstractIndexProvider {

	public static final String EXTENSION_POINT_ID_INDEX = HelpPlugin.PLUGIN_ID + ".index"; //$NON-NLS-1$
	public static final String ELEMENT_NAME_INDEX = "index"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME_FILE = "file"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractIndexProvider#getIndexContributions(java.lang.String)
	 */
	public IndexContribution[] getIndexContributions(String locale) {
		List contributions = new ArrayList();
		IndexFile[] indexFiles = getIndexFiles(locale);
		IndexFileParser parser = new IndexFileParser();
		for (int i=0;i<indexFiles.length;++i) {
			try {
				IndexContribution toc = parser.parse(indexFiles[i]);
				contributions.add(toc);
			}
			catch (Throwable t) {
				String msg = "Error reading index file \"" + indexFiles[i].getFile() + "\" in extension specified in plug-in: " + indexFiles[i].getPluginId(); //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg, null);
			}
		}
		return (IndexContribution[])contributions.toArray(new IndexContribution[contributions.size()]);
	}

	/*
	 * Returns all available IndexFiles for the given locale.
	 */
	private IndexFile[] getIndexFiles(String locale) {
		List indexFiles = new ArrayList();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_POINT_ID_INDEX);
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

			if (elem.getName().equals(ELEMENT_NAME_INDEX)) {
				String file = elem.getAttribute(ATTRIBUTE_NAME_FILE);
				
				if (file == null) {
					// log and skip
					String msg = ELEMENT_NAME_INDEX + " element for extension point " + EXTENSION_POINT_ID_INDEX + " must specify a " + ATTRIBUTE_NAME_FILE + " attribute"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					try {
						msg += " (declared from plug-in " + elem.getNamespaceIdentifier() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					catch (InvalidRegistryObjectException e) {
						// skip the declaring plugin part
					}
					HelpPlugin.logError(msg, null);
					continue;
				}
				
				IndexFile indexFile = new IndexFile(pluginId, file, locale);
				indexFiles.add(indexFile);
			}
		}
		return (IndexFile[])indexFiles.toArray(new IndexFile[indexFiles.size()]);
	}
}
