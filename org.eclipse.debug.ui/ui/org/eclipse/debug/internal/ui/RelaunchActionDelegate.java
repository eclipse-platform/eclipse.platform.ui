package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;

public class RelaunchActionDelegate extends ControlActionDelegate {
	
	private static final String PREFIX= "relaunch_action.";

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object object) {
		relaunch(object, null);
	}

	/**
	 * Re-launches the launch of the given object.
	 */
	public static void relaunch(Object object) {
		relaunch(object, null);
	}

	/**
	 * Re-launches the launch of the given object in the specified mode.
	 */
	public static void relaunch(Object object, String mode) {
		ILaunch launch= null;
		if (object instanceof IDebugElement) {
			launch= ((IDebugElement)object).getLaunch();
		} else if (object instanceof ILaunch) {
			launch= (ILaunch)object;
		} else if (object instanceof IProcess) {
			launch= ((IProcess)object).getLaunch();
		}
		if (launch != null) {
			ILauncher launcher= launch.getLauncher();
			Object element= launch.getElement();
			String launchMode= (mode == null) ? launch.getLaunchMode() : mode;
			launcher.launch(new Object[]{element}, launchMode);
		}
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof ILaunch || element instanceof IDebugElement || element instanceof IProcess;
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
}