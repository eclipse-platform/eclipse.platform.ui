package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

public class DisableBreakpointsAction extends EnableBreakpointsAction {
	
	/**
	 * When there is a multi-selection, this method will indicate which way
	 * the breakpoints will be toggled
	 */
	protected boolean isEnableAction() {
		return false;
	}
}
