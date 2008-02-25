/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.help.internal.base.remote.RemoteIC;


public interface IRemoteHelpListViewer {

	/**
	 * Add a help entry to the model and update the view 
	 * 
	 * @param RemoteIC
	 */
	public void addRemoteIC(RemoteIC remote_ic);

	/**
	 * Remove a help entry from the model and update the view 
	 * 
	 * @param RemoteIC
	 */
	public void removeRemoteIC(RemoteIC remote_ic);

	/**
	 * Update an entry in the model 
	 * @param RemoteIC
	 */
	public void updateRemoteIC(RemoteIC remote_ic);

	/**
	 * Refresh an entry in the model at the specified index
	 * @param RemoteIC
	 */
	public void refreshRemoteIC(RemoteIC remote_ic, int selectedIndex);
	
	/**
	 * Remove all entries in the model 
	 * @param RemoteIC
	 */	
	public void removeAllRemoteICs(Object [] remoteICs);
}
