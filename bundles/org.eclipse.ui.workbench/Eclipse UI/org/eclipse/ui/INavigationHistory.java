/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui;



/**
 * 2.1 - WORK_IN_PROGRESS. Do not use.
 */




/**
 * Manages a list of entries to keep history of locations on editors
 * to enable the user to go back and forward without losing context.
 * 
 * The history is a list of <code>INavigationLocation</code> and a pointer
 * to the current location. Whenever the back or forward action runs the
 * history restores the previous or next location.
 *
 * The back and/or forward actions should not change the content of the history
 * in any way.
 * 
 * If the user steps N times in one direction (back or forward) and then N times to
 * the oposit direction, the editor and location should be exactly the same as if
 * nothing as done.
 * 
 * Not intended to be implemented by clients.
 */
public interface INavigationHistory {
	/**
	 * Mark the current location into the history. This message is sent
	 * by the workbench whenever an editors is activated or deactivated. 
	 * and should be sent by clients (editors) whenever significant changes
	 * in location are detected.
	 * 
	 * The location is obtened by calling <code>INavigationLocationProvider.createNavigationLocation</code>	 */
	public void markLocation();
	/**
	 * Return the current location;
	 * 	 * @return INavigationLocation the current location	 */
	public INavigationLocation getCurrentLocation();
	/**
	 * Return all entries in the history.
	 * 	 * @return INavigationLocation[]	 */
	public INavigationLocation[] getLocations();
}