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
package org.eclipse.update.internal.core;
import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Plugin Content Consumer on a Site
 * for a plugin that will run from a jar
 */
public class SiteFilePackedPluginContentConsumer extends ContentConsumer {

	private IPluginEntry pluginEntry;
	private ISite site;
	private boolean closed = false;
	private String jarPath;
	private String tempPath;

	/*
	 * Constructor
	 */
	public SiteFilePackedPluginContentConsumer(IPluginEntry pluginEntry, ISite site) {
		this.pluginEntry = pluginEntry;
		this.site = site;
	}

	/*
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		InputStream inStream = null;

		if (closed) {
			UpdateCore.warn("Attempt to store in a closed SiteFilePluginContentConsumer", new Exception()); //$NON-NLS-1$
			return;
		}

		try {
			URL newURL = new URL(site.getURL(), Site.DEFAULT_PLUGIN_PATH + pluginEntry.getVersionedIdentifier().toString() + ".jar"); //$NON-NLS-1$
			inStream = contentReference.getInputStream();
			jarPath = newURL.getFile().replace(File.separatorChar, '/');
			File jarFile = new File(jarPath);
			if (jarFile.exists()) {
				throw Utilities.newCoreException(Policy.bind("UpdateManagerUtils.FileAlreadyExists", new Object[] { jarFile }), null); //$NON-NLS-1$
			}
			// error recovery
			tempPath= ErrorRecoveryLog.getLocalRandomIdentifier(jarPath+".tmp"); //$NON-NLS-1$
				ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.BUNDLE_JAR_ENTRY, tempPath);
			//
			UpdateManagerUtils.copyToLocal(inStream, tempPath, null);
		} catch (IOException e) {
			throw Utilities.newCoreException(Policy.bind("GlobalConsumer.ErrorCreatingFile", tempPath), e); //$NON-NLS-1$
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
			UpdateCore.warn("Attempt to close a closed SiteFilePluginContentConsumer", new Exception()); //$NON-NLS-1$
			return;
		}

		if (tempPath != null) {
			// rename file 
			ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.RENAME_ENTRY, tempPath);
			File fileToRename = new File(tempPath);
			boolean sucess = false;
			if (fileToRename.exists()) {
				File renamedFile = new File(jarPath);
				sucess = fileToRename.renameTo(renamedFile);
			}
			if (!sucess) {
				String msg = Policy.bind("ContentConsumer.UnableToRename", tempPath, jarPath); //$NON-NLS-1$
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
			UpdateCore.warn("Attempt to abort a closed SiteFilePluginContentConsumer", new Exception()); //$NON-NLS-1$
			return;
		}

		boolean sucess = true;

		// delete plugin.jar
		if (jarPath != null) {
			ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.DELETE_ENTRY, jarPath);
			File fileToRemove = new File(jarPath);

			if (fileToRemove.exists()) {
				sucess = fileToRemove.delete();
			}
		}

		if (!sucess) {
			String msg = Policy.bind("Unable to delete", jarPath); //$NON-NLS-1$
			UpdateCore.log(msg, null);
		}
		closed = true;
	}

}
