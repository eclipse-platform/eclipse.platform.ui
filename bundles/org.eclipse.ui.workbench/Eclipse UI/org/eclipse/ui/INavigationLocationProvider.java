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
 * Should be implemented by editors that wish to contribute to the
 * navigation history. The message <code>createNavigationLocation</code>
 * will be sent when a new location is marked in the history.
 * 
 * The provider must guaranty that the current location is
 * always in the history. Independent if that is done by adding
 * a new location or by updating the current location.
 */
public interface INavigationLocationProvider {
	/**
	 * Creates an empty navigation location. The message <code>restoryState</code>
	 * will be sent to the location to restore its state.
	 * 
	 * @return INavigationLocation
	 */
	public INavigationLocation createLocation();	
	/**
	 * Creates a navigation location describing the current state.
	 * 	 * @return INavigationLocation	 */
	public INavigationLocation createCurrentLocation();
}
