package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchManager;

public class DebugWithAction extends LaunchWithAction {
	
	public DebugWithAction() {
		super(ILaunchManager.DEBUG_MODE);
	}
}
