package org.eclipse.update.internal.core;

import java.io.InputStream;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.VersionedIdentifier; 

public class DefaultExecutableFeature extends AbstractFeature {

	/**
	 * Constructor for DefaultExecutableFeature
	 */
	public DefaultExecutableFeature(IFeature sourceFeature, ISite targetSite) {
		super(sourceFeature, targetSite);
	}

	/**
	 * Constructor for DefaultExecutableFeature
	 */
	public DefaultExecutableFeature(
		VersionedIdentifier identifier,
		ISite targetSite) {
		super(identifier, targetSite);
	}

	/**
	 * @see AbstractFeature#getContentReferenceToInstall(IPluginEntry[])
	 */
	public String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall) {
		return null;
	}

	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	public InputStream getInputStreamFor(IPluginEntry pluginEntry,String name) {
		return null;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames(IPluginEntry)
	 */
	public String[] getStorageUnitNames(IPluginEntry pluginEntry) {
		return null;
	}

	/**
	 * @see AbstractFeature#getFeatureInputStream()
	 */
	public InputStream getFeatureInputStream() {
		// TODO:
		// the feature url is pointing at the directory, teh feature.xml is inside
		return null;
	}

	/**
	 * @see AbstractFeature#getContentReferences()
	 */
	public String[] getContentReferences() {
		return null;
	}

	/**
	 * @see AbstractFeature#isExecutable()
	 */
	public boolean isExecutable() {
		return true;
	}

	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	protected InputStream getInputStreamFor(String name) {
		return null;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames()
	 */
	protected String[] getStorageUnitNames() {
		return null;
	}

}

