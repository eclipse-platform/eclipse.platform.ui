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
package org.eclipse.update.internal.ui;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.net.URLEncoder;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.security.*;
import org.osgi.framework.*;

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
	private String appServerHost;
	private int appServerPort;
	private UpdateLabelProvider labelProvider;
	private static boolean remindOnCancel = true;

	/**
	 * The constructor.
	 */
	public UpdateUI() {

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
		return getDefault().getBundle().getSymbolicName();
	}

	public UpdateLabelProvider getLabelProvider() {
		if (labelProvider == null)
			labelProvider = new UpdateLabelProvider();
		return labelProvider;
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

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		model = new UpdateModel();
		authenticator = new UpdateManagerAuthenticator();
		Authenticator.setDefault(authenticator);
		int historyPref =
			getPluginPreferences().getInt(UpdateCore.P_HISTORY_SIZE);
		if (historyPref > 0) {
			UpdateCore.DEFAULT_HISTORY = historyPref;
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

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (model != null)
			model.shutdown();

		if (labelProvider != null)
			labelProvider.dispose();
		super.stop(context);

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
//			 Should log on the update plugin's log
//			Platform.getPlugin("org.eclipse.core.runtime").getLog().log(status);
			Bundle bundle = Platform.getBundle("org.eclipse.update.ui");  //$NON-NLS-1$
			Platform.getLog(bundle).log(status);
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

	
	/**
	 * Prompts the user to restart
	 * @param restartIsReallyNeeded true when a restart is needed, false if the user feels lucky (tm) and wants the changes
	 * applied to the current config
	 */
	public static void requestRestart(boolean restartIsReallyNeeded) {
		boolean restart =
			RestartDialog.openQuestion(
				getActiveWorkbenchShell(),
				restartIsReallyNeeded);
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
			try {
				value = URLEncoder.encode(value, "UTF-8"); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
			}
			return value;
		}
	}
	
	public static boolean getRemindOnCancel() {
		return remindOnCancel;
	}
	
	public static void setRemindOnCancel(boolean remind) {
		remindOnCancel = remind; 
	}
}
