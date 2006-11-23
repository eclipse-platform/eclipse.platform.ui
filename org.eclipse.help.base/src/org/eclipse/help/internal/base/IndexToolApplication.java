/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.search.PluginVersionInfo;
import org.eclipse.help.internal.search.SearchIndex;
import org.eclipse.help.internal.search.SearchIndexWithIndexingProgress;
import org.eclipse.osgi.util.NLS;

/*
 * An Eclipse application used for pre-indexing user assistance documentation (help,
 * welcome, cheatsheets, etc) in order to avoid the initial search performance penalty
 * at the cost of disk space.
 * 
 * This can be used to either index content for an entire product, or for a specific
 * plug-in.
 * 
 * Accepted arguments:
 * 
 * - indexOutput: A full path to the directory to store the generated index.
 * - indexLocale: The locale (e.g. "en" or "en_US") of the indexed content.
 * - indexPlugin (optional): The id of the plug-in to index, e.g. "org.eclipse.platform.doc.user".
 *      If not specified, assumes all plug-ins.
 */
public class IndexToolApplication implements IPlatformRunnable, IExecutableExtension {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		try {
			String directory = System.getProperty("indexOutput"); //$NON-NLS-1$
			if (directory == null || directory.length() == 0) {
				throw new Exception(NLS.bind(HelpBaseResources.IndexToolApplication_propertyNotSet, "indexOutput")); //$NON-NLS-1$
			}
			String localeStr = System.getProperty("indexLocale"); //$NON-NLS-1$
			if (localeStr == null) {
				localeStr = Platform.getNL();
			}
			Locale locale;
			if (localeStr.length() >= 5) {
				locale = new Locale(localeStr.substring(0, 2), localeStr.substring(3, 5));
			}
			else {
				locale = new Locale(localeStr.substring(0, 2), ""); //$NON-NLS-1$
			}
			preindex(directory, locale);
		} catch (Exception e) {
			e.printStackTrace();
			HelpBasePlugin.logError("Preindexing failed.", e); //$NON-NLS-1$
		}
		return EXIT_OK;
	}

	private void preindex(String outputDir, Locale locale) throws Exception {
		// clean
		File indexPath = new File(HelpBasePlugin.getConfigurationDirectory(), "index/" + locale); //$NON-NLS-1$
		if (indexPath.exists()) {
			delete(indexPath);
		}
		
		// index
		SearchIndexWithIndexingProgress index = BaseHelpSystem.getLocalSearchManager().getIndex(locale.toString());
		String indexPlugin = System.getProperty("indexPlugin"); //$NON-NLS-1$
		if (indexPlugin != null) {
			List bundleIds = Arrays.asList(new String[] { indexPlugin });
			PluginVersionInfo docPlugins = new PluginVersionInfo(SearchIndex.INDEXED_CONTRIBUTION_INFO_FILE, bundleIds, indexPath, true);
			index.setDocPlugins(docPlugins);
		}
		BaseHelpSystem.getLocalSearchManager().ensureIndexUpdated(new NullProgressMonitor(), index);

		// package
		if (indexPlugin == null) {
			outputDir += File.separator + "nl" + File.separator + locale.getLanguage(); //$NON-NLS-1$
			if (locale.getCountry().length() > 0) {
				outputDir += File.separator + locale.getCountry();
			}
			zipDirectory(indexPath, outputDir);
		}
		else {
			copyDirectory(indexPath, outputDir);
		}
	}

	/**
	 * Recursively deletes directory and files.
	 * 
	 * @param file
	 * @throws IOException
	 */
	private static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			File files[] = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				delete(files[i]);
			}
		}
		if (!file.delete()) {
			throw new IOException(
					NLS.bind(HelpBaseResources.IndexToolApplication_cannotDelete, file.getAbsolutePath()));
		}
	}

	private static void zipDirectory(File srcDir, String destPath) throws IOException {
		File destDir = new File(destPath);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File(destDir, "doc_index.zip"))); //$NON-NLS-1$
		try {
			zipDirectory(srcDir, zout, null);
		}
		finally {
			zout.close();
		}
	}

	private static void zipDirectory(File dir, ZipOutputStream zout, String base)
			throws IOException {
		byte buffer[] = new byte[8192];
		String[] files = dir.list();
		if (files == null || files.length == 0)
			return;
		for (int i = 0; i < files.length; i++) {
			String path;
			if (base == null) {
				path = files[i];
			} else {
				path = base + "/" + files[i]; //$NON-NLS-1$
			}
			File f = new File(dir, files[i]);
			if (f.isDirectory())
				zipDirectory(f, zout, path);
			else {
				ZipEntry zentry = new ZipEntry(path);
				zout.putNextEntry(zentry);
				FileInputStream inputStream = new FileInputStream(f);
				int len;
				while ((len = inputStream.read(buffer)) != -1)
					zout.write(buffer, 0, len);
				inputStream.close();
				zout.flush();
				zout.closeEntry();
			}
		}
	}

	private static void copyDirectory(File src, String outputDir) throws IOException {
		byte buffer[] = new byte[8192];
		File dest = new File(outputDir);
		if (dest.exists()) {
			delete(dest);
		}
		dest.mkdirs();
		
		String[] files = src.list();
		if (files != null) {
			for (int i=0;i<files.length;++i) {
				File srcFile = new File(src, files[i]);
				File destFile = new File(dest, files[i]);
				FileInputStream in = new FileInputStream(srcFile);
				FileOutputStream out = new FileOutputStream(destFile);
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				in.close();
				out.close();
			}
		}
	}
}
