package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.internal.boot.Policy;
import org.eclipse.update.core.model.ContentEntryModel;
import org.eclipse.update.core.model.NonPluginEntryModel;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.internal.core.UpdateManagerUtils;

/**
 * Base class for feature content providers.
 * </p>
 * @since 2.0
 */

public abstract class FeatureContentProvider implements IFeatureContentProvider {

	private URL base;
	private IFeature feature;
	private File tmpDir; // local work area for each provider

	/**
	 * @since 2.0
	 */
	public FeatureContentProvider(URL base) {
		this.base = base;
		this.feature = null;
	}

	/*
	 * @see IFeatureContentProvider#getURL()
	 */
	public URL getURL() {
		return base;
	}

	/*
	 * @see IFeatureContentProvider#getFeature()
	 */
	public IFeature getFeature() {
		return feature;
	}

	/*
	 * @see IFeatureContentProvider#setFeature(IFeature)
	 */
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	/**
	 * Returns the specified reference as a local file system reference.
	 * If required, the file represented by the specified content
	 * reference is first downloaded to the local system
	 * 
	 * @since 2.0
	 */
	public ContentReference asLocalReference(ContentReference ref, InstallMonitor monitor) throws IOException {

		// check to see if this is already a local reference
		if (ref.isLocalReference())
			return ref;

		// check to see if we already have a local file for this reference
		String key = ref.toString();
		File localFile = Utilities.lookupLocalFile(key);
		if (localFile != null)
			return ref.createContentReference(ref.getIdentifier(), localFile);

		// download the referenced file into local temporary area
		localFile = Utilities.createLocalFile(getWorkingDirectory(), key, null /*name*/
		);
		InputStream is = null;
		OutputStream os = null;
		try {
			if (monitor != null) {
				monitor.saveState();
				monitor.setTaskName(Policy.bind("FeatureContentProvider.Downloading")); //$NON-NLS-1$
				monitor.subTask(ref.getIdentifier() + " "); //$NON-NLS-1$
				monitor.setTotalCount(ref.getInputSize());
				monitor.showCopyDetails(true);
			}
			is = ref.getInputStream();
			os = new FileOutputStream(localFile);
			Utilities.copy(is, os, monitor);
		} catch (IOException e) {
			Utilities.removeLocalFile(key);
			throw e;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
			if (monitor != null)
				monitor.restoreState();
		}
		return ref.createContentReference(ref.getIdentifier(), localFile);
	}

	/**
	 * Returns the specified reference as a local file.
	 * If required, the file represented by the specified content
	 * reference is first downloaded to the local system
	 * 
	 * @since 2.0
	 */
	public File asLocalFile(ContentReference ref, InstallMonitor monitor) throws IOException {
		File file = ref.asFile();
		if (file != null)
			return file;

		ContentReference localRef = asLocalReference(ref, monitor);
		file = localRef.asFile();
		return file;
	}

	/**
	 * Returns working directory for this content provider
	 * 
	 * @since 2.0
	 */
	protected File getWorkingDirectory() throws IOException {
		if (tmpDir == null)
			tmpDir = Utilities.createWorkingDirectory();
		return tmpDir;
	}
	/*
	 * @see IFeatureContentProvider#getDownloadSizeFor(IPluginEntry[], INonPluginEntry[])
	 */
	public long getDownloadSizeFor(IPluginEntry[] pluginEntries, INonPluginEntry[] nonPluginEntries) {
		long result = 0;

		if ((pluginEntries == null || pluginEntries.length == 0) && (nonPluginEntries == null || nonPluginEntries.length == 0)) {
			return ContentEntryModel.UNKNOWN_SIZE;
		}

		// loop on plugin entries
		long size = 0;
		for (int i = 0; i < pluginEntries.length; i++) {
			size = ((PluginEntryModel) pluginEntries[i]).getDownloadSize();
			if (size == ContentEntryModel.UNKNOWN_SIZE) {
				return ContentEntryModel.UNKNOWN_SIZE;
			}
			result += size;
		}

		// loop on non plugin entries
		for (int i = 0; i < pluginEntries.length; i++) {
			size = ((NonPluginEntryModel) nonPluginEntries[i]).getDownloadSize();
			if (size == ContentEntryModel.UNKNOWN_SIZE) {
				return ContentEntryModel.UNKNOWN_SIZE;
			}
			result += size;
		}

		return result;
	}

	/*
	 * @see IFeatureContentProvider#getInstallSizeFor(IPluginEntry[], INonPluginEntry[])
	 */
	public long getInstallSizeFor(IPluginEntry[] pluginEntries, INonPluginEntry[] nonPluginEntries) {
		long result = 0;

		if ((pluginEntries == null || pluginEntries.length == 0) && (nonPluginEntries == null || nonPluginEntries.length == 0)) {
			return ContentEntryModel.UNKNOWN_SIZE;
		}

		// loop on plugin entries
		long size = 0;
		for (int i = 0; i < pluginEntries.length; i++) {
			size = ((PluginEntryModel) pluginEntries[i]).getInstallSize();
			if (size == ContentEntryModel.UNKNOWN_SIZE) {
				return ContentEntryModel.UNKNOWN_SIZE;
			}
			result += size;
		}

		// loop on non plugin entries
		for (int i = 0; i < nonPluginEntries.length; i++) {
			size = ((NonPluginEntryModel) nonPluginEntries[i]).getInstallSize();
			if (size == ContentEntryModel.UNKNOWN_SIZE) {
				return ContentEntryModel.UNKNOWN_SIZE;
			}
			result += size;
		}

		return result;
	}


}