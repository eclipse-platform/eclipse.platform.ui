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


/**
 * Constants for Memory View and Memory Rendering View
 * These constants can be accessed by third-party plugins
 * TODO:  Move this to correct constant class
 * 
 * @since 3.0
 */
public interface IMemoryViewConstants {
	
	// Default renderings provided
	public static final String RENDERING_RAW_MEMORY = "org.eclipse.debug.ui.rendering.raw_memory"; //$NON-NLS-1$


	// Properties to be synchronized by MemoryViewTab
	// Memory View and Memory Rendering View make use of these property ids
	// to synchronize the cursor, scroll bar and column size

	// Address highlighted by the cursor in MemoryViewTab
	public static final String PROPERTY_SELECTED_ADDRESS = "org.eclipse.debug.ui.MemoryViewTab.selectedAddress"; //$NON-NLS-1$

	// Column size of StoageViewTab
	public static final String PROPERTY_COL_SIZE = "org.eclipse.debug.ui.MemoryViewTab.columnSize"; //$NON-NLS-1$
	
	// Top visble address of MemoryViewTab
	public static final String PROPERTY_TOP_ADDRESS = "org.eclipse.debug.ui.MemoryViewTab.topAddress"; //$NON-NLS-1$
	
}
