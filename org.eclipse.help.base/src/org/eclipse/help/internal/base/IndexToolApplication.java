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
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.util.NLS;

/**
 * application org.eclipse.help.indexTool
 */
public class IndexToolApplication implements IApplication {
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public synchronized Object start(IApplicationContext context) throws Exception {
		try {
			String directory = System.getProperty("indexOutput"); //$NON-NLS-1$
			if (directory == null || directory.length() == 0) {
				throw new Exception(NLS.bind(HelpBaseResources.IndexToolApplication_propertyNotSet, "indexOutput")); //$NON-NLS-1$
			}
			String localeStr = System.getProperty("indexLocale"); //$NON-NLS-1$
			if (localeStr == null || localeStr.length() < 2) {
				throw new Exception(NLS.bind(HelpBaseResources.IndexToolApplication_propertyNotSet, "indexLocale")); //$NON-NLS-1$
			}
			Locale locale;
			if (localeStr.length() >= 5) {
				locale = new Locale(localeStr.substring(0, 2), localeStr.substring(3, 5));
			}
			else {
				locale = new Locale(localeStr.substring(0, 2), ""); //$NON-NLS-1$
			}
			preindex(directory, locale);
		}
		catch (Exception e) {
			e.printStackTrace();
			HelpBasePlugin.logError("Preindexing failed.", e); //$NON-NLS-1$
		}
		return EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public synchronized void stop() {
	}
	
	private void preindex(String outputDir, Locale locale) throws Exception {
		File indexPath = new File(HelpBasePlugin.getConfigurationDirectory(),
				"index/" + locale); //$NON-NLS-1$

		// clean
		if (indexPath.exists()) {
			delete(indexPath);
		}
		// index
		BaseHelpSystem.getLocalSearchManager().ensureIndexUpdated(
				new NullProgressMonitor(),
				BaseHelpSystem.getLocalSearchManager().getIndex(locale.toString()));
		// zip up
		File d = new File(outputDir,
				"nl" + File.separator + locale.getLanguage()); //$NON-NLS-1$
		if (locale.getCountry().length() > 0) {
			d = new File(d, locale.getCountry());
		}
		if (!d.exists())
			d.mkdirs();
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(
				new File(d, "doc_index.zip"))); //$NON-NLS-1$
		try {
			zipDirectory(indexPath, zout, null);
		} finally {
			zout.close();
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

	/**
	 * Adds files in a directory to a zip stream
	 * 
	 * @param dir
	 *            directory with files to zip
	 * @param zout
	 *            ZipOutputStream
	 * @param base
	 *            directory prefix for file entries inside the zip or null
	 * @throws IOException
	 */
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
}
