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
	 * @return the top view tab from the Memory View from the specified pane
	 */
	public IMemoryViewTab getTopMemoryTab(String paneId);
	
	/**
	 * @return all view tabs from current tab folder from the specified pane
	 */
	public IMemoryViewTab[] getAllViewTabs(String paneId);
	
	/**
	 * Move specified view tab to the top in the specified pane
	 * @param viewTab
	 */
	public void moveToTop(String paneId, IMemoryViewTab viewTab);
}
