package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

public class RelaunchActionDelegate extends ControlActionDelegate {
	
	/**
	 * @see ControlActionDelegate
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
		boolean ok= launcher.launch(new Object[]{element}, mode);
		if (!ok) {
			String string= DebugUIMessages.getString("RelaunchActionDelegate.Launch_attempt_failed__{0}_1"); //$NON-NLS-1$
			String message= MessageFormat.format(string, new String[] {launcher.getLabel()});
			MessageDialog.openError(DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), DebugUIMessages.getString("RelaunchActionDelegate.Launch_failed_2"), message);	 //$NON-NLS-1$
		}				
	}
	
	public static void relaunch(LaunchHistoryElement history) {
		relaunch(history.getLauncher(), history.getMode(), history.getLaunchElement());
	}
	
	public static void relaunch(IDebugElement element) {
		relaunch(element.getLaunch());
	}
	
	public static void relaunch(IProcess process) {
		relaunch(process.getLaunch());
	}
	
	public static void relaunch(ILaunch launch) {
		relaunch(launch.getLauncher(), launch.getLaunchMode(), launch.getElement());
	}
	
	public static void relaunch(ILaunch launch, String mode) {
		relaunch(launch.getLauncher(), mode, launch.getElement());
	}
	

	/**
	 * @see ControlActionDelegate
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

		return launch != null && DebugUIPlugin.getDefault().isVisible(launch.getLauncher());
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.RELAUNCH_ACTION;
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_RELAUNCH));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RELAUNCH));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RELAUNCH));
	}		
	/*
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return DebugUIMessages.getString("RelaunchActionDelegate.Launch_failed_2"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return null;
	}

	/*
	 * @see ControlActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return null;
	}
	/*
	 * @see ControlActionDelegate#getToolTipText()
	 */
	protected String getToolTipText() {
		return DebugUIMessages.getString("RelaunchActionDelegate.Relaunch_3"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return DebugUIMessages.getString("RelaunchActionDelegate.Re&launch_4"); //$NON-NLS-1$
	}
}