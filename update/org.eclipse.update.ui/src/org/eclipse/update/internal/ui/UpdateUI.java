/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.update.internal.core.UpdateCore;
import org.eclipse.update.internal.ui.model.UpdateModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateUI extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.update.ui"; //$NON-NLS-1$
	public static final String WEB_APP_ID = "org.eclipse.update"; //$NON-NLS-1$
	// preference key
	public static final String P_DISCOVERY_SITES_ENABLED = "discoverySitesEnabled"; //$NON-NLS-1$
	//The shared instance.
	private static UpdateUI plugin;
	private UpdateModel model;
	private String appServerHost;
	private int appServerPort;
	private UpdateLabelProvider labelProvider;

	/**
	 * The constructor.
	 */
	public UpdateUI() {

		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateUI getDefault() {
		return plugin;
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		return window != null ? window.getShell() : getStandardDisplay().getActiveShell();
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

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		model = new UpdateModel();
		int historyPref =
			getPluginPreferences().getInt(UpdateCore.P_HISTORY_SIZE);
		if (historyPref > 0) {
			UpdateCore.DEFAULT_HISTORY = historyPref;
		}
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
		Bundle bundle = Platform.getBundle("org.eclipse.update.ui"); //$NON-NLS-1$
		Platform.getLog(bundle).log(status);
		if (Display.getCurrent() == null || !showErrorDialog)
			return;
		if (status.getSeverity() != IStatus.INFO) {
			ErrorDialog.openError(getActiveWorkbenchShell(), null, null, status);
		} else {
			MessageDialog.openInformation(getActiveWorkbenchShell(), null, status.getMessage());
		}
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
	 * Prompts the user to restart, and performs the restart if the user gives the ok.
	 * 
	 * @param restartIsReallyNeeded true when a restart is needed, false if the user feels lucky (tm) and wants the changes
	 * applied to the current config
	 * @return <code>true</code> if the system is restarting, and <code>false</code> otherwise
	 */
	public static boolean requestRestart(boolean restartIsReallyNeeded) {
		boolean restart =
			RestartDialog.openQuestion(
				getActiveWorkbenchShell(),
				restartIsReallyNeeded);
		if (restart)
			return PlatformUI.getWorkbench().restart();
		return false;
	}

	public static void showURL(String url) {
		showURL(url, false);
	}

	public static void showURL(String url, boolean encodeHostAndPort) {
		if (encodeHostAndPort)
			url = encodeHostAndPort(url);

		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			IWebBrowser browser = support.getExternalBrowser();
			browser.openURL(new URL(url));
		}
		catch (MalformedURLException e) {
			UpdateUI.logException(e);
		}
		catch (PartInitException e) {
			UpdateUI.logException(e);
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
				value = URLCoder.encode(value);
			} catch (UnsupportedEncodingException e) {
			}
			return value;
		}
	}
	
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated disaply. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#initializeDefaultPluginPreferences()
	 */
	protected void initializeDefaultPluginPreferences() {
		Preferences store = getPluginPreferences();
		store.setDefault(P_DISCOVERY_SITES_ENABLED, true);
	}
}
