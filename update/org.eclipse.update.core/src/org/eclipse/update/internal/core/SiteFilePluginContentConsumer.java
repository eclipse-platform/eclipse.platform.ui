package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Plugin Content Consumer on a Site
 */
public class SiteFilePluginContentConsumer extends ContentConsumer {

	private IPluginEntry pluginEntry;
	private ISite site;

	/**
	 * Constructor for FileSite
	 */
	public SiteFilePluginContentConsumer(IPluginEntry pluginEntry, ISite site) {
		this.pluginEntry = pluginEntry;
		this.site = site;
	}

	/*
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		//String path = site.getURL().getFile();
		InputStream inStream = null;
		String pluginPath = null;		
		
		try {
			// FIXME: fragment code
			if (pluginEntry.isFragment()) {
				URL newURL = new URL(site.getURL(), Site.DEFAULT_FRAGMENT_PATH + pluginEntry.getVersionedIdentifier().toString());
				pluginPath = newURL.getFile();
			} else {
				URL newURL = new URL(site.getURL(), Site.DEFAULT_PLUGIN_PATH + pluginEntry.getVersionedIdentifier().toString());
				pluginPath = newURL.getFile();
			}
			String contentKey = contentReference.getIdentifier();
			pluginPath += pluginPath.endsWith(File.separator) ? contentKey : File.separator + contentKey;

			inStream = contentReference.getInputStream();
			UpdateManagerUtils.copyToLocal(inStream, pluginPath, null);
		} catch (IOException e) {
			throw newCoreException(Policy.bind("GlobalConsumer.ErrorCreatingFile") + pluginPath, e); //$NON-NLS-1$
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}

	}

	/*
	 * @see ISiteContentConsumer#close()
	 */
	public void close() {
		if (site instanceof SiteFile) 
			((SiteFile) site).addPluginEntry(pluginEntry);
	}
	
	/**
	 * 
	 */
	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		return new CoreException(new Status(IStatus.ERROR,id,0,s,e));
	}	
	
	
}