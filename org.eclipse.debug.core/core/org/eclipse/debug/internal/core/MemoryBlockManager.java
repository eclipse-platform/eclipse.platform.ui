/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *	   WindRiver - Bug 192028 [Memory View] Memory view does not 
 *                 display memory blocks that do not reference IDebugTarget
 *******************************************************************************/

package org.eclipse.debug.internal.core;

import java.util.ArrayList;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IMemoryBlockListener;
import org.eclipse.debug.core.IMemoryBlockManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;


/**
 * Implementation of IMemoryBlockManager
 * The manager is responsible to manage all memory blocks in the workbench.
 * 
 * @since 3.1
 * 
 */
public class MemoryBlockManager implements IMemoryBlockManager, IDebugEventSetListener {
	
	private ArrayList listeners = new ArrayList();			// list of all IMemoryBlockListener
	private ArrayList memoryBlocks = new ArrayList();	// list of all memory blocks 
	
	private static final int ADDED = 0;
	private static final int REMOVED = 1;
	/**
	 * Notifies a memory block listener in a safe runnable to
	 * handle exceptions.
	 */
	class MemoryBlockNotifier implements ISafeRunnable {
		
		private IMemoryBlockListener fListener;
		private int fType;
		private IMemoryBlock[] fMemoryBlocks;
		
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
					fListener.memoryBlocksAdded(fMemoryBlocks);
					break;
				case REMOVED:
					fListener.memoryBlocksRemoved(fMemoryBlocks);
					break;
			}			
		}

		/**
		 * Notify listeners of added/removed memory block events
		 * 
		 * @param memBlocks blocks that have changed
		 * @param update type of change
		 */
		public void notify(IMemoryBlock[] memBlocks, int update) {
			if (listeners != null) {
				fType = update;
				Object[] copiedListeners= listeners.toArray(new IMemoryBlockListener[listeners.size()]);
				for (int i= 0; i < copiedListeners.length; i++) {
					fListener = (IMemoryBlockListener)copiedListeners[i];
					fMemoryBlocks = memBlocks;
                    SafeRunner.run(this);
				}			
			}
			fListener = null;
			fMemoryBlocks = null;
		}
	}
	
	/**
	 * Returns the <code>MemoryBlockNotifier</code>
	 * @return the <code>MemoryBlockNotifier</code>
	 * 
	 * TODO consider using only one of these, and sync where needed, 
	 * this way we are not creating a new every single time.
	 */
	private MemoryBlockNotifier getMemoryBlockNotifier() {
		return new MemoryBlockNotifier();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#addMemoryBlock(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void addMemoryBlocks(IMemoryBlock[] mem) {
		if (memoryBlocks == null) {
			return;
		}
		if (mem == null) {
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.addMemoryBlock", null); //$NON-NLS-1$
			return;			
		}
		
		if(mem.length > 0) {
			ArrayList newMemoryBlocks = new ArrayList();
			for (int i=0; i<mem.length; i++) {
				// do not allow duplicates
				if (!memoryBlocks.contains(mem[i])) {
					newMemoryBlocks.add(mem[i]);
					memoryBlocks.add(mem[i]);
					// add listener for the first memory block added
					if (memoryBlocks.size() == 1) {
						DebugPlugin.getDefault().addDebugEventListener(this);
					}
				}
			}
			notifyListeners((IMemoryBlock[])newMemoryBlocks.toArray(new IMemoryBlock[newMemoryBlocks.size()]), ADDED);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#removeMemoryBlock(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void removeMemoryBlocks(IMemoryBlock[] memBlocks) {
		if (memoryBlocks == null) {
			return;
		}
		if (memBlocks == null){
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.removeMemoryBlock", null); //$NON-NLS-1$
			return;			
		}		
		
		if(memBlocks.length > 0) {
			for (int i=0; i<memBlocks.length; i++) {
				memoryBlocks.remove(memBlocks[i]);
				// remove listener after the last memory block has been removed
				if (memoryBlocks.size() == 0) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
				}
				if (memBlocks[i] instanceof IMemoryBlockExtension) { 
					try {
						((IMemoryBlockExtension)memBlocks[i]).dispose();
					} catch (DebugException e) {
						DebugPlugin.log(e);
					}
				}
			}
			notifyListeners(memBlocks, REMOVED);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#addListener(org.eclipse.debug.ui.IMemoryBlockListener)
	 */
	public void addListener(IMemoryBlockListener listener) {
		
		if(listeners == null) {
			return;
		}
		if(listener == null) {
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.addListener", null); //$NON-NLS-1$
			return;
		}
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#removeListener(org.eclipse.debug.ui.IMemoryBlockListener)
	 */
	public void removeListener(IMemoryBlockListener listener) {
		
		if(listeners == null) {
			return;
		}
		if(listener == null) {
			DebugPlugin.logMessage("Null argument passed into IMemoryBlockManager.removeListener", null); //$NON-NLS-1$
			return;
		}
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IMemoryBlockManager#getMemoryBlocks()
	 */
	public IMemoryBlock[] getMemoryBlocks() {
		return (IMemoryBlock[])memoryBlocks.toArray(new IMemoryBlock[memoryBlocks.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#getMemoryBlocks(org.eclipse.debug.core.model.IDebugTarget)
	 */
	public IMemoryBlock[] getMemoryBlocks(IDebugTarget debugTarget) {
		IMemoryBlock[] blocks = (IMemoryBlock[])memoryBlocks.toArray(new IMemoryBlock[memoryBlocks.size()]);
		ArrayList memoryBlocksList = new ArrayList();
		for (int i=0; i<blocks.length; i++) {
			if (blocks[i].getDebugTarget() == debugTarget) {
				memoryBlocksList.add(blocks[i]);
			}
		}
		return (IMemoryBlock[])memoryBlocksList.toArray(new IMemoryBlock[memoryBlocksList.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockManager#getMemoryBlocks(org.eclipse.debug.core.model.IMemoryBlockRetrieval)
	 */
	public IMemoryBlock[] getMemoryBlocks(IMemoryBlockRetrieval retrieve) {
		IMemoryBlock[] blocks = (IMemoryBlock[])memoryBlocks.toArray(new IMemoryBlock[memoryBlocks.size()]);
		ArrayList memoryBlocksList = new ArrayList(blocks.length);
		for (int i=0; i<blocks.length; i++)	{
			if (blocks[i] instanceof IMemoryBlockExtension)	{	
				if (((IMemoryBlockExtension)blocks[i]).getMemoryBlockRetrieval() == retrieve) {	
					memoryBlocksList.add(blocks[i]);
				}
			}
			else {	
				IMemoryBlockRetrieval mbRetrieval = (IMemoryBlockRetrieval)blocks[i].getAdapter(IMemoryBlockRetrieval.class);
				
				// standard memory block always uses the debug target as the memory block retrieval
				if (mbRetrieval == null)
					mbRetrieval = blocks[i].getDebugTarget();
				
				if (mbRetrieval == retrieve) {
					memoryBlocksList.add(blocks[i]);
				}
			}
		}
		return (IMemoryBlock[])memoryBlocksList.toArray(new IMemoryBlock[memoryBlocksList.size()]);
	}
	
	/**
	 * Notifies the listeners about the given memory blocks and the event to be sent
	 * @param memBlocks the array of memory blocks
	 * @param event the event to notify to the blocks
	 */
	private void notifyListeners(IMemoryBlock[] memBlocks, int event) {
		getMemoryBlockNotifier().notify(memBlocks, event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i=0; i < events.length; i++) {
			handleDebugEvent(events[i]);
		}
	}
	
	/**
	 * Handles a debug event
	 * @param event the {@link DebugEvent}
	 */
	public void handleDebugEvent(DebugEvent event) {
		Object obj = event.getSource();
		IDebugTarget dt = null;
		
		if (event.getKind() == DebugEvent.TERMINATE) {
			// a terminate event could happen from an IThread or IDebugTarget
			// only handle a debug event from the debug target
			if (obj instanceof IDebugTarget) {
				dt = ((IDebugTarget)obj);
				
				// getMemoryBlocks will return an empty array if it is null
				IMemoryBlock[] deletedMemoryBlocks = getMemoryBlocks(dt);
				removeMemoryBlocks(deletedMemoryBlocks);
			}
		}
	}
	
	/**
	 * Clean up when the plugin is shut down
	 */
	public void shutdown() {
		if (listeners != null) {
			listeners.clear();
			listeners = null;
		}
		
		if (memoryBlocks != null) {
			memoryBlocks.clear();
			memoryBlocks = null;
		}
	}
	
}
