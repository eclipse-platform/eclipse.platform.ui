package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;

public interface IInstallHandler {

	public static final int HANDLER_ACTION_INSTALL = 1;
	public static final int HANDLER_ACTION_CONFIGURE = 2;
	public static final int HANDLER_ACTION_UNCONFIGURE = 3;
	public static final int HANDLER_ACTION_UNINSTALL = 4;

	public void initialize(
		int type,
		IFeature feature,
		IInstallHandlerEntry entry,
		InstallMonitor monitor)
		throws CoreException;

	public void installInitiated() throws CoreException;

	public void pluginsDownloaded(IPluginEntry[] plugins) throws CoreException;

	public void nonPluginDataDownloaded(
		INonPluginEntry[] nonPluginData,
		IVerificationListener listener)
		throws CoreException;

	public void completeInstall(IFeatureContentConsumer consumer)
		throws CoreException;

	public void installCompleted(boolean success) throws CoreException;

	public void configureInitiated() throws CoreException;

	public void completeConfigure() throws CoreException;

	public void configureCompleted(boolean success) throws CoreException;

	public void unconfigureInitiated() throws CoreException;

	public void completeUnconfigure() throws CoreException;

	public void unconfigureCompleted(boolean success) throws CoreException;

	public void uninstallInitiated() throws CoreException;

	public void completeUninstall() throws CoreException;

	public void uninstallCompleted(boolean success) throws CoreException;

}