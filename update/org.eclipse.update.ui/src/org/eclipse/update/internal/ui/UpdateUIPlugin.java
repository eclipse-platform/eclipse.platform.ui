package org.eclipse.update.internal.ui;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import java.util.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.manager.*;
import org.eclipse.update.internal.transform.*;
import org.eclipse.update.core.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateUIPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.update.ui";
	public static final String UPDATE_MANAGER_ID = PLUGIN_ID+".updateManager";
	//The shared instance.
	private static UpdateUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private UpdateAdapterFactory adapterFactory;
	private TransformFactory transformFactory;
	
	private UpdateModel model;
	private TransformManager tmanager;
	private Hashtable urlActions = new Hashtable();
	
	/**
	 * The constructor.
	 */
	public UpdateUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.update.ui.UpdateUIPluginResources");
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
	
	public void registerURLAction(String id, IURLAction action) {
		urlActions.put(id, action);
	}
	
	public void unregisterURLAction(String id) {
		urlActions.remove(id);
	}
	
	public IURLAction getURLAction(String id) {
		return (IURLAction)urlActions.get(id);
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
		tmanager = new TransformManager();
		transformFactory = new TransformFactory();
		manager.registerAdapters(transformFactory, ModelObject.class);
		manager.registerAdapters(transformFactory, IFeature.class);
		manager.registerAdapters(transformFactory, ISite.class);
	}
	
	public void shutdown() throws CoreException {
		IAdapterManager manager = Platform.getAdapterManager();
		manager.unregisterAdapters(adapterFactory);
		manager.unregisterAdapters(transformFactory);
		tmanager.shutdown();
		model.shutdown();
		super.shutdown();
	}
	
	public UpdateModel getUpdateModel() {
		return model;
	}
	
	public TransformManager getTransformManager() {
		return tmanager;
	}
	
	
}
