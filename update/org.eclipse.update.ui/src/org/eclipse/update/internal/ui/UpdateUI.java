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
package org.eclipse.update.internal.ui;

import java.lang.reflect.*;
import java.net.*;
import java.net.URLEncoder;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.plugin.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.model.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.AboutInfo;
import org.eclipse.update.internal.ui.security.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateUI extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.update.ui"; //$NON-NLS-1$
	public static final String WEB_APP_ID = "org.eclipse.update"; //$NON-NLS-1$

	
	//The shared instance.
	private static UpdateUI plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private UpdateModel model;
	private UpdateManagerAuthenticator authenticator;
	private AboutInfo aboutInfo;
	private String appServerHost;
	private int appServerPort;
	private UpdateLabelProvider labelProvider;
	private static boolean remindOnCancel = true;

	/**
	 * The constructor.
	 */
	public UpdateUI(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle =
				ResourceBundle.getBundle(
					"org.eclipse.update.internal.ui.UpdateUIPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateUI getDefault() {
		return plugin;
	}

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
			return window.getActivePage();
		return null;
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		return window != null ? window.getShell() : null;
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

	public UpdateLabelProvider getLabelProvider() {
		if (labelProvider == null)
			labelProvider = new UpdateLabelProvider();
		return labelProvider;
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
	public static String getString(String key) {
		ResourceBundle bundle = UpdateUI.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static String getFormattedMessage(String key, String[] args) {
		String text = getString(key);
		return java.text.MessageFormat.format(text, args);
	}

	public static String getFormattedMessage(String key, String arg) {
		String text = getString(key);
		return java.text.MessageFormat.format(text, new String[] { arg });
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
		authenticator = new UpdateManagerAuthenticator();
		Authenticator.setDefault(authenticator);
		int historyPref =
			getPluginPreferences().getInt(UpdateCore.P_HISTORY_SIZE);
		if (historyPref > 0) {
			SiteLocalModel.DEFAULT_HISTORY = historyPref;
		}
	}

	public void startWebApp() throws CoreException {

		// configure web install handler
		try {
			WebappManager.start(WEB_APP_ID, PLUGIN_ID, new Path("webapp")); //$NON-NLS-1$
		} catch (CoreException e) {
			UpdateUI.logException(e);
			return;
		}

		appServerHost = WebappManager.getHost();
		appServerPort = WebappManager.getPort();
	}

	public void stopWebApp() throws CoreException {
		try {
			// unconfigure web install handler
			WebappManager.stop(WEB_APP_ID);
		} finally {
			appServerHost = null;
			appServerPort = 0;
		}
	}

	public boolean isWebAppStarted() {
		return appServerHost != null;
	}

	public String getAppServerHost() {
		return appServerHost;
	}

	public int getAppServerPort() {
		return appServerPort;
	}

	public void shutdown() throws CoreException {
		model.shutdown();

		if (labelProvider != null)
			labelProvider.dispose();
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
		if (status.getSeverity() != IStatus.INFO) {
			if (showErrorDialog)
				ErrorDialog.openError(
					getActiveWorkbenchShell(),
					null,
					null,
					status);
			//ResourcesPlugin.getPlugin().getLog().log(status);
			Platform.getPlugin("org.eclipse.core.runtime").getLog().log(status); //$NON-NLS-1$
		} else {
			MessageDialog.openInformation(
				getActiveWorkbenchShell(),
				null,
				status.getMessage());
		}
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
			IFeature feature = references[i].getFeature(null);
			String id = feature.getVersionedIdentifier().getIdentifier();
			if (featureId.equals(id)) {
				result.add(feature);
			}
		}
		return (IFeature[]) result.toArray(new IFeature[result.size()]);
	}

	public static IFeature[] getInstalledFeatures(IFeature feature) {
		return getInstalledFeatures(feature, true);
	}

	public static IFeature[] getInstalledFeatures(
		IFeature feature,
		boolean onlyConfigured) {
		Vector features = new Vector();
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			IConfiguredSite[] isites = config.getConfiguredSites();
			VersionedIdentifier vid = feature.getVersionedIdentifier();
			String id = vid.getIdentifier();

			for (int i = 0; i < isites.length; i++) {
				IConfiguredSite isite = isites[i];
				IFeature[] result =
					UpdateUI.searchSite(id, isite, onlyConfigured);
				for (int j = 0; j < result.length; j++) {
					IFeature installedFeature = result[j];
					features.add(installedFeature);
				}
			}
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
		return (IFeature[]) features.toArray(new IFeature[features.size()]);
	}

	public static boolean isPatch(IFeature candidate) {
		IImport[] imports = candidate.getImports();

		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			if (iimport.isPatch())
				return true;
		}
		return false;
	}

	public static boolean isPatch(IFeature target, IFeature candidate) {
		VersionedIdentifier vid = target.getVersionedIdentifier();
		IImport[] imports = candidate.getImports();

		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			if (iimport.isPatch()) {
				VersionedIdentifier ivid = iimport.getVersionedIdentifier();
				if (vid.equals(ivid)) {
					// Bingo.
					return true;
				}
			}
		}
		return false;
	}

	public static IInstallConfiguration getBackupConfigurationFor(IFeature feature) {
		VersionedIdentifier vid = feature.getVersionedIdentifier();
		String key = "@" + vid.getIdentifier() + "_" + vid.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			ILocalSite lsite = SiteManager.getLocalSite();
			IInstallConfiguration[] configs =
				lsite.getPreservedConfigurations();
			for (int i = 0; i < configs.length; i++) {
				IInstallConfiguration config = configs[i];
				if (config.getLabel().startsWith(key))
					return config;
			}
		} catch (CoreException e) {
		}
		return null;
	}

	/**
	 * Gets the authenticator.
	 * @return Returns a UpdateManagerAuthenticator
	 */
	public UpdateManagerAuthenticator getAuthenticator() {
		return authenticator;
	}

	public static URL getOriginatingURL(String id) {
		IDialogSettings section = getOriginatingURLSection();
		String value = section.get(id);
		if (value != null) {
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
			}
		}
		return null;
	}

	public static void setOriginatingURL(String id, URL url) {
		IDialogSettings section = getOriginatingURLSection();
		section.put(id, url.toString());
	}

	private static IDialogSettings getOriginatingURLSection() {
		IDialogSettings settings = getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection("originatingURLs"); //$NON-NLS-1$
		if (section == null)
			section = settings.addNewSection("originatingURLs"); //$NON-NLS-1$
		return section;
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
	
	public static void requestRestart() {
		String title = UpdateUI.getString("RestartTitle"); //$NON-NLS-1$
		String message = UpdateUI.getString("RestartMessage"); //$NON-NLS-1$
		boolean restart =
			MessageDialog.openQuestion(
				getActiveWorkbenchShell(),
				title,
				message);
		if (restart)
			PlatformUI.getWorkbench().restart();
	}

	public static void showURL(String url) {
		showURL(url, false);
	}
	
	public static void showURL(String url, boolean encodeHostAndPort) {
		if (encodeHostAndPort)
			url = encodeHostAndPort(url);

		if (SWT.getPlatform().equals("win32")) { //$NON-NLS-1$
			Program.launch(url);
		} else {
			IBrowser browser = BrowserManager.getInstance().createBrowser();
			try {
				browser.displayURL(url);
			} catch (Exception e) {
				UpdateUI.logException(e);
			}
		}
	}

	private static String encodeHostAndPort(String urlName) {
		String callbackURL = getCallbackURLAsString();
		if (callbackURL == null)
			return urlName;
		String callbackParameter = "updateURL=" + callbackURL; //$NON-NLS-1$
		if (urlName.indexOf('?') != -1)
			return urlName + "&" + callbackParameter; //$NON-NLS-1$
		else
			return urlName + "?" + callbackParameter; //$NON-NLS-1$
	}
	
	private static String getCallbackURLAsString() {
		String host = getDefault().getAppServerHost();
		int port = getDefault().getAppServerPort();
		if (host == null || port == 0)
			return null;
		else {
			String value =
				"http://" //$NON-NLS-1$
					+ host
					+ ":" //$NON-NLS-1$
					+ port
					+ "/" //$NON-NLS-1$
					+ WEB_APP_ID
					+ "/install"; //$NON-NLS-1$
			return URLEncoder.encode(value);
		}
	}
	
	public static boolean getRemindOnCancel() {
		return remindOnCancel;
	}
	
	public static void setRemindOnCancel(boolean remind) {
		remindOnCancel = remind; 
	}

}
