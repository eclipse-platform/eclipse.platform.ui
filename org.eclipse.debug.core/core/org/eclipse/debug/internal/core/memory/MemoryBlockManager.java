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

package org.eclipse.debug.internal.core.memory;

import java.util.ArrayList;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;


/**
 * Implementation of IMemoryBlockManager
 * The manager is responsible to manage all memory blocks in the workbench.
 * 
 * @since 3.0
 * 
 */
public class MemoryBlockManager implements IMemoryBlockManager, IDebugEventSetListener {
	
	private ArrayList listeners = new ArrayList();			// list of all IMemoryBlockListener
	private ArrayList memoryBlocks = new ArrayList();	// list of all memory blocks 
	
	private static final int ADDED = 0;
	private static final int REMOVED = 1;
	
	
	/**
	 * The singleton memory block manager.
	 */
	private static MemoryBlockManager fgMemoryBlockManager;
	
	/**
	 * The singleton memory rendering manager.
	 */
	private static MemoryRenderingManager fgMemoryRenderingManager;
	
	/**
	 * Returns the memory block manager.
	 * @return the memory block manager.
	 * @see IMemoryBlockManager
	 * @since 3.0
	 */
	public static IMemoryBlockManager getMemoryBlockManager(){
		if (fgMemoryBlockManager == null)
		{
			fgMemoryBlockManager = new MemoryBlockManager();
			
			if (fgMemoryRenderingManager == null)
			{
				// create rendering manager and make sure it's the first listener
				fgMemoryRenderingManager = new MemoryRenderingManager();
			}
		}
		
		return fgMemoryBlockManager;
	}
	
	/**
	 * Returns the memory rendering manager.
	 * @return the memory rendering manager.
	 * @see IMemoryRenderingManager
	 * @since 3.0
	 */
	public static IMemoryRenderingManager getMemoryRenderingManager() {
		if (fgMemoryRenderingManager == null)
		{
			fgMemoryRenderingManager = new MemoryRenderingManager();
		}
		
		return fgMemoryRenderingManager;
	}
	
	public static void pluginShutdown() {
		if (fgMemoryBlockManager != null) {
			fgMemoryBlockManager.shutdown();
		}
		if (fgMemoryRenderingManager != null) {
			fgMemoryRenderingManager.shutdown();
		}
		
	}
	
	/**
	 * Notifies a memory block listener in a safe runnable to
	 * handle exceptions.
	 */
	class MemoryBlockNotifier implements ISafeRunnable {
		
		private IMemoryBlockListener fListener;
		private int fType;
		private IMemoryBlock fMemoryBlock;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			DebugPlugin.log(exception);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.MemoryBlockAdded(fMemoryBlock);
					break;
				case REMOVED:
					fListener.MemoryBlockRemoved(fMemoryBlock);
					break;
			}			
		}

		/**
		 * Notify listeners of added/removed memory block events
		 */
		public void notify(IMemoryBlock memoryBlock, int update) {
			if (listeners != null) {
				fType = update;
				Object[] copiedListeners= listeners.toArray(new IMemoryBlockListener[listeners.size()]);
				for (int i= 0; i < copiedListeners.length; i++) {
					fListener = (IMemoryBlockListener)copiedListeners[i];
					fMemoryBlock = memoryBlock;
					Platform.run(this);
				}			
			}
			fListener = null;
			fMemoryBlock = null;
		}
	}
	
	private MemoryBlockNotifier getMemoryBlockNotifier() {
		return new MemoryBlockNotifier();
	}	


	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#addMemoryBlock(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void addMemoryBlock(IMemoryBlock mem, boolean addDefaultRenderings) {
		
		if (memoryBlocks == null)
			return;
		
		if (mem == null){
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.addMemoryBlock", null); //$NON-NLS-1$
			return;			
		}
		
		// do not allow duplicates
		if (memoryBlocks.contains(mem))
			return;
		
		MemoryRenderingManager renderingManager = (MemoryRenderingManager) getMemoryRenderingManager();
		
		if (!addDefaultRenderings)
		{
			renderingManager.setHandleMemoryBlockAddedEvent(false);
		}
		else
		{
			renderingManager.setHandleMemoryBlockAddedEvent(true);
		}
		
		memoryBlocks.add(mem);
		
		// add listener for the first memory block added
		if (memoryBlocks.size() == 1)
		{
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
		
		notifyListeners(mem, ADDED);

		// always set it back to true
		renderingManager.setHandleMemoryBlockAddedEvent(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#removeMemoryBlock(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void removeMemoryBlock(IMemoryBlock mem) {
		
		if (memoryBlocks == null)
			return;
		
		if (mem == null){
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.removeMemoryBlock", null); //$NON-NLS-1$
			return;			
		}		
		
		memoryBlocks.remove(mem);
		
		// remove listener after the last memory block has been removed
		if (memoryBlocks.size() == 0)
		{
			DebugPlugin.getDefault().removeDebugEventListener(this);
		}
		
		if (mem instanceof IMemoryBlockExtension)
		{ 
			((IMemoryBlockExtension)mem).dispose();
		}
		
		notifyListeners(mem, REMOVED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#addListener(org.eclipse.debug.ui.IMemoryBlockListener)
	 */
	public void addListener(IMemoryBlockListener listener) {
		
		if(listeners == null)
			return;
		
		if(listener == null){
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.addListener", null); //$NON-NLS-1$
			return;
		}
		
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#removeListener(org.eclipse.debug.ui.IMemoryBlockListener)
	 */
	public void removeListener(IMemoryBlockListener listener) {
		
		if(listeners == null)
			return;
		
		if(listener == null){
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.removeListener", null); //$NON-NLS-1$
			return;
		}
		
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#getAllMemoryBlocks()
	 */
	public IMemoryBlock[] getAllMemoryBlocks() {
		
		IMemoryBlock[] blocks = (IMemoryBlock[])memoryBlocks.toArray(new IMemoryBlock[memoryBlocks.size()]);
		
		return blocks;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#getMemoryBlocks(org.eclipse.debug.core.model.IDebugTarget)
	 */
	public IMemoryBlock[] getMemoryBlocks(IDebugTarget debugTarget) {
		
		IMemoryBlock[] blocks = (IMemoryBlock[])memoryBlocks.toArray(new IMemoryBlock[memoryBlocks.size()]);
		
		ArrayList memoryBlocksList = new ArrayList();
		
		for (int i=0; i<blocks.length; i++)
		{
			if (blocks[i].getDebugTarget() == debugTarget)
				memoryBlocksList.add(blocks[i]);
		}
		
		return (IMemoryBlock[])memoryBlocksList.toArray(new IMemoryBlock[memoryBlocksList.size()]);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#getMemoryBlocks(org.eclipse.debug.core.model.IMemoryBlockRetrieval)
	 */
	public IMemoryBlock[] getMemoryBlocks(IMemoryBlockRetrieval retrieve) {
		IMemoryBlock[] blocks = (IMemoryBlock[])memoryBlocks.toArray(new IMemoryBlock[memoryBlocks.size()]);
		
		ArrayList memoryBlocksList = new ArrayList(blocks.length);
		
		for (int i=0; i<blocks.length; i++)
		{
			if (blocks[i] instanceof IMemoryBlockExtension)
			{	
			
				if (((IMemoryBlockExtension)blocks[i]).getMemoryBlockRetrieval() == retrieve)
				{	
					memoryBlocksList.add(blocks[i]);
				}
			}
			else
			{	
				// standard memory block always uses the debug target as the memory block retrieval
				if (blocks[i].getDebugTarget() == retrieve)
					memoryBlocksList.add(blocks[i]);
			}
		}
		
		return (IMemoryBlock[])memoryBlocksList.toArray(new IMemoryBlock[memoryBlocksList.size()]);
	}
	
	
	private void notifyListeners(IMemoryBlock memoryBlock, int event)
	{
		getMemoryBlockNotifier().notify(memoryBlock, event);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		
		for (int i=0; i < events.length; i++)
			handleDebugEvent(events[i]);
		
	}
	
	public void handleDebugEvent(DebugEvent event) {
		Object obj = event.getSource();
		IDebugTarget dt = null;
		
		if (event.getKind() == DebugEvent.TERMINATE)
		{
			// a terminate event could happen from an IThread or IDebugTarget
			// only handle a debug event from the debug target
			if (obj instanceof IDebugTarget)
			{
				dt = ((IDebugTarget)obj);
			}
			
			// getMemoryBlocks will return an empty array if dt is null
			IMemoryBlock[] deletedMemoryBlocks = getMemoryBlocks(dt);
			
			for (int i=0; i<deletedMemoryBlocks.length; i++)
			{
				removeMemoryBlock(deletedMemoryBlocks[i]);
			}
		}
	}
	
	/**
	 * Clean up when the plugin is shut down
	 */
	public void shutdown()
	{
		if (listeners != null)
		{
			listeners.clear();
			listeners = null;
		}
		
		if (memoryBlocks != null)
		{
			memoryBlocks.clear();
			memoryBlocks = null;
		}
	}
}
