package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

public class DisableBreakpointsAction extends EnableBreakpointsAction {
	
	/**
	 * This action disables breakpoints.
	 */
	protected boolean isEnableAction() {
		return false;
	}
}
