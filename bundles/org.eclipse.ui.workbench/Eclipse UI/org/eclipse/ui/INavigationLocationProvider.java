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
 * Should be implemented by editors that wish to contribute to the
 * navigation history. The message <code>createNavigationLocation</code>
 * will be sent when a new location is marked in the history.
 * 
 * @since 2.1
 */
public interface INavigationLocationProvider {
	/**
	 * Creates an empty navigation location. The message <code>restoryState</code>
	 * will be sent to the location to restore its state.
	 * 
	 * @return INavigationLocation
	 */
	public INavigationLocation createEmptyNavigationLocation();	
	/**
	 * Creates a navigation location describing the current state.
	 * 	 * @return INavigationLocation	 */
	public INavigationLocation createNavigationLocation();
}
