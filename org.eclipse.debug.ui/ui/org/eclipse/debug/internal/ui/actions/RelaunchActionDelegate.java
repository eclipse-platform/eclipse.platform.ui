package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

public class RelaunchActionDelegate extends AbstractDebugActionDelegate {
	
	/**
	 * @see ControlActionDelegate#doAction(Object)
	 */
	protected void doAction(Object object) {
		if (object instanceof IDebugElement) {
			relaunch((IDebugElement)object);
		} else if (object instanceof ILaunch) {
			relaunch((ILaunch)object);
		} else if (object instanceof IProcess) {
			relaunch((IProcess)object);
		}
	}

	/**
	 * Re-launches the launch of the given object in the specified mode.
	 */
	public static void relaunch(ILauncher launcher, String mode, Object element) {
		launcher.launch(new Object[]{element}, mode);		
	}
	
	public static void relaunch(LaunchConfigurationHistoryElement history) {
		if (history.isConfigurationBased()) {
			relaunch(history.getLaunchConfiguration(), history.getMode());			
		} else {
			if (verifyHistoryElement(history)) {
				relaunch(history.getLauncher(), history.getMode(), history.getLaunchElement());
			} else {
				DebugUIPlugin.getDefault().removeHistoryElement(history);
			}
		}
	}
	
	/**
	 * Returns whether the launcher and launch element this history element
	 * refers to are still valid. Reports errors to the user.
	 * 
	 * @param history the launch history element to verify
	 * @return whether this given launch history element is still valid
	 */
	protected static boolean verifyHistoryElement(LaunchConfigurationHistoryElement history) {
		ILauncher launcher = history.getLauncher();
		if (launcher == null) {
			MessageDialog.openError(
				DebugUIPlugin.getShell(),
				ActionMessages.getString("RelaunchActionDelegate.Unable_to_Launch"), //$NON-NLS-1$
				ActionMessages.getString("RelaunchActionDelegate.launcher_no_longer_exists")); //$NON-NLS-1$
			return false;
		}
		Object element = history.getLaunchElement();
		if (element == null) {
		MessageDialog.openError(
				DebugUIPlugin.getShell(),
				ActionMessages.getString("RelaunchActionDelegate.Unable_to_Launch"), //$NON-NLS-1$
				ActionMessages.getString("RelaunchActionDelegate.element_no_longer_exists"));			 //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	public static void relaunch(IDebugElement element) {
		relaunch(element.getLaunch());
	}
	
	public static void relaunch(IProcess process) {
		relaunch(process.getLaunch());
	}
	
	public static void relaunch(ILaunch launch) {
		if (launch.getLaunchConfiguration() == null) {
			relaunch(launch.getLauncher(), launch.getLaunchMode(), launch.getElement());
		} else {
			relaunch(launch.getLaunchConfiguration(), launch.getLaunchMode());
		}
	}
	
	public static void relaunch(ILaunch launch, String mode) {
		if (launch.getLaunchConfiguration() == null) {
			relaunch(launch.getLauncher(), mode, launch.getElement());
		} else {
			relaunch(launch.getLaunchConfiguration(), mode);
		}
	}
	
	/*
	public static void relaunch(LaunchConfigurationHistoryElement history) {
		relaunch(history.getLaunchConfiguration(), history.getMode());
	}
	*/
	
	/**
	 * Re-launches the given configuration in the specified mode.
	 */
	public static void relaunch(ILaunchConfiguration config, String mode) {
		// This is necessary until support for the old-style (launcher-based) form
		// of launching is removed
		if (config == null) {
			return;
		}
		try {
			config.launch(mode, null);		
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), "Launch Failed", "An exception occurred while launching.", ce.getStatus());
		}
	}
	
	/**
	 * @see ControlActionDelegate#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		ILaunch launch= null;
		
		if (element instanceof ILaunch) {
			launch= (ILaunch)element;
		} else if (element instanceof IDebugElement) {
			launch= ((IDebugElement)element).getLaunch();
		} else if (element instanceof IProcess) {
			launch= ((IProcess)element).getLaunch();
		}
		
		if (launch == null) {
			return false;
		}
		if (launch.getLaunchConfiguration() == null) {
			// old launcher support
			if (launch.getLauncher() != null) {
				return DebugUIPlugin.getDefault().isVisible(launch.getLauncher());
			} 
			return false;
		} else {
			// new launch configuration support
			//relaunch is based on the launch history which is either
			//in the new or old mode.  Currently relaunch does not work for
			//launch configuration.
			return false;
		}	
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.RELAUNCH_ACTION;
	}
	
	/**
	 * @see ControlActionDelegate#setActionImages(IAction)
	 */
	protected void setActionImages(IAction action) {
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_RELAUNCH));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RELAUNCH));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RELAUNCH));
	}
			
	/**
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("RelaunchActionDelegate.Launch_failed_2"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getToolTipText()
	 */
	protected String getToolTipText() {
		return ActionMessages.getString("RelaunchActionDelegate.Relaunch_3"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return ActionMessages.getString("RelaunchActionDelegate.Re&launch_4"); //$NON-NLS-1$
	}
}