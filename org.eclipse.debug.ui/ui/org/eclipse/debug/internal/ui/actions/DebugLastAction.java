package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.debug.core.ILaunchManager;

/**
 * Relaunches the last debug-mode launch
 */
public class DebugLastAction extends RelaunchLastAction {
	
	/**
	 * @see RelaunchLastAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}	
}
