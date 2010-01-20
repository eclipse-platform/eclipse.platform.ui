/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
import org.eclipse.help.ITocContribution;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.util.ResourceLocator;
import org.xml.sax.SAXParseException;

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
	public ITocContribution[] getTocContributions(String locale) {
		List contributions = new ArrayList();
		TocFile[] tocFiles = getTocFiles(locale);
		TocFileParser parser = new TocFileParser();
		for (int i=0;i<tocFiles.length;++i) {
			try {
				ITocContribution toc = parser.parse(tocFiles[i]);
				contributions.add(toc);
			}
			catch (Throwable t) {
				String locationInfo = ""; //$NON-NLS-1$
				if (t instanceof SAXParseException) {
					SAXParseException spe = (SAXParseException) t;
					locationInfo = " at line " + spe.getLineNumber()  //$NON-NLS-1$
					             + ", column " + spe.getColumnNumber(); //$NON-NLS-1$
				}
				String pluginId = tocFiles[i].getPluginId();
				String file = tocFiles[i].getFile();
				String msg = "Error reading help table of contents file /\""  //$NON-NLS-1$
					+ ResourceLocator.getErrorPath(pluginId, file, locale)
					+ locationInfo 
					+ "\" (skipping file)"; //$NON-NLS-1$
				HelpPlugin.logError(msg, t);			
			}
		}
		return (ITocContribution[])contributions.toArray(new ITocContribution[contributions.size()]);
	}

	/*
	 * Returns all available TocFiles for the given locale.
	 */
	protected TocFile[] getTocFiles(String locale) {
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
				boolean primary = "true".equalsIgnoreCase(elem.getAttribute(ATTRIBUTE_NAME_PRIMARY)); //$NON-NLS-1$
				String extradir = elem.getAttribute(ATTRIBUTE_NAME_EXTRADIR);
				String category = elem.getAttribute(ATTRIBUTE_NAME_CATEGORY);
				TocFile tocFile = new TocFile(pluginId, file, primary, locale, extradir, category);
				tocFiles.add(tocFile);
			}
		}
		return (TocFile[])tocFiles.toArray(new TocFile[tocFiles.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractTocProvider#getPriority()
	 */
	public int getPriority() {
		return TOC_FILE_PRIORITY;
	}
}
