package org.eclipse.ui.examples.jobs;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.examples.jobs.views.ProgressExampleAdapterFactory;
import org.eclipse.ui.examples.jobs.views.SlowElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProgressExamplesPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ProgressExamplesPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public ProgressExamplesPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("ProgressExamples.ProgressExamplesPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static ProgressExamplesPlugin getDefault() {
		return plugin;
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
		ResourceBundle bundle= ProgressExamplesPlugin.getDefault().getResourceBundle();
		try {
			return (bundle!=null ? bundle.getString(key) : key);
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
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException {
		IAdapterManager m = Platform.getAdapterManager();
		IAdapterFactory f = new ProgressExampleAdapterFactory();
		m.registerAdapters(f, SlowElement.class);
	}
}
