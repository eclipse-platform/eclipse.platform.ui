package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.server.HelpServer;
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
	}
	/**
	 * @return com.ibm.ua.HelpViewerPlugin
	 */
	public static WorkbenchHelpPlugin getDefault() {
		return plugin;
	}
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		HelpServer.instance().close();
		super.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() {
		HelpServer.instance();
	}
}