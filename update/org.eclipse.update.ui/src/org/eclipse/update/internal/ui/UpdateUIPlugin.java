package org.eclipse.update.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import java.util.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.manager.*;
import org.eclipse.update.core.*;
import java.lang.reflect.*;
import org.eclipse.jface.dialogs.ErrorDialog;


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
	
	/**
	 * The constructor.
	 */
	public UpdateUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.update.internal.ui.UpdateUIPluginResources");
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
		ResourceBundle bundle= UpdateUIPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	
	public static String getFormattedMessage(String key, String [] args) {
		String text = getResourceString(key);
		return java.text.MessageFormat.format(text, args);
	}
	
	public static String getFormattedMessage(String key, String arg) {
		String text = getResourceString(key);
		return java.text.MessageFormat.format(text, new Object [] { arg });
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	public void startup() throws CoreException {
		super.startup();
		model = new UpdateModel();
		model.startup();
		IAdapterManager manager = Platform.getAdapterManager();
		adapterFactory = new UpdateAdapterFactory();
		manager.registerAdapters(adapterFactory, ModelObject.class);
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
			e = ((InvocationTargetException)e).getTargetException();
		}
		String message = e.getMessage();
		if (message==null)
	 		message = e.toString();
		Status status = new Status(IStatus.ERROR, getPluginId(), IStatus.OK, message, e);
		if (showErrorDialog) 
		   ErrorDialog.openError(getActiveWorkbenchShell(), null, null, status);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static IFeature [] searchSite(String featureId, ISite site) throws CoreException {
		IFeatureReference [] references = site.getFeatureReferences();
		Vector result=new Vector();

		for (int i=0; i<references.length; i++) {
			IFeature feature = references[i].getFeature();
			String id = feature.getVersionIdentifier().getIdentifier();
			if (featureId.equals(id)) {
				result.add(feature);
			}
		}
		return (IFeature[])result.toArray(new IFeature[result.size()]);
	}
}
