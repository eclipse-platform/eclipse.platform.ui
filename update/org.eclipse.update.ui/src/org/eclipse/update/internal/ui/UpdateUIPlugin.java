package org.eclipse.update.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.forms.UpdateAdapterFactory;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.AboutInfo;
import org.eclipse.update.internal.ui.security.AuthorizationDatabase;

/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateUIPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.update.ui";
	//The shared instance.
	private static UpdateUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private UpdateAdapterFactory adapterFactory;
	private UpdateModel model;
	private AuthorizationDatabase database;
	private AboutInfo aboutInfo;

	/**
	 * The constructor.
	 */
	public UpdateUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle =
				ResourceBundle.getBundle(
					"org.eclipse.update.internal.ui.UpdateUIPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateUIPlugin getDefault() {
		return plugin;
	}

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	private IWorkbenchPage internalGetActivePage() {
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}
	
	public AboutInfo getAboutInfo() {
		return aboutInfo;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = UpdateUIPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static String getFormattedMessage(String key, String[] args) {
		String text = getResourceString(key);
		return java.text.MessageFormat.format(text, args);
	}

	public static String getFormattedMessage(String key, String arg) {
		String text = getResourceString(key);
		return java.text.MessageFormat.format(text, new Object[] { arg });
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void startup() throws CoreException {
		super.startup();
		readInfo();
		model = new UpdateModel();
		model.startup();
		IAdapterManager manager = Platform.getAdapterManager();
		adapterFactory = new UpdateAdapterFactory();
		manager.registerAdapters(adapterFactory, UIModelObject.class);
		database = new AuthorizationDatabase();
		Authenticator.setDefault(database);
	}

	public void shutdown() throws CoreException {
		IAdapterManager manager = Platform.getAdapterManager();
		manager.unregisterAdapters(adapterFactory);
		model.shutdown();
		super.shutdown();
	}

	public UpdateModel getUpdateModel() {
		return model;
	}

	public static void logException(Throwable e) {
		logException(e, true);
	}

	public static void logException(Throwable e, boolean showErrorDialog) {
		if (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}

		IStatus status = null;
		if (e instanceof CoreException) {
			status = ((CoreException) e).getStatus();
		} else {
			String message = e.getMessage();
			if (message == null)
				message = e.toString();
			status =
				new Status(
					IStatus.ERROR,
					getPluginId(),
					IStatus.OK,
					message,
					e);
		}
		log(status, showErrorDialog);
	}

	public static void log(IStatus status, boolean showErrorDialog) {
		if (showErrorDialog)
			ErrorDialog.openError(
				getActiveWorkbenchShell(),
				null,
				null,
				status);
		//ResourcesPlugin.getPlugin().getLog().log(status);
		Platform.getPlugin("org.eclipse.core.runtime").getLog().log(status);
	}

	public static IFeature[] searchSite(
		String featureId,
		IConfiguredSite site,
		boolean onlyConfigured)
		throws CoreException {
		IFeatureReference[] references = null;

		if (onlyConfigured)
			references = site.getConfiguredFeatures();
		else
			references = site.getSite().getFeatureReferences();
		Vector result = new Vector();

		for (int i = 0; i < references.length; i++) {
			IFeature feature = references[i].getFeature();
			String id = feature.getVersionedIdentifier().getIdentifier();
			if (featureId.equals(id)) {
				result.add(feature);
			}
		}
		return (IFeature[]) result.toArray(new IFeature[result.size()]);
	}
	
	public static IFeature[] getInstalledFeatures(IFeature feature) {
		Vector features = new Vector();
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			IConfiguredSite[] isites = config.getConfiguredSites();
			VersionedIdentifier vid = feature.getVersionedIdentifier();
			String id = vid.getIdentifier();

			for (int i = 0; i < isites.length; i++) {
				IConfiguredSite isite = isites[i];
				IFeature[] result = UpdateUIPlugin.searchSite(id, isite, true);
				for (int j = 0; j < result.length; j++) {
					IFeature installedFeature = result[j];
					features.add(installedFeature);
				}
			}
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
		return (IFeature[]) features.toArray(new IFeature[features.size()]);
	}
	/**
	 * Gets the database.
	 * @return Returns a AuthorizationDatabase
	 */
	public AuthorizationDatabase getDatabase() {
		return database;
	}

	private void readInfo() {
		// determine the identifier of the "dominant" application 
		IPlatformConfiguration conf =
			BootLoader.getCurrentPlatformConfiguration();
		String versionedFeatureId = conf.getPrimaryFeatureIdentifier();

		if (versionedFeatureId == null) {
			aboutInfo = new AboutInfo(null, null); // Ok to pass null
		} else {
			int index = versionedFeatureId.lastIndexOf("_"); //$NON-NLS-1$
			if (index == -1)
				aboutInfo = new AboutInfo(versionedFeatureId, null);
			else {
				String mainPluginName = versionedFeatureId.substring(0, index);
				PluginVersionIdentifier mainPluginVersion = null;
				try {
					mainPluginVersion =
						new PluginVersionIdentifier(
							versionedFeatureId.substring(index + 1));
				} catch (Exception e) {
					IStatus iniStatus = new Status(IStatus.ERROR, WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 0, "Unknown plugin version " + versionedFeatureId, e); //$NON-NLS-1$
					log(iniStatus, true); //$NON-NLS-1$
				}
				aboutInfo = new AboutInfo(mainPluginName, mainPluginVersion);
			}
		}
		try {
			aboutInfo.readINIFile();
		} catch (CoreException e) {
			log(e.getStatus(), true); //$NON-NLS-1$
		}
	}
	public static void informRestartNeeded() {
		String title = UpdateUIPlugin.getResourceString("RestartTitle");
		String message = UpdateUIPlugin.getResourceString("RestartMessage");
		boolean restart =
			MessageDialog.openQuestion(
				getActiveWorkbenchShell(),
				title,
				message);
		if (restart)
			PlatformUI.getWorkbench().restart();
	}
}