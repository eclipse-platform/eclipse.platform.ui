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
	
	// Enabled Reference of the memory block
	// This property acts as a reference count object for the memory block blocked
	// by view tabs from Memory View or Memory Rendering View.  When a view tab
	// is enabled, it needs to add itself to this property object.
	// When a view tab is disabled, it removes itself from this property object.  This
	// property is used to determine if a memory block can be disabled when no more
	// view tab is referencing to the memory block.
	public static final String PROPERTY_ENABLED_REFERENCES = "org.eclipse.debug.ui.MemoryViewTab.enabledReferences"; //$NON-NLS-1$
}
