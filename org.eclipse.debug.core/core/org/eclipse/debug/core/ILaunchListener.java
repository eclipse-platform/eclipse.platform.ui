package org.eclipse.debug.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * A launch listener is notified of launches as they
 * are added and removed from the launch manager. Also,
 * when a process or debug target is added to a launch,
 * listeners are notified of a change.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see org.eclipse.debug.core.ILaunch
 * @see org.eclipse.debug.core.ILaunchesListener
 */
public interface ILaunchListener {	
	/**
	 * Notifies this listener that the specified
	 * launch has been removed.
	 *
	 * @param launch the removed launch
	 * @since 2.0
	 */
	public void launchRemoved(ILaunch launch);
	/**
	 * Notifies this listener that the specified launch
	 * has been added.
	 * 
	 * @param launch the newly added launch
	 * @since 2.0
	 */
	public void launchAdded(ILaunch launch);	
	/**
	 * Notifies this listener that the specified launch
	 * has changed. For example, a process or debug target
	 * has been added to the launch.
	 * 
	 * @param launch the changed launch
	 * @since 2.0
	 */
	public void launchChanged(ILaunch launch);	
}
