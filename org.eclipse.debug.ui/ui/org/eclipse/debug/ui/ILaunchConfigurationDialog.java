package org.eclipse.debug.ui;

import org.eclipse.jface.operation.IRunnableContext;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ILaunchConfigurationDialog extends IRunnableContext {
	
	/**
	 * Refresh any status information in this dialog.
	 */
	public void refreshStatus();
}
