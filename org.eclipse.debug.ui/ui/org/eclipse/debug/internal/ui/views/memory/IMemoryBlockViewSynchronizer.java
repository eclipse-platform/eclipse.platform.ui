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

import org.eclipse.debug.core.model.IMemoryBlock;


/**
 * Allows for synchronization for any view that displays a memory block with
 * the Memory View.  When a view needs to be synchronized, the view 
 * needs to implement ISynchronizedMemoryBlockView interface and adds itself
 * to the synchronizer.  The view can define properties for synchronization.
 * It is up to the view to handle synchronized properties changed events.
 * 
 * @since 3.0
 */
public interface IMemoryBlockViewSynchronizer
{
	/**
	 * Add the view to the synchronizer.  The view will
	 * be notified when one of the synchronization properties has changed.
	 * @param view - the view  listening for property changed events.
	 * @param filters - list of properties the view tab is interested in.
	 * Null if view tab is interested in all events.
	 */
	public void addView(ISynchronizedMemoryBlockView view, String[] filters);
	
	/**
	 * Remove the view from the synchronizer.  The view 
	 * will no longer be notified about changes in synchronization properties.
	 * @param view
	 */
	public void removeView(ISynchronizedMemoryBlockView view);
	
	/**
	 * Add listener to the synchronizer.  Listener will be notified
	 * when the enablement of the synchronizer has changed.
	 * @param listener
	 */
	public void addSynchronizerListener(ISynchronizerListener listener);
	
	/**
	 * Remove listener from synchronizer.
	 * @param listener
	 */
	public void removeSynchronizerListener(ISynchronizerListener listener);
	
	/**
	 * Enable/disable the synchronizer.  Synchronizer does not fire
	 * property change events if it is disabled.
	 * @param enabled 
	 */
	public void setEnabled(boolean enabled);
	
	/**
	 * @return if the synchronizer is currently enabled
	 */
	public boolean isEnabled();
	
	/**
	 * Sets a property to the synchronizer to be synchronized.
	 * A change event will be fired if the value provided is different from the
	 * value stored in the synchronizer.  Otherwise, no change event will be
	 * fired.
	 * @param memoryBlock
	 * @param propertyId
	 * @param value
	 */
	public void setSynchronizedProperty(IMemoryBlock memoryBlock, String propertyId, Object value);
	
	/**
	 * Get the property from the synchronizer for a memory block
	 * @param memoryBlock
	 * @param propertyId
	 * @return the synchronized property for the given memory block and property id
	 */
	public Object getSynchronizedProperty(IMemoryBlock memoryBlock, String propertyId);
	
	/**
	 * Set up a list of properties that the view wishes to be notified about when the properties
	 * have changed.  
	 * @param view - view listening for property changed events.
	 * @param filters - properties that the view are interested in.  Synchronizer will filter
	 * out events not listed in the filters.  Enter null when the view wishes to listen to all events.
	 */
	public void setPropertyFilters(ISynchronizedMemoryBlockView view, String[] filters);
}


