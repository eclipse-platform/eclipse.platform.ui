package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.IAppServer;
import org.eclipse.help.internal.server.HelpServer;
import org.eclipse.help.ui.browser.IBrowser;
import org.eclipse.help.ui.internal.browser.BrowserManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
/**
  * This class is a UI plugin. This may need to change to regular 
  * plugin if the plugin class is moved into the base help.
  */
public class WorkbenchHelpPlugin extends AbstractUIPlugin {
	private final static String APP_SERVER_EXTENSION_ID =
		"org.eclipse.help.app-server";
	private static final String APP_SERVER_CLASS_ATTRIBUTE = "class";
	private static WorkbenchHelpPlugin plugin;
	private IAppServer appServer;
	private IBrowser browser;
	/**
	 * WorkbenchHelpPlugin constructor. It is called as part of plugin
	 * activation.
	 */
	public WorkbenchHelpPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		// set dynamic address
		appServer = getAppServer();
		if (appServer != null)
			appServer.setAddress(null, 80);
	}
	/**
	 * @return HelpViewerPlugin
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
		// stop the web app
		if (getAppServer() != null) {
			getAppServer().remove("help", "org.eclipse.help.webapp");
		}
		BrowserManager.getInstance().closeAll();
		HelpServer.instance().close();
		super.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() {
		// get an app server and start the help web app
		if (getAppServer() != null) {
			getAppServer().add("help", "org.eclipse.help.webapp", "");
		}
		HelpServer.instance();
	}
	public IAppServer getAppServer() {
		if (appServer == null) {
			// Initializes the app server by getting an instance via 
			// app-server the extension point
			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
				public void run() {
					// get the app server extension from the system plugin registry	
					IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
					IExtensionPoint point =
						pluginRegistry.getExtensionPoint(APP_SERVER_EXTENSION_ID);
					if (point == null)
						return;
					IExtension[] extensions = point.getExtensions();
					if (extensions.length == 0)
						return;
					// There should only be one extension/config element so we just take the first
					IConfigurationElement[] elements = extensions[0].getConfigurationElements();
					if (elements.length == 0)
						return;
					// Instantiate the app server
					try {
						appServer =
							(IAppServer) elements[0].createExecutableExtension(APP_SERVER_CLASS_ATTRIBUTE);
					} catch (CoreException e) {
						WorkbenchHelpPlugin.this.getLog().log(e.getStatus());
					}
				}
			});
		}
		return appServer;
	}
	public IBrowser getHelpBrowser() {
		if (browser == null)
			browser = BrowserManager.getInstance().createBrowser();
		return browser;
	}
}