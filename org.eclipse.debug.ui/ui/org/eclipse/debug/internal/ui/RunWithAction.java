package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchManager;

public class RunWithAction extends LaunchWithAction {
	
	public RunWithAction() {
		super(ILaunchManager.RUN_MODE);
	}
}
