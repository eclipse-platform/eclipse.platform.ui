package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

public class DisableBreakpointsAction extends EnableBreakpointsAction {
	
	/**
	 * This action disables breakpoints.
	 */
	protected boolean isEnableAction() {
		return false;
	}
}
