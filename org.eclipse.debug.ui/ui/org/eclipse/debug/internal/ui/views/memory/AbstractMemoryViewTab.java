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

import java.util.ArrayList;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.TabItem;

/**
 * This class is created to help maintain reference counting to a  memory block.
 * Plugins that wish to contribute a new rendering to Memory Rendering View can extend
 * from this class.  When a view tab is enabled, a reference will be addd to a reference
 * counting object in the synchronizer.  When a view tab is disabled, a reference will be removed
 * to a reference counting object in the synchronizer.  Memory Block will be enabled / disabled
 * based on this reference counting object.  If setEnabled is to be overriden, implementor must call
 * AbstractMemoryViewTab.setEnabled() or handle reference counting by itself.
 * 
 * @since 3.0
 */
abstract public class AbstractMemoryViewTab implements IMemoryViewTab {
	
	protected IMemoryBlock fMemoryBlock;
	protected TabItem fTabItem;
	protected MenuManager fMenuMgr;
	protected IMemoryRendering fRendering;
	
	public AbstractMemoryViewTab(IMemoryBlock newMemory, TabItem newTab, MenuManager menuMgr, IMemoryRendering rendering)
	{
		fMemoryBlock = newMemory;
		fTabItem = newTab;
		fMenuMgr = menuMgr;
		fRendering = rendering;
		
		fTabItem.setData(this);
		
		// Whenever a new view tab is created, enable the tab to ensure
		// that reference counting and memory block enablement state is
		// maintained.
		maintainRefAndEnablement(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		maintainRefAndEnablement(enabled);
	}
	
	/**
	 * Maintain memory enabled reference counting and its enablement
	 * If addReference is true, a reference count will be added to the memory block.
	 * The memory block will be enabled if there is more than one object referencing to the
	 * memory block.
	 * If addReference is false, a refernece count will be removed from the memory block
	 * The memory block will be disabled if no object is referencing to the memory block.
	 * @param addReference
	 */
	protected void maintainRefAndEnablement(boolean addReference)
	{
		// if add enabled reference
		if (addReference)
		{
			if (getMemoryBlock() instanceof IExtendedMemoryBlock)
			{
				// add view tab to synchronizer
				ArrayList references = addReferenceToSynchronizer();
				
				// if this is the first enabled reference to the memory block
				// enable the memory block
				if (references.size() == 1 && !((IExtendedMemoryBlock)getMemoryBlock()).isEnabled() )
					((IExtendedMemoryBlock)getMemoryBlock()).enable();
			}
		}
		// if remove enabled reference
		else if (!addReference){
			if (getMemoryBlock() instanceof IExtendedMemoryBlock)
			{
				ArrayList references = removeReferenceFromSynchronizer();
				
				if (references == null)
					return;
				
				// if there is not more enabled reference to the memory block
				// disable the memory block
				if (references.size() == 0 && ((IExtendedMemoryBlock)getMemoryBlock()).isEnabled())							
					((IExtendedMemoryBlock)getMemoryBlock()).disable();
			}			
		}		
	}
	
	/**
	 * 	Multiple view tabs can reference to the same memory block.
	 *	Use this property to keep track of all references that are enabled.
	 *	(i.e.  requiring change events from the memory block.)
	 *	When a view tab is created/enabled, a reference will be added to the synchronizer.
	 *	When a view tab is disposed/disabled, the reference will be removed from the synchronizer.
	 *	The view tab examines this references array and will only enable
	 *	a memory block if there is at least one enabled reference to the memory
	 *	block.  If there is no enabled reference to the memory block, the
	 *	memory block should be disabled.
	 * @return the reference object
	 */
	protected ArrayList addReferenceToSynchronizer() {

		ArrayList references = (ArrayList)DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer().getSynchronizedProperty(getMemoryBlock(), IMemoryViewConstants.PROPERTY_ENABLED_REFERENCES);
		
		// first reference count
		if (references == null)
		{
			references = new ArrayList();
		}
		
		// add the reference to the reference counting object
		if (!references.contains(this))
		{
			references.add(this);
		}
		
		DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer().setSynchronizedProperty(getMemoryBlock(), IMemoryViewConstants.PROPERTY_ENABLED_REFERENCES, references);
		return references;
	}
	
	/**
	 * @return the reference object, null if the reference object does not exisit in the synchronizer
	 */
	protected ArrayList removeReferenceFromSynchronizer()
	{
		ArrayList references = (ArrayList)DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer().getSynchronizedProperty(getMemoryBlock(), IMemoryViewConstants.PROPERTY_ENABLED_REFERENCES);
		
		// do not create a new reference object if it does not exist
		// the memory block may have been deleted
		if (references == null)
		{
			return null;
		}
		
		// remove the reference from the reference counting object
		references.remove(this);
		
		DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer().setSynchronizedProperty(getMemoryBlock(), IMemoryViewConstants.PROPERTY_ENABLED_REFERENCES, references);
		return references;		
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#dispose()
	 */
	public void dispose() {
		
		// whenever a view tab is disposed, disable view tab to ensure that
		// reference counting and memory block enablement state is maintained.
		maintainRefAndEnablement(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#getMemoryBlock()
	 */
	public IMemoryBlock getMemoryBlock() {
		return fMemoryBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#getRenderingId()
	 */
	public String getRenderingId() {
		return fRendering.getRenderingId();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTab#getRendering()
	 */
	public IMemoryRendering getRendering()
	{
		return fRendering;
	}

}
