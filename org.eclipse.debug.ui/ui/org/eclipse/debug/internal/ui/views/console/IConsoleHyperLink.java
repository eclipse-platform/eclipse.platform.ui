package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.text.IRegion;

/**
 */
public interface IConsoleHyperLink extends IRegion {
	
	/**
	 * Notification that the mouse has entered this link's region.
	 */
	public void linkEntered();
	
	/**
	 * Notification that the mouse has exited this link's region
	 */
	public void linkExited();
	
	/**
	 * Notification that this link has been activated. This link should
	 * perform what ever action is associated with the link.
	 */
	public void linkActivated();

}
