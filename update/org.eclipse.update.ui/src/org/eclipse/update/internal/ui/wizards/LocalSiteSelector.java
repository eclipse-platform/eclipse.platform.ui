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
package org.eclipse.update.internal.ui.wizards;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;

/**
 */
public class LocalSiteSelector {

	/**
	 * 
	 */
	static String lastLocation = null;
	public LocalSiteSelector() {
		super();
	}
	public static SiteBookmark getLocaLSite(Shell parent) {
		DirectoryDialog dialog = new DirectoryDialog(parent);
		dialog.setMessage(
			UpdateUI.getString("LocalSiteSelector.dialogMessage")); //$NON-NLS-1$
		dialog.setFilterPath(lastLocation);
		String dir = dialog.open();

		SiteBookmark siteBookmark = null;
		while (dir != null && siteBookmark == null) {
			File dirFile = new File(dir);
			if (isDirSite(dirFile)) {
				siteBookmark = createDirSite(dirFile);
				lastLocation = dir;
			} else {
				MessageDialog.openInformation(
					parent,
					UpdateUI.getString("LocalSiteSelector.dirInfoTitle"), //$NON-NLS-1$
					UpdateUI.getString("LocalSiteSelector.dirInfoMessage")); //$NON-NLS-1$
				dialog.setFilterPath(dir);
				dir = dialog.open();
			}
		}
		return siteBookmark;
	}
	public static SiteBookmark getLocaLZippedSite(Shell parent) {
		FileDialog dialog = new FileDialog(parent);
		dialog.setText(
			UpdateUI.getString("LocalSiteSelector.dialogMessagezip")); //$NON-NLS-1$
		//dialog.setFilterExtensions(new String[] { "*.zip", "*.jar" });
		// //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] { "*.jar;*.zip" }); //$NON-NLS-1$

		SiteBookmark siteBookmark = null;

		String zip = dialog.open();
		while (zip != null && siteBookmark == null) {
			File zipF = new File(zip);
			if (isZipSite(zipF)) {
				siteBookmark = createZipSite(zipF);
			} else {
				MessageDialog.openInformation(
					parent,
					UpdateUI.getString("LocalSiteSelector.zipInfoTitle"), //$NON-NLS-1$
					UpdateUI.getString("LocalSiteSelector.zipInfoMessage")); //$NON-NLS-1$
				zip = dialog.open();
			}
		}
		return siteBookmark;
	}
	/**
	 * Returns true the zip file contains an update site
	 * 
	 * @param file
	 * @return
	 */
	static boolean isZipSite(File file) {
		if (!file.getName().toLowerCase().endsWith(".zip") //$NON-NLS-1$
			&& !file.getName().toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
			return false;
		}

		ZippedSiteValidator validator = new ZippedSiteValidator(file);
		BusyIndicator.showWhile(
			UpdateUI.getActiveWorkbenchShell().getDisplay(),
			validator);
		return validator.isValid();
	}

	/**
	 * Returns true if the specified dir contains an update site
	 * 
	 * @param dir
	 * @return
	 */
	static boolean isDirSite(File dir) {
		File siteXML = new File(dir, "site.xml"); //$NON-NLS-1$
		File featuresDir = new File(dir, "features"); //$NON-NLS-1$
		File pluginsDir = new File(dir, "plugins"); //$NON-NLS-1$
		return siteXML.exists()
			|| featuresDir.exists()
			&& featuresDir.isDirectory()
			&& pluginsDir.exists()
			&& pluginsDir.isDirectory();
	}

	/**
	 * Creates a bookmark to a zipped site
	 * 
	 * @param file
	 * @return
	 */
	static SiteBookmark createZipSite(File file) {
		try {
			URL fileURL = new URL("file", null, file.getAbsolutePath()); //$NON-NLS-1$
			URL url =
				new URL(
					"jar:" //$NON-NLS-1$
						+ fileURL.toExternalForm().replace('\\', '/')
						+ "!/"); //$NON-NLS-1$
			SiteBookmark site = new SiteBookmark(file.getName(), url, false);
			site.setLocal(true);
			return site;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Creates a bookmark to a site on the file system
	 * 
	 * @param file
	 * @return
	 */
	static SiteBookmark createDirSite(File file) {
		try {
			URL url = file.toURL();
			String siteName = file.getAbsolutePath();
			SiteBookmark site = new SiteBookmark(siteName, url, false);
			site.setLocal(true);
			return site;
		} catch (Exception e) {
			return null;
		}
	}
	static class ZippedSiteValidator implements Runnable {
		File file;
		boolean valid = false;
		public ZippedSiteValidator(File file) {
			this.file = file;
		}
		public void run() {
			ZipFile siteZip = null;
			try {
				// check if the zip file contains site.xml
				siteZip = new ZipFile(file);
				if (siteZip.getEntry("site.xml") != null) { //$NON-NLS-1$
					valid = true;
					return;
				}

				boolean hasFeatures = false;
				boolean hasPlugins = false;
				for (Enumeration enum = siteZip.entries();
					enum.hasMoreElements();
					) {
					ZipEntry zEntry = (ZipEntry) enum.nextElement();
					if (!hasFeatures
						&& zEntry.getName().startsWith("features")) { //$NON-NLS-1$
						hasFeatures = true;
					}
					if (!hasPlugins
						&& zEntry.getName().startsWith("plugins")) { //$NON-NLS-1$
						hasPlugins = true;
					}
					if (hasFeatures && hasPlugins) {
						valid = true;
						return;
					}
				}
			} catch (Exception e) {
			} finally {
				try {
					if (siteZip != null) {
						siteZip.close();
					}
				} catch (IOException ioe) {
				}
			}

		}
		/**
		 * @return Returns the valid. */
		public boolean isValid() {
			return valid;
		}
	}

}
