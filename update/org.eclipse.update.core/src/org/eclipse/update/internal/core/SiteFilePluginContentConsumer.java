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
package org.eclipse.update.internal.core;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Plugin Content Consumer on a Site
 */
public class SiteFilePluginContentConsumer extends ContentConsumer {

	private IPluginEntry pluginEntry;
	private ISite site;
	private boolean closed = false;

	// recovery
	private String oldPath;
	private String newPath;

	// for abort
	private List /*of path as String */
	installedFiles;

	/*
	 * Constructor
	 */
	public SiteFilePluginContentConsumer(IPluginEntry pluginEntry, ISite site) {
		this.pluginEntry = pluginEntry;
		this.site = site;
		installedFiles = new ArrayList();
	}

	/*
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		InputStream inStream = null;
		String pluginPath = null;

		if (closed) {
			UpdateCore.warn("Attempt to store in a closed SiteFilePluginContentConsumer", new Exception());
			return;
		}

		try {
			URL newURL = new URL(site.getURL(), Site.DEFAULT_PLUGIN_PATH + pluginEntry.getVersionedIdentifier().toString());
			pluginPath = newURL.getFile(); 
			String contentKey = contentReference.getIdentifier();
			inStream = contentReference.getInputStream();
			pluginPath += pluginPath.endsWith(File.separator) ? contentKey : File.separator + contentKey;

			// error recovery
			if (pluginPath.endsWith("\\plugin.xml") || pluginPath.endsWith("/plugin.xml")) {
				oldPath = pluginPath.replace(File.separatorChar, '/');
				File localFile = new File(oldPath);
				if (localFile.exists()) {
					throw Utilities.newCoreException(Policy.bind("UpdateManagerUtils.FileAlreadyExists", new Object[] { localFile }), null);
				}
				pluginPath = ErrorRecoveryLog.getLocalRandomIdentifier(pluginPath);
				newPath = pluginPath;
				ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.PLUGIN_ENTRY, pluginPath);
			}
			if (pluginPath.endsWith("\\fragment.xml") || pluginPath.endsWith("/fragment.xml")) {
				oldPath = pluginPath.replace(File.separatorChar, '/');
				File localFile = new File(oldPath);
				if (localFile.exists()) {
					throw Utilities.newCoreException(Policy.bind("UpdateManagerUtils.FileAlreadyExists", new Object[] { localFile }), null);
				}
				pluginPath = ErrorRecoveryLog.getLocalRandomIdentifier(pluginPath);
				newPath = pluginPath;
				ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.FRAGMENT_ENTRY, pluginPath);
			}
			UpdateManagerUtils.copyToLocal(inStream, pluginPath, null);
			UpdateManagerUtils.checkPermissions(contentReference, pluginPath); // 20305
			installedFiles.add(pluginPath);
		} catch (IOException e) {
			throw Utilities.newCoreException(Policy.bind("GlobalConsumer.ErrorCreatingFile", pluginPath), e);
			//$NON-NLS-1$
		} finally {
			if (inStream != null) {
				try {
					// close stream
					inStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * @see ISiteContentConsumer#close() 
	 */
	public void close() throws CoreException {

		if (closed) {
			UpdateCore.warn("Attempt to close a closed SiteFilePluginContentConsumer", new Exception());
			return;
		}

		if (newPath != null) {
			// rename file 
			ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.RENAME_ENTRY, newPath);
			File fileToRename = new File(newPath);
			boolean sucess = false;
			if (fileToRename.exists()) {
				File renamedFile = new File(oldPath);
				sucess = fileToRename.renameTo(renamedFile);
			}
			if (!sucess) {
				String msg = Policy.bind("ContentConsumer.UnableToRename", newPath, oldPath);
				throw Utilities.newCoreException(msg, new Exception(msg));
			}
		}

		if (site instanceof SiteFile)
			 ((SiteFile) site).addPluginEntry(pluginEntry);
		closed = true;
	}

	/*
	 * 
	 */
	public void abort() throws CoreException {

		if (closed) {
			UpdateCore.warn("Attempt to abort a closed SiteFilePluginContentConsumer", new Exception());
			return;
		}

		boolean sucess = true;

		// delete plugin.xml first
		if (oldPath != null) {
			ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.DELETE_ENTRY, oldPath);
			File fileToRemove = new File(oldPath);

			if (fileToRemove.exists()) {
				sucess = fileToRemove.delete();
			}
		}

		if (!sucess) {
			String msg = Policy.bind("Unable to delete", oldPath);
			UpdateCore.log(msg, null);
		} else {
			// remove the plugin files;
			Iterator iter = installedFiles.iterator();
			File featureFile = null;
			while (iter.hasNext()) {
				String path = (String) iter.next();
				featureFile = new File(path);
				UpdateManagerUtils.removeFromFileSystem(featureFile);
			}

			// remove the plugin directory if empty
			try {
				URL newURL = new URL(site.getURL(), Site.DEFAULT_PLUGIN_PATH + pluginEntry.getVersionedIdentifier().toString());
				String pluginPath = newURL.getFile();
				UpdateManagerUtils.removeEmptyDirectoriesFromFileSystem(new File(pluginPath));
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(e.getMessage(), e);
			}
		}
		closed = true;
	}

}
