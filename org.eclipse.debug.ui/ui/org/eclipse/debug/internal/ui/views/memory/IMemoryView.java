/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;


/**
 * Functions to allow user to control the Memory View or the Memory Rendering View
 * 
 * @since 3.0
 */
public interface IMemoryView
{
	/**
	 * @return the top view tab from the Memory View
	 */
	public IMemoryViewTab getTopMemoryTab( );
	
	/**
	 * @return all view tabs from current tab folder
	 */
	public IMemoryViewTab[] getAllViewTabs();
	
	/**
	 * Move specified view tab to the top
	 * @param viewTab
	 */
	public void moveToTop(IMemoryViewTab viewTab);
	
}
