package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.util.HelpPreferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.*;
/**
 * Information Center Launching Action Delegate
 */
public class InfocenterAction //extends Action
implements IWorkbenchWindowActionDelegate, IExecutableExtension {
	public InfocenterAction() {
	}
	public void dispose() {
	}
	/**
	 * Initializes the action delegate with the workbench window it will work in.
	 *
	 * @param window the window that provides the context for this delegate
	 */
	public void init(IWorkbenchWindow window) {
	}
	/**
	 * Implementation of method defined on <code>IAction</code>.
	 *
	 * [Issue: Will be made abstract. For now, calls <code>actionPerformed()</code> for backwards compatibility.]
	 */
	public void run(IAction a) {
		//This may take a while, so use the busy indicator
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				String url =
					HelpSystem.getPreferences().getString(HelpPreferences.INFOCENTER_URL_KEY);
				if (url.trim().length() > 0)
					WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(url);
				else
					ErrorUtil.displayInfoDialog(
						WorkbenchResources.getString("configure_infocenter"));
			}
		});
	}
	/**
	 * Selection in the workbench has changed. Plugin provider
	 * can use it to change the availability of the action
	 * or to modify other presentation properties.
	 *
	 * <p>Action delegate cannot be notified about
	 * selection changes before it is loaded. For that reason,
	 * control of action's enable state should also be performed
	 * through simple XML rules defined for the extension
	 * point. These rules allow enable state control before
	 * the delegate has been loaded.</p>
	 *
	 * @param action action proxy that handles presentation
	 * portion of the plugin action
	 * @param selection current selection in the workbench
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	/**
	 * Initializes the action with data from the xml declaration
	 */
	public void setInitializationData(
		IConfigurationElement cfig,
		String propertyName,
		Object data) {
	}
}