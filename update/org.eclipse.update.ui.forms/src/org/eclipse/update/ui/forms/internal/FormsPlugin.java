package org.eclipse.update.ui.forms.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class FormsPlugin extends AbstractUIPlugin {
	private static FormsPlugin instance;
	private ResourceBundle resourceBundle;

	/**
	 * Constructor for FormsPlugin.
	 * @param descriptor
	 */
	public FormsPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		instance = this;
		try {
			resourceBundle =
				ResourceBundle.getBundle(
					"org.eclipse.update.ui.forms.internal.FormsPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = FormsPlugin.getDefault().getResourceBundle();
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

	public static FormsPlugin getDefault() {
		return instance;
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
		Platform.getPlugin("org.eclipse.core.runtime").getLog().log(status);
	}
}