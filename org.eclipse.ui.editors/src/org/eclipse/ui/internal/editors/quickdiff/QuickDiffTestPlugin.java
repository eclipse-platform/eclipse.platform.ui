package org.eclipse.ui.internal.editors.quickdiff;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * The main plugin class to be used in the desktop.
 */
public class QuickDiffTestPlugin {
	private static final String REFERENCE_PROVIDER_EXTENSION_POINT= "quickdiffreferenceprovider"; //$NON-NLS-1$
	private static final String PLUGIN_ID= "org.eclipse.ui.editors"; //$NON-NLS-1$

	/** The shared instance. */
	private static QuickDiffTestPlugin plugin;
	/** The resource bundle. */
	private ResourceBundle resourceBundle;
	/** The default reference provider's descriptor. */
	private ReferenceProviderDescriptor fDefaultDescriptor;
	/** The menu entries for the editor's ruler context menu. */
	ArrayList fMenuEntries= new ArrayList();

	/**
	 * The constructor.
	 */
	private QuickDiffTestPlugin() {
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.ui.internal.editors.quickdiff.QuickDiffTestPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle= null;
		}

		registerExtensions();
	}

	/**
	 * Returns the shared instance.
	 */
	public synchronized static QuickDiffTestPlugin getDefault() {
		if (plugin == null)
			plugin= new QuickDiffTestPlugin();
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
		ResourceBundle bundle= QuickDiffTestPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null ? bundle.getString(key) : key);
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

	/** reads all extensions */
	private void registerExtensions() {
		IPluginRegistry registry= Platform.getPluginRegistry();

		IConfigurationElement[] elements= registry.getConfigurationElementsFor(PLUGIN_ID, REFERENCE_PROVIDER_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			ReferenceProviderDescriptor desc= new ReferenceProviderDescriptor(elements[i]);
			if (fDefaultDescriptor == null && desc.getDefault())
				fDefaultDescriptor= desc;
			fMenuEntries.add(desc);
		}
	}

	public ReferenceProviderDescriptor[] getExtensions() {
		ReferenceProviderDescriptor[] arr= new ReferenceProviderDescriptor[fMenuEntries.size()];
		return (ReferenceProviderDescriptor[])fMenuEntries.toArray(arr);
	}

	/**
	 * Returns the first descriptor with the <code>default</code> attribute set to <code>true</code>.
	 * @return the descriptor of the default reference provider.
	 */
	public ReferenceProviderDescriptor getDefaultProvider() {
		return fDefaultDescriptor;
	}
}
