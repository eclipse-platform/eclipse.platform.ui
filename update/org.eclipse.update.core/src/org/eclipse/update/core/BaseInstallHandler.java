package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * @since 2.0
 */

public class BaseInstallHandler implements IInstallHandler {
	
	protected int type;
	protected IFeature feature;
	protected IInstallHandlerEntry entry;
	protected InstallMonitor monitor;
	protected IPluginEntry[] pluginEntries;
	protected INonPluginEntry[] nonPluginEntries;
	protected boolean initialized = false;

	/**
	 * @see IInstallHandler#initialize(int, IFeature, IInstallHandlerEntry)
	 */
	public void initialize(
		int type,
		IFeature feature,
		IInstallHandlerEntry entry, InstallMonitor monitor)
		throws CoreException {
			
		if (this.initialized)
			return;
		else {
			if (feature == null)
				throw new IllegalArgumentException();
			this.type = type;
			this.feature = feature;
			this.entry = entry;
			this.monitor = monitor;
			this.initialized = true;
		}
	}

	/**
	 * @see IInstallHandler#installInitiated()
	 */
	public void installInitiated() throws CoreException {
	}

	/**
	 * @see IInstallHandler#pluginsDownloaded(IPluginEntry[])
	 */
	public void pluginsDownloaded(IPluginEntry[] plugins)
		throws CoreException {
			
		this.pluginEntries = plugins;
	}

	/**
	 * @see IInstallHandler#nonPluginDataDownloaded(INonPluginEntry[], IVerificationListener)
	 */
	public void nonPluginDataDownloaded(
		INonPluginEntry[] nonPluginData,
		IVerificationListener listener)
		throws CoreException {
			
		this.nonPluginEntries = nonPluginData;
	}

	/**
	 * @see IInstallHandler#pluginsInstalled(IFeatureContentConsumer)
	 */
	public void completeInstall(IFeatureContentConsumer consumer)
		throws CoreException {
	}

	/**
	 * @see IInstallHandler#installCompleted(boolean)
	 */
	public void installCompleted(boolean success)
		throws CoreException {
	}

	/**
	 * @see IInstallHandler#configureInitiated()
	 */
	public void configureInitiated() throws CoreException {
	}

	/**
	 * @see IInstallHandler#completeConfigure()
	 */
	public void completeConfigure() throws CoreException {
	}

	/**
	 * @see IInstallHandler#configureCompleted(boolean)
	 */
	public void configureCompleted(boolean success)
		throws CoreException {
	}

	/**
	 * @see IInstallHandler#unconfigureInitiated()
	 */
	public void unconfigureInitiated() throws CoreException {
	}

	/**
	 * @see IInstallHandler#completeUnconfigure()
	 */
	public void completeUnconfigure() throws CoreException {
	}

	/**
	 * @see IInstallHandler#unconfigureCompleted(boolean)
	 */
	public void unconfigureCompleted(boolean success)
		throws CoreException {
	}

	/**
	 * @see IInstallHandler#uninstallInitiated()
	 */
	public void uninstallInitiated() throws CoreException {
	}

	/**
	 * @see IInstallHandler#completeUninstall()
	 */
	public void completeUninstall() throws CoreException {
	}

	/**
	 * @see IInstallHandler#uninstallCompleted(boolean)
	 */
	public void uninstallCompleted(boolean success)
		throws CoreException {
	}
}
