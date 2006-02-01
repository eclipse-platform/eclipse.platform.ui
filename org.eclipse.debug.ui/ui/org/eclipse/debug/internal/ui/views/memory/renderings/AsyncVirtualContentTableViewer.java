/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTableViewer;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTableViewerContentManager;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;

public class AsyncVirtualContentTableViewer extends AsynchronousTableViewer {

	private Object fPendingTopIndexKey;
	private ArrayList fTopIndexQueue = new ArrayList();
	
	private boolean fPendingResizeColumns;
	private ListenerList fVirtualContentListeners;
	private SelectionListener fScrollSelectionListener;
	private ListenerList fPresentationErrorListeners;
	private Object fTopIndexKey;
	private IVirtualContentManager fVirtualContentManager;
	public static boolean DEBUG_DYNAMIC_LOADING;
	
	public AsyncVirtualContentTableViewer(Composite parent, int style) {
		super(parent, style);
		fVirtualContentListeners = new ListenerList();
		fPresentationErrorListeners = new ListenerList();
		initScrollBarListener();
	}
	
	private void initScrollBarListener()
	{
		ScrollBar scroll = getTable().getVerticalBar();
		fScrollSelectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleScrollBarSelection();
			}};
		scroll.addSelectionListener(fScrollSelectionListener);
	}
	
	
	public void setLabels(Widget widget, String[] labels, ImageDescriptor[] imageDescriptors) {
		super.setLabels(widget, labels, imageDescriptors);
		fPendingResizeColumns = attemptResizeColumnsToPreferredSize();
	}

	public void setTopIndex(Object key)
	{
		fPendingTopIndexKey = key;
		attemptSetTopIndex();
	}
	
	protected Object getPendingSetTopIndexKey()
	{
		return fPendingTopIndexKey;
	}
	
	protected void handlePresentationFailure(IAsynchronousRequestMonitor monitor, IStatus status) {
		notifyPresentationError(monitor, status);
	}
	
	public void disposeColumns()
	{
		// clean up old columns
		TableColumn[] oldColumns = getTable().getColumns();
		
		for (int i=0; i<oldColumns.length; i++)
		{
			oldColumns[i].dispose();
		}
	}
	
	public void disposeCellEditors()
	{
		// clean up old cell editors
		CellEditor[] oldCellEditors = getCellEditors();
		
		if (oldCellEditors != null)
		{
			for (int i=0; i<oldCellEditors.length; i++)
			{
				oldCellEditors[i].dispose();
			}
		}
	}
    
	/**
	 * Resize column to the preferred size.
	 */
	public void resizeColumnsToPreferredSize() {
		fPendingResizeColumns = true;
		fPendingResizeColumns = attemptResizeColumnsToPreferredSize();
	}
	
	private boolean attemptResizeColumnsToPreferredSize()
	{
		if (fPendingResizeColumns)
		{
			if(!hasPendingUpdates()) {
				Table table = getTable();
				TableColumn[] columns = table.getColumns();
				
				for (int i=0 ;i<columns.length-1; i++)
				{	
					columns[i].pack();
				}
				return false;
			}
		}
		return fPendingResizeColumns;
	}
	
	protected IVirtualContentManager getVirtualContentManager()
	{
		if (fVirtualContentManager == null)
		{
			if (getContentManager() instanceof IVirtualContentManager)
				fVirtualContentManager = (IVirtualContentManager)getContentManager();
		}
		return fVirtualContentManager;
	}
	
	/**
	 * Attempts to update any pending setTopIndex
	 * 
	 * @param reveal whether to reveal the selection
	 */
	protected synchronized void attemptSetTopIndex() {
		if (fPendingTopIndexKey != null) {
            Object remaining = doAttemptSetTopIndex(fPendingTopIndexKey);
            if (remaining == null)
            {
            	fPendingTopIndexKey = remaining;
            }
		}
	}
	
	private synchronized Object doAttemptSetTopIndex(final Object topIndexKey)
	{
		final int i = getVirtualContentManager().indexOf(topIndexKey);
		if (i >= 0)
		{
			UIJob job = new UIJob("set top index"){ //$NON-NLS-1$

				public IStatus runInUIThread(IProgressMonitor monitor)  {
						if (getTable().isDisposed())
							return Status.OK_STATUS;
						
						// remove the top index key from queue when it is processed
						removeKeyFromQueue(topIndexKey);
						int idx = getVirtualContentManager().indexOf(topIndexKey);
						if (idx > 0)
						{
							if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
								System.out.println("actual set top index: " + ((BigInteger)topIndexKey).toString(16)); //$NON-NLS-1$
							setTopIndexKey(topIndexKey);
							getTable().setTopIndex(idx);
						}
						else
						{
							if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
								System.out.println("cannot find key, put it back to the queue: " + topIndexKey); //$NON-NLS-1$
							fPendingTopIndexKey = topIndexKey;
						}
					return Status.OK_STATUS;
				}};
				
			// set top index does not happen immediately, keep track of
			// all pending set top index
			addKeyToQueue(topIndexKey);
			
			job.setSystem(true);
			job.schedule();
			return null;
		}
		return topIndexKey;
	}

	// TODO:  do we need to push this down to async table viewer?
	 protected void setChildren(final Widget widget, final List children)
	 {
		 preservingSelection(new Runnable() {

			public void run() {
				if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
					System.out.println(Thread.currentThread().getName() + " Set children"); //$NON-NLS-1$
				 AsyncVirtualContentTableViewer.super.setChildren(widget, children);
			}});
		
		 if (widget == getTable())
			 attemptSetTopIndex();
	 }
	 
	 public void addVirtualContentListener(IVirtualContentListener listener)
	 {
		 fVirtualContentListeners.add(listener);
	 }
	 
	 public void removeVirtualContentListener(IVirtualContentListener listener)
	 {
		 fVirtualContentListeners.remove(listener);
	 }
	 
	 protected void notifyListenersAtBufferStart()
	 {
		 int topIdx = getTable().getTopIndex();
		 Object[] listeners = fVirtualContentListeners.getListeners();
	 
		for (int i = 0; i < listeners.length; i++) {
			final IVirtualContentListener listener = (IVirtualContentListener) listeners[i];
			if (topIdx <= listener.getThreshold())
			{
				Platform.run(new ISafeRunnable() {
					public void run() throws Exception {	
						listener.handledAtBufferStart();
					}
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
				});
			}
		}
	 }
	 
	 protected void notifyListenersAtBufferEnd()
	 {
		Object[] listeners = fVirtualContentListeners.getListeners();
		int topIdx = getTable().getTopIndex();
		int bottomIdx = topIdx + getContentManager().getVisibleItemCount(topIdx);
		int elementsCnt = getContentManager().getElements().length;
		int numLinesLeft = elementsCnt - bottomIdx;
	 
		for (int i = 0; i < listeners.length; i++) {
			final IVirtualContentListener listener = (IVirtualContentListener) listeners[i];
			if (numLinesLeft <= listener.getThreshold())
			{
				Platform.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.handleAtBufferEnd();
					}
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
				});
			}
		}
	 }
	 
	protected void  handleScrollBarSelection()
	{
//		 ignore event if there is pending set top index in the queue
		if (!fTopIndexQueue.isEmpty())
			return;
		topIndexChanged();
	}
	
	public void topIndexChanged()
	{
		MemorySegment a = (MemorySegment)getTable().getItem(getTable().getTopIndex()).getData();
		setTopIndexKey(getVirtualContentManager().getKey(getTable().getTopIndex()));

		if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
			System.out.println(Thread.currentThread().getName() + " handle scroll bar moved:  top index: " + a.getAddress().toString(16)); //$NON-NLS-1$
		
		notifyListenersAtBufferStart();
		notifyListenersAtBufferEnd();
	}
	
	protected void setTopIndexKey(Object key)
	{
		fTopIndexKey = key;
	}
	
	protected Object getTopIndexKey()
	{
		return fTopIndexKey;
	}

	/* 
	 * TODO:  also push this to base table viewer?
	 */
	protected synchronized void preservingSelection(Runnable updateCode) {
		if (fPendingTopIndexKey == null) {
			Object oldTopIndexKey = null;
			try {
				// preserve selection
				oldTopIndexKey = getTopIndexKey();
				// perform the update
				updateCode.run();
			} finally {			
				if (oldTopIndexKey != null)
				{
					setTopIndex(oldTopIndexKey);
				}
			}
		} else {
			updateCode.run();
		}
	}
	
	// TODO:  base asyn table viewer can use this too?
	public void addPresentationErrorListener(IPresentationErrorListener errorListener)
	{
		fPresentationErrorListeners.add(errorListener);
	}
	
	public void removePresentationErrorListener(IPresentationErrorListener errorListener)
	{
		fPresentationErrorListeners.remove(errorListener);
	}
	
	private void notifyPresentationError(final IAsynchronousRequestMonitor monitor, final IStatus status)
	{
		Object[] listeners = fPresentationErrorListeners.getListeners();
	 
		for (int i = 0; i < listeners.length; i++) {
			
			if (listeners[i] instanceof IPresentationErrorListener)
			{
				final IPresentationErrorListener listener = (IPresentationErrorListener)listeners[i];
				Platform.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.handlePresentationFailure(monitor, status);
					}
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
				});
			}
		}
	}

	// TODO:  viewer needs to create a content manager that implements IVirtualContentManager
	protected AsynchronousTableViewerContentManager createContentManager() {
		return super.createContentManager();
	}
	
	private void addKeyToQueue(Object topIndexKey)
	{
		synchronized(fTopIndexQueue){
			if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
				System.out.println(" >>> add to queue: " + ((BigInteger)topIndexKey).toString(16)); //$NON-NLS-1$
			fTopIndexQueue.add(topIndexKey);
		}
	}
	
	private void removeKeyFromQueue(Object topIndexKey)
	{
		synchronized(fTopIndexQueue){
			if (AsyncVirtualContentTableViewer.DEBUG_DYNAMIC_LOADING)
				System.out.println(" >>> remove frome queue: " + ((BigInteger)topIndexKey).toString(16)); //$NON-NLS-1$
			fTopIndexQueue.remove(topIndexKey);
		}
	}

}
