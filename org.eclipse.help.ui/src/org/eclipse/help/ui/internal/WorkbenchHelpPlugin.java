package org.eclipse.help.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.help.ui.internal.workingset.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;

/**
  * This class is a UI plugin. This may need to change to regular 
  * plugin if the plugin class is moved into the base help.
  */
public class WorkbenchHelpPlugin extends AbstractUIPlugin {
	private static WorkbenchHelpPlugin plugin;
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
		HelpSystem.setDefaultErrorUtil(new ErrorUtil());
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
		return HelpSystem.getHelpBrowser();
	}

	public HelpWorkingSetSynchronizer getWorkingSetSynchronizer() {
		return workingSetListener;
	}
}