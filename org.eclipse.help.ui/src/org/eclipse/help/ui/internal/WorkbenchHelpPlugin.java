package org.eclipse.help.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.appserver.WebappManager;
import org.eclipse.help.ui.browser.IBrowser;
import org.eclipse.help.ui.internal.browser.BrowserManager;
import org.eclipse.help.ui.internal.workingset.HelpWorkingSetSynchronizer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
  * This class is a UI plugin. This may need to change to regular 
  * plugin if the plugin class is moved into the base help.
  */
public class WorkbenchHelpPlugin extends AbstractUIPlugin {
	private static WorkbenchHelpPlugin plugin;
	private IBrowser browser;
	private HelpWorkingSetSynchronizer workingSetListener;

	/**
	 * WorkbenchHelpPlugin constructor. It is called as part of plugin
	 * activation.
	 */
	public WorkbenchHelpPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
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
		WebappManager.stop("help");
		BrowserManager.getInstance().closeAll();
		if (HelpSystem.getMode() == HelpSystem.MODE_WORKBENCH) {
			PlatformUI
				.getWorkbench()
				.getWorkingSetManager()
				.removePropertyChangeListener(
				workingSetListener);
			HelpSystem.getWorkingSetManager().removePropertyChangeListener(
				workingSetListener);
		}
		super.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() {
		if (HelpSystem.getMode() == HelpSystem.MODE_WORKBENCH) {
			// register the working set listener to keep the ui and the help working sets in sych
			workingSetListener = new HelpWorkingSetSynchronizer();
			PlatformUI
				.getWorkbench()
				.getWorkingSetManager()
				.addPropertyChangeListener(
				workingSetListener);
			HelpSystem.getWorkingSetManager().addPropertyChangeListener(
				workingSetListener);
		}
	}

	public IBrowser getHelpBrowser() {
		if (browser == null)
			browser = BrowserManager.getInstance().createBrowser();
		return browser;
	}

	public HelpWorkingSetSynchronizer getWorkingSetSynchronizer() {
		return workingSetListener;
	}
}