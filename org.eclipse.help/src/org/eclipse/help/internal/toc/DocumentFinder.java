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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.util.ResourceLocator;
import org.osgi.framework.Bundle;

public class DocumentFinder {
	
	public static String[] collectExtraDocuments(TocFile tocFile) {
		String dir = HrefUtil.normalizeDirectoryHref(tocFile.getPluginId(), tocFile.getExtraDir());
		String locale = tocFile.getLocale();
		
		List result = new ArrayList();
		String pluginID = HrefUtil.getPluginIDFromHref(dir);
		if (pluginID == null) {
			return new String[0];
		}
		Bundle pluginDesc = Platform.getBundle(pluginID);
		if (pluginDesc == null || pluginDesc.getState() == Bundle.INSTALLED
				|| pluginDesc.getState() == Bundle.UNINSTALLED)
			return new String[0];
		String directory = HrefUtil.getResourcePathFromHref(dir);
		if (directory == null) {
			// the root - all files in a zip should be indexed
			directory = ""; //$NON-NLS-1$
		}
		// Find doc.zip file
		IPath iPath = new Path("$nl$/doc.zip"); //$NON-NLS-1$
		Map override = new HashMap(1);
		override.put("$nl$", locale); //$NON-NLS-1$
		URL url = FileLocator.find(pluginDesc, iPath, override);
		if (url == null) {
			url = FileLocator.find(pluginDesc, new Path("doc.zip"), null); //$NON-NLS-1$
		}
		if (url != null) {
			// collect topics from doc.zip file
			result.addAll(collectExtraDocumentsFromZip(pluginID, directory, url));
		}
		
		// Find topics in plugin
		Set paths = ResourceLocator.findTopicPaths(pluginDesc, directory,
				locale);
		for (Iterator it = paths.iterator(); it.hasNext();) {
			String href = "/" + pluginID + "/" + (String) it.next();  //$NON-NLS-1$//$NON-NLS-2$
			href = HrefUtil.normalizeDirectoryPath(href);
			result.add(href);
		}
		return (String[])result.toArray(new String[result.size()]);
	}
	
	private static List collectExtraDocumentsFromZip(String pluginID, String directory,
			URL url) {
		List result = new ArrayList();
		URL realZipURL;
		try {
			realZipURL = FileLocator.toFileURL(FileLocator.resolve(url));
			if (realZipURL.toExternalForm().startsWith("jar:")) { //$NON-NLS-1$
				// doc.zip not allowed in jarred plug-ins.
				return result;
			}
		} catch (IOException ioe) {
			HelpPlugin.logError("IOException occurred, when resolving URL " //$NON-NLS-1$
					+ url.toString() + ".", ioe); //$NON-NLS-1$
			return result;
		}
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(realZipURL.getFile());
			result = createExtraTopicsFromZipFile(pluginID, zipFile, directory);
			zipFile.close();
		} catch (IOException ioe) {
			HelpPlugin.logError(
					"IOException occurred, when accessing Zip file " //$NON-NLS-1$
							+ realZipURL.getFile()
							+ ".  File might not be locally available.", ioe); //$NON-NLS-1$
			return new ArrayList();
		}
		return result;
	}
	
	private static List createExtraTopicsFromZipFile(String pluginID, ZipFile zipFile,
			String directory) {
		String constantHrefSegment = "/" + pluginID + "/"; //$NON-NLS-1$ //$NON-NLS-2$
		List result = new ArrayList();
		for (Enumeration entriesEnum = zipFile.entries(); entriesEnum.hasMoreElements();) {
			ZipEntry zEntry = (ZipEntry) entriesEnum.nextElement();
			if (zEntry.isDirectory()) {
				continue;
			}
			String docName = zEntry.getName();
			int l = directory.length();
			if (l == 0 || docName.length() > l && docName.charAt(l) == '/'
					&& directory.equals(docName.substring(0, l))) {
				String href = constantHrefSegment + docName;
				result.add(href);
			}
		}
		return result;
	}
}
