package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.util.HelpPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
  * This class is a UI plugin. This may need to change to regular 
  * plugin if the plugin class is moved into the base help.
  */
public class WorkbenchHelpPlugin extends AbstractUIPlugin {
	protected static WorkbenchHelpPlugin plugin;

	/**
	 * HelpViewerPlugin constructor. It is called as part of plugin
	 * activation.
	 */
	public WorkbenchHelpPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		HelpSystem.setPlugin(plugin);
	}
	/**
	 * @return com.ibm.ua.HelpViewerPlugin
	 */
	public static WorkbenchHelpPlugin getDefault() {
		return plugin;
	}
	public void initializeFromStore() {
		IPreferenceStore ini = getPreferenceStore();
		HelpSystem.setInstall(ini.getInt(HelpPreferencePage.INSTALL_OPTION_KEY));
		HelpSystem.setRemoteServerInfo(
			ini.getString(HelpPreferencePage.SERVER_PATH_KEY));
		if (ini.getInt(HelpPreferencePage.LOCAL_SERVER_CONFIG) > 0) {
			HelpSystem.setLocalServerInfo(
				ini.getString(HelpPreferencePage.LOCAL_SERVER_ADDRESS_KEY),
				ini.getString(HelpPreferencePage.LOCAL_SERVER_PORT_KEY));
		} else {
			HelpSystem.setLocalServerInfo(null, "0");
		}
		HelpSystem.setDebugLevel(ini.getInt(HelpPreferencePage.LOG_LEVEL_KEY));
		HelpSystem.setBrowserPath(ini.getString(HelpPreferencePage.BROWSER_PATH_KEY));

	}
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		HelpSystem.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() {
		if (getWorkbench() != null)
			initializeFromStore();
		HelpSystem.startup();
	}
}