package org.eclipse.debug.internal.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

/**
 * Implementors of this interface are notified whenever a change is made to the launch history.
 * This could be an addition or deletion from either of the run or debug histories, or change
 * to the last launched item.
 */
public interface ILaunchHistoryChangedListener {

	/**
	 * Notification that the launch history has changed.  Any of the run history, debug history
	 * or last launched items could have changed.  To examine the history items, retrieve them
	 * from the <code>DebugUIPlugin</code>.
	 */
	public void launchHistoryChanged();
}
