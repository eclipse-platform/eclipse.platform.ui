package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;

public class RelaunchActionDelegate extends AbstractDebugActionDelegate {
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
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
	
	public static void relaunch(LaunchConfigurationHistoryElement history) {
		relaunch(history.getLaunchConfiguration(), history.getMode());			
	}
		
	public static void relaunch(IDebugElement element) {
		relaunch(element.getLaunch());
	}
	
	public static void relaunch(IProcess process) {
		relaunch(process.getLaunch());
	}
	
	public static void relaunch(ILaunch launch) {
		relaunch(launch.getLaunchConfiguration(), launch.getLaunchMode());
	}
	
	public static void relaunch(ILaunch launch, String mode) {
		relaunch(launch.getLaunchConfiguration(), mode);
	}
	
	/**
	 * Re-launches the given configuration in the specified mode.
	 */
	public static void relaunch(ILaunchConfiguration config, String mode) {
		try {
			config.launch(mode, null);		
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), ActionMessages.getString("RelaunchActionDelegate.Launch_Failed_1"), ActionMessages.getString("RelaunchActionDelegate.An_exception_occurred_while_launching_2"), ce); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		ILaunch launch= null;
		if (element instanceof ILaunch) {
			launch= (ILaunch)element;
		} else if (element instanceof IDebugElement) {
			launch= ((IDebugElement)element).getLaunch();
		} else if (element instanceof IProcess) {
			launch= ((IProcess)element).getLaunch();
		}
		
		return launch != null && launch.getLaunchConfiguration() != null;
	}
			
	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("RelaunchActionDelegate.Launch_Failed_1"); //$NON-NLS-1$
	}
	
	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("RelaunchActionDelegate.Launch_1"); //$NON-NLS-1$
	}
	
	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("RelaunchActionDelegate.An_exception_occurred_while_launching_2"); //$NON-NLS-1$
	}
}