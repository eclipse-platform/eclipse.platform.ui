package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;

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
	public SiteFilePluginContentConsumer(IPluginEntry pluginEntry,ISite site){
		this.pluginEntry = pluginEntry;
		this.site = site;
	}

	/*
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		String path = UpdateManagerUtils.getPath(site.getURL());
		InputStream inStream = null;

		// FIXME: fragment code
		String pluginPath = null;
		if (pluginEntry.isFragment()) {
			pluginPath = path + Site.DEFAULT_FRAGMENT_PATH + pluginEntry.getIdentifier().toString();
		} else {
			pluginPath = path + Site.DEFAULT_PLUGIN_PATH + pluginEntry.getIdentifier().toString();
		}
		String contentKey = contentReference.getIdentifier();
		pluginPath += pluginPath.endsWith(File.separator) ? contentKey : File.separator + contentKey;

		try {
			inStream = contentReference.getInputStream();
			UpdateManagerUtils.copyToLocal(inStream, pluginPath, null);
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Error creating file:" + pluginPath, e);
			throw new CoreException(status);
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {}
		}

	}

	/*
	 * @see ISiteContentConsumer#close()
	 */
	public void close() {
	}
}