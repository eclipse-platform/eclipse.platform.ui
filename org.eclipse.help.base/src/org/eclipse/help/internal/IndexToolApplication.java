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
package org.eclipse.help.internal;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;

/**
 * application org.eclipse.help.indexTool
 */
public class IndexToolApplication
	implements IPlatformRunnable, IExecutableExtension {

	/**
	 * Constructor for IndexToolApplication.
	 */
	public IndexToolApplication() {
		super();
	}

	/**
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(
		IConfigurationElement config,
		String propertyName,
		Object data)
		throws CoreException {
	}

	/**
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		try {
			String directory = System.getProperty("indexOutput");
			if (directory == null || directory.length() == 0) {
				throw new Exception("indexOutput property is not set.");
			}
			String localeStr = System.getProperty("indexLocale");
			if (localeStr == null || localeStr.length() < 2) {
				throw new Exception("indexLocale property is not set.");
			}
			Locale locale;
			if (localeStr.length() >= 5) {
				locale =
					new Locale(
						localeStr.substring(0, 2),
						localeStr.substring(3, 5));
			} else {
				locale = new Locale(localeStr.substring(0, 2), "");
			}
			preindex(directory, locale);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			HelpBasePlugin.logError("Preindexing failed", e);
		}
		return EXIT_OK;
	}

	private void preindex(String outputDir, Locale locale) throws Exception {
		String helpStatePath =
			HelpBasePlugin.getDefault().getStateLocation().toOSString();
		String relIndexPath = "nl" + File.separator + locale.toString();
		File indexPath =
			new File(helpStatePath + File.separator + relIndexPath);
		// clean
		if (indexPath.exists()) {
			delete(indexPath);
		}
		// index
		HelpSystem.getSearchManager().updateIndex(
			new NullProgressMonitor(),
			HelpSystem.getSearchManager().getIndex(locale.toString()));
		// zip up
		File d =
			new File(outputDir, "nl" + File.separator + locale.getLanguage());
		if (locale.getCountry().length() > 0) {
			d = new File(d, locale.getCountry());
		}
		if (!d.exists())
			d.mkdirs();
		ZipOutputStream zout =
			new ZipOutputStream(
				new FileOutputStream(new File(d, "doc_index.zip")));
		try {
			zipDirectory(indexPath, zout, null);
		} finally {
			zout.close();
		}
	}
	/**
	 * Recursively deletes directory and files.
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
			throw new IOException("Cannot delete file " + file);
		}
	}
	/**
	 * Adds files in a directory to a zip stream
	 * @param dir directory with files to zip
	 * @param zout ZipOutputStream
	 * @param base directory prefix for file entries inside the zip or null
	 * @throws Exception
	 */
	private static void zipDirectory(
		File dir,
		ZipOutputStream zout,
		String base)
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
				path = base + "/" + files[i];
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
