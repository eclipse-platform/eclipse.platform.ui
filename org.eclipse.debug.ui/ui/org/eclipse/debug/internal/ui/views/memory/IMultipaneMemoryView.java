/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

public interface IMultipaneMemoryView {
	
	/**
	 * @param paneId
	 * @return the memory pane with the specified pane id
	 * Return null if the specified view pane cannot be found.
	 */
	public IMemoryViewPane getViewPane(String paneId);
	
	/**
	 * @return all view panes from the memory view
	 */
	public IMemoryViewPane[] getViewPanes();
	
	/**
	 * Show or hide the specified view pane
	 * @param show
	 * @param paneId
	 */
	public void showViewPane(boolean show, String paneId);
	
	/**
	 * @param paneId
	 * @return if the specified is currently visible in the memory view
	 */
	public boolean isViewPaneVisible(String paneId);
}
