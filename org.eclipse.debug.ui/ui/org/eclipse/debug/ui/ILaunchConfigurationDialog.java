package org.eclipse.debug.ui;

import org.eclipse.jface.operation.IRunnableContext;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ILaunchConfigurationDialog extends IRunnableContext {
	
	/**
	 * Constant used as return value from <code>open()</code> method of a
	 * launch configuration dialog.
	 */
	public static final int SINGLE_CLICK_LAUNCHED = 2;
	
	/**
	 * Refresh any status information in this dialog.
	 */
	public void refreshStatus();
}
