package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

public class RelaunchActionDelegate extends ControlActionDelegate {
	
	private static final String PREFIX= "relaunch_action.";
	private static final String ERROR= "error.";
	private static final String LAUNCH_PREFIX= "launch_action.";
	private static final String LAUNCH_ERROR_TITLE= LAUNCH_PREFIX + ERROR + "title";
	private static final String LAUNCH_ERROR_MESSAGE= LAUNCH_PREFIX + ERROR + "message";		

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
			String string= DebugUIUtils.getResourceString(LAUNCH_ERROR_MESSAGE);
			String message= MessageFormat.format(string, new String[] {launcher.getLabel()});
			MessageDialog.openError(DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), DebugUIUtils.getResourceString(LAUNCH_ERROR_TITLE), message);	
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
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.RELAUNCH_ACTION;
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {
	}		
}