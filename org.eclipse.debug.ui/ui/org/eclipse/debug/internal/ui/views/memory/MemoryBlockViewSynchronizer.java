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

import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.memory.IMemoryBlockListener;
import org.eclipse.debug.internal.core.memory.MemoryBlockManager;


/**
 * Synchronizer to handle synchronization between Memory View and Memory
 * rendering View.
 * 
 * @since 3.0
 */
public class MemoryBlockViewSynchronizer implements IMemoryBlockViewSynchronizer, IMemoryBlockListener
{	
	private Hashtable fSynchronizeInfo;	
		
	public MemoryBlockViewSynchronizer()
	{
		fSynchronizeInfo = new Hashtable();
		MemoryBlockManager.getMemoryBlockManager().addListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockViewSynchronizer#addViewTab(org.eclipse.debug.ui.ISynchronizedMemoryBlockView)
	 */
	public void addView(ISynchronizedMemoryBlockView view, String[] filters)
	{
		IMemoryBlock key = view.getMemoryBlock();

		if (fSynchronizeInfo.get(key) == null)
		{
			// create a synchronize info object for the memory block
			SynchronizeInfo newInfo = new SynchronizeInfo(view.getMemoryBlock());
			
			fSynchronizeInfo.put(key, newInfo);
			
			newInfo.addSynchronizedView(view, filters);
		}
		else
		{
			SynchronizeInfo info = (SynchronizeInfo)fSynchronizeInfo.get(key);
			info.addSynchronizedView(view, filters);	
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockViewSynchronizer#removeViewTab(org.eclipse.debug.ui.ISynchronizedMemoryBlockView)
	 */
	public void removeView(ISynchronizedMemoryBlockView view)
	{
		IMemoryBlock key = view.getMemoryBlock();
		
		if (fSynchronizeInfo.get(key) == null)
		{
			return;
		}
		SynchronizeInfo info = (SynchronizeInfo)fSynchronizeInfo.get(key);
		info.removeSynchronizedView(view);				
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockViewSynchronizer#setSynchronizedProperty(org.eclipse.debug.ui.ISynchronizedMemoryBlockView, java.lang.String, java.lang.Object)
	 */
	public void setSynchronizedProperty(IMemoryBlock memoryBlock, String propertyId, Object value)
	{
		// find the synchronize info object for the memory block
		SynchronizeInfo info = (SynchronizeInfo)fSynchronizeInfo.get(memoryBlock);
		
		// if info is not available, need to create one to hold the property
		if (info == null)
		{
			info = new SynchronizeInfo(memoryBlock);
			fSynchronizeInfo.put(memoryBlock, info);
		}
		
		// get the value of the property
		Object oldValue = info.getProperty(propertyId);
		
		if (oldValue == null)
		{
			// if the value has never been added to the info object
			// set the property and fire a change event
			info.setProperty(propertyId, value);
			info.firePropertyChanged(propertyId);
			return;
		}
		else if (!oldValue.equals(value))
		{
			// if the value has changed
			// set the property and fire a change event
			info.setProperty(propertyId, value);
			info.firePropertyChanged(propertyId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockViewSynchronizer#getSynchronizedProperty(org.eclipse.debug.ui.ISynchronizedMemoryBlockView, java.lang.String)
	 */
	public Object getSynchronizedProperty(IMemoryBlock memoryBlock, String propertyId)
	{
		SynchronizeInfo info = (SynchronizeInfo)fSynchronizeInfo.get(memoryBlock);
		
		if (info != null)
		{
			Object value = info.getProperty(propertyId);
			return value;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockListener#MemoryBlockAdded(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void MemoryBlockAdded(IMemoryBlock memory) {
		// do nothing when a memory block is added
		// create a synchronize info object when there is a fView
		// tab registered to be synchronized.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockListener#MemoryBlockRemoved(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void MemoryBlockRemoved(IMemoryBlock memory) {
		
		// delete the info object and remove it from fSynchronizeInfo
		// when the memory block is deleted
		SynchronizeInfo info = (SynchronizeInfo)fSynchronizeInfo.get(memory);
		
		if (info != null)
		{	
			info.delete();
			fSynchronizeInfo.remove(memory);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockViewSynchronizer#addProperties(org.eclipse.debug.ui.ISynchronizedMemoryBlockView, java.lang.String[])
	 */
	public void setPropertyFilters(ISynchronizedMemoryBlockView view, String[] filters) {
		IMemoryBlock blk = view.getMemoryBlock();
		
		SynchronizeInfo info = (SynchronizeInfo)fSynchronizeInfo.get(blk);
		
		if (info != null){
			info.setPropertyFilters(view, filters);
		}
	}
	
	/**
	 * Clean up when the plugin is shutdown
	 */
	public void shutdown()
	{
		if (fSynchronizeInfo != null)
		{	
			Enumeration enumeration = fSynchronizeInfo.elements();
			
			// clean up all synchronize info objects
			while (enumeration.hasMoreElements()){
				SynchronizeInfo info = (SynchronizeInfo)enumeration.nextElement();
				info.delete();
			}
			
			fSynchronizeInfo.clear();
			fSynchronizeInfo = null;
		}
	}
}
