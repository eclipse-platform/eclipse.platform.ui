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
import java.util.Enumeration;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

public class MemoryViewPane extends AbstractMemoryViewPane {
	
	public static final String MEMORY_VIEW_PANE_ID = DebugUIPlugin.getUniqueIdentifier() + ".MemoryView.MemoryViewPane"; //$NON-NLS-1$
	private AddMemoryBlockAction fAddMemoryBlockAction;
	private IAction fRemoveMemoryBlockAction;
	private IAction fResetMemoryBlockAction;
	private IAction fCopyViewToClipboardAction;
	private IAction fPrintViewTabAction;
	
	/**
	 * @param parent
	 */
	public MemoryViewPane(IViewPart parent) {
		super(parent);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	
		try {
			
			if (part == this)
				return;
			
			if (!(selection instanceof IStructuredSelection))
				return;
			
			// back up current view tab
			IMemoryViewTab lastViewTab = getTopMemoryTab();
						
			//only single selection of IDebugElement is allowed for this action
			if (selection == null || selection.isEmpty())
			{
				// do not do anything if there is no selection 
				// In the case when a debug adpater fires a debug event incorrectly, Launch View sets
				// selection to nothing.  If the view tab is disabled, it erases all the "delta" information
				// in the content.  This may not be desirable as it will cause memory to show up as
				// unchanged when it's actually changed.  Do not disable the view tab until there is a 
				// valid selection.
				return;
			}
	
			Object[] selections = ((IStructuredSelection)selection).toArray();
			
			for (int i=0; i<selections.length; i++)
			{
				Object selectedObj = selections[i];
			
				if (selectedObj instanceof IMemoryRendering){
					handleMemoryBlockSelection(lastViewTab, ((IMemoryRendering)selectedObj).getBlock());
				}
				else if (selectedObj instanceof IMemoryBlock)
				{
					handleMemoryBlockSelection(lastViewTab, (IMemoryBlock)selectedObj);
				}
				else if (selectedObj instanceof IDebugElement)
				{
					handleDebugElementSelection(lastViewTab, (IDebugElement)selectedObj);
				}
				else
				{
					if (part.getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW))
					{
						if (lastViewTab != null)
							lastViewTab.setEnabled(false);
						emptyFolder();
					}
					fKey = null;
					return;				
				}
			}
		}
		catch(SWTException se)
		{
			DebugUIPlugin.log(se);
		}
	}
	private void handleMemoryBlockSelection(final IMemoryViewTab lastViewTab, final IMemoryBlock memoryBlock)
	{
		// check top view tab, if already showing the memory block, do
		// nothing
		if (getTopMemoryTab() != null) {
			if (getTopMemoryTab().getMemoryBlock() == memoryBlock) {
				return;
			}
			// otherwise, find the memory block to display
			TabFolder folder = (TabFolder) fStackLayout.topControl;
			TabItem[] items = folder.getItems();

			for (int i = 0; i < items.length; i++) {
				IMemoryViewTab viewTab =
					(IMemoryViewTab) items[i].getData();
				if (viewTab.getMemoryBlock() == memoryBlock) {
					boolean isEnabled = lastViewTab.isEnabled();

					// switch to that memory block
					lastViewTab.setEnabled(false);
					folder.setSelection(i);
					fSelectionProvider.setSelection(
						new StructuredSelection(memoryBlock));
					getTopMemoryTab().setEnabled(isEnabled && fVisible);
					break;
				}
			}
		} else {
			// do nothing since there is nothing else to display
		}
	}
	
	private void handleDebugElementSelection(IMemoryViewTab lastViewTab, IDebugElement elem)
	{	
		// find the folder associated with the given IMemoryBlockRetrieval
		IMemoryBlockRetrieval retrieve = (IMemoryBlockRetrieval)elem.getAdapter(IMemoryBlockRetrieval.class);
		IDebugTarget debugTarget = elem.getDebugTarget();
		
		// if IMemoryBlockRetrieval is null, use debugtarget
		if (retrieve == null)
			retrieve = debugTarget;
		
		fKey = retrieve;	
		
		if (fKey == null)
			return;

		if (debugTarget == null ||debugTarget.isTerminated() || debugTarget.isDisconnected()) {
			emptyFolder();
			
			if (fTabFolderForDebugView != null)
			{
				if (fTabFolderForDebugView.containsKey(fKey))
				{
					TabFolder deleteFolder = (TabFolder)fTabFolderForDebugView.get(fKey);
					
					// dispose folder if not already disposed
					if (!deleteFolder.isDisposed())
						deleteFolder.dispose();
					
					fTabFolderForDebugView.remove(fKey);
				}
			}
			
			fKey = null;
			return;
		}
		
		// should never get here... added code for safety
		if (fTabFolderForDebugView == null)
		{
			emptyFolder();
			fKey = null;
			return;				
		}

		//if we've got a tabfolder to go with the IMemoryBlockRetrieval, display it
		if (fTabFolderForDebugView.containsKey(retrieve)) {
			if (fStackLayout.topControl != (TabFolder)fTabFolderForDebugView.get(retrieve)) {
				setTabFolder((TabFolder)fTabFolderForDebugView.get(retrieve));
				fViewPaneCanvas.layout();
			}
		} else {	//otherwise, add a new one
			fTabFolderForDebugView.put(retrieve, new TabFolder(fViewPaneCanvas, SWT.NULL));
			setTabFolder((TabFolder)fTabFolderForDebugView.get(retrieve));
			fViewPaneCanvas.layout();
		}
		
		// restore view tabs based on memory block retrieval
		IMemoryBlock[] memoryBlocks = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(retrieve);
		TabFolder toDisplay = (TabFolder)fStackLayout.topControl;
		
		if (toDisplay.getItemCount() == 0)
		{
			restoreViewTabs(memoryBlocks);
		}
		
		// disable last view tab as it becomes hidden
		IMemoryViewTab newViewTab = getTopMemoryTab();
		
		if (lastViewTab != null && lastViewTab != newViewTab)
		{
			lastViewTab.setEnabled(false);
		}
		
		if (newViewTab != null)
		{
			// if new view tab is not already enabled, enable it
			if (!newViewTab.isEnabled())
			{
				// if the view tab is visible, enable it
				if (fVisible)
				{
					newViewTab.setEnabled(fVisible);
					fSelectionProvider.setSelection(new StructuredSelection(newViewTab.getMemoryBlock()));
				}					
			}
		}			
		
		//set toolbar actions enabled/disabled
		updateToolBarActionsEnablement();		
	}
	
	private void restoreViewTabs(IMemoryBlock[] memoryBlocks)
	{
		memoryBlocksAdded(memoryBlocks);
	}
	public void memoryBlocksRemoved(final IMemoryBlock[] memoryBlocks) {
			
		// need to run the following code on the UI Thread to avoid invalid thread access exception
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				for (int j=0; j<memoryBlocks.length; j++)
				{
					IMemoryBlock memory = memoryBlocks[j];
					TabFolder tabFolder = (TabFolder) fStackLayout.topControl;
					
					if (tabFolder.isDisposed())
						return;
					
					TabItem[] tabs = tabFolder.getItems();
					boolean foundTab = false;
					for (int i = 0; i < tabs.length; i++)
					{
						IMemoryViewTab viewTab = (IMemoryViewTab) tabs[i].getData();
						if (viewTab.getMemoryBlock() == memory)
						{
							disposeViewTab(viewTab, tabs[i]);
							foundTab = true;
							break;
						}
					}
					if (foundTab)
					{
						if (tabFolder.getItemCount() == 0)
						{
							fRemoveMemoryBlockAction.setEnabled(false);
							fResetMemoryBlockAction.setEnabled(false);
							fCopyViewToClipboardAction.setEnabled(false);
							fPrintViewTabAction.setEnabled(false);
							
							// if there is no more item in the top tab folder
							// set selection as empty
							fSelectionProvider.setSelection(new StructuredSelection(new Object[0]));
							
							// if there is no item left in the folder and if the debug target
							// for the last memory block has been terminated
							// Clean up the tab folder and use the EmptyTabFolder for display
							IDebugTarget dt = memory.getDebugTarget();
							if (dt.isTerminated() || dt.isDisconnected())
							{
								if (fKey != null)
								{
									fTabFolderForDebugView.remove(fKey);
								}
								if (!tabFolder.isDisposed())
								{
									tabFolder.dispose();
								}
								emptyFolder();
							}
						}
					}
					// if a tab is not found in the current top control
					// this deletion is a result of a debug target termination
					// find memory from other folder and dispose the view tab
					if (!foundTab)
					{
						Enumeration enumeration = fTabFolderForDebugView.elements();
						while (enumeration.hasMoreElements())
						{
							tabFolder = (TabFolder) enumeration.nextElement();
							tabs = tabFolder.getItems();
							IMemoryViewTab viewTab = null;
							for (int i = 0; i < tabs.length; i++)
							{
								viewTab = (IMemoryViewTab) tabs[i].getData();
								if (viewTab.getMemoryBlock() == memory)
								{
									disposeViewTab(viewTab, tabs[i]);
									foundTab = true;
									break;
								}
							}
							if (foundTab)
							{
								if (tabFolder.getItemCount() == 0)
								{
									// if there is no item left in the folder and if the debug target
									// for the last memory block has been terminated
									// Clean up the tab folder and use the EmptyTabFolder for display
									IDebugTarget dt = memory.getDebugTarget();
									if (dt.isTerminated() || dt.isDisconnected())
									{
										Enumeration keyEnum = fTabFolderForDebugView.keys();
										Object tabKey = null;
										while (keyEnum.hasMoreElements())
										{
											tabKey = keyEnum.nextElement();
											if (fTabFolderForDebugView.get(tabKey) == tabFolder)
											{
												break;
											}
										}
										// dispose of the folder if it no longer contains anything
										if (!tabFolder.isDisposed())
											tabFolder.dispose();
										// remove the folder from the hashtable
										if (tabKey != null)
											fTabFolderForDebugView.remove(tabKey);
										// use empty folder for display
										emptyFolder();
									}
								}
								break;
							}
						}
					}
				}
			}
		});
	}	
	
	public void memoryBlocksAdded(IMemoryBlock[] memoryBlocks) {
		
		for (int i=0; i<memoryBlocks.length; i++)
		{
			IMemoryBlock memory = memoryBlocks[i];
		
			// disable current view tab
			if (getTopMemoryTab() != null)
				getTopMemoryTab().setEnabled(false);
			TabFolder tabFolder = (TabFolder) fStackLayout.topControl;
			TabItem tab = new TabItem(tabFolder, SWT.NULL);
			// create memory tab with new memory
			IMemoryViewTab memoryTab;
			
			// create a new menu manager for each view tab
			// menu manager will be cleaned up when the view tab is disposed
			// or when the view is disposed
			MenuManager menuMgr = createContextMenuManager();
			
			HexRendering hexRendering = new HexRendering(memory, IMemoryViewConstants.RENDERING_RAW_MEMORY);
			
			IMemoryViewTabFactory factory = getViewTabFactory(IMemoryViewConstants.RENDERING_RAW_MEMORY);
			AbstractMemoryRenderer renderer = getRenderer(IMemoryViewConstants.RENDERING_RAW_MEMORY);
			
			memoryTab = factory.createViewTab(memory, tab, menuMgr, hexRendering, renderer);
			
			// put to hashtable to be cleaned up later
			fMenuMgr.put(memoryTab, menuMgr);
			
			// bring new tab to the front
			tabFolder.setSelection(tabFolder.indexOf(tab));
	
			updateToolBarActionsEnablement();
	
			fSelectionProvider.setSelection(new StructuredSelection(memoryTab.getMemoryBlock()));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e)
	{
		Object selectedItem = e.item.getData();
		if (selectedItem instanceof IMemoryViewTab)
		{	
			IMemoryBlock blk = ((IMemoryViewTab)e.item.getData()).getMemoryBlock();
			
			if (blk != null)
			{
				fSelectionProvider.setSelection(new StructuredSelection(blk));
			}			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e)
	{

		
	}
	
	public Object getCurrentSelection() {
		
		if (getTopMemoryTab() != null)
			return getTopMemoryTab().getMemoryBlock();
        return new Object();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryView#getAllViewTabs()
	 */
	public IMemoryViewTab[] getAllViewTabs() {
		
		// otherwise, find the view tab to display
		TabFolder folder = (TabFolder) fStackLayout.topControl;
		TabItem[] items = folder.getItems();
		
		IMemoryViewTab[] viewTabs = new IMemoryViewTab[folder.getItemCount()];
		
		for(int i=0; i<items.length; i++){
			viewTabs[i] = (IMemoryViewTab)items[i].getData();
		}
		
		return viewTabs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryView#moveToTop(org.eclipse.debug.ui.IMemoryViewTab)
	 */
	public void moveToTop(IMemoryViewTab viewTab) {
		
		IMemoryViewTab lastViewTab = getTopMemoryTab();
		
		if (viewTab == lastViewTab)
			return;
		
		// otherwise, find the view tab to display
		TabFolder folder = (TabFolder) fStackLayout.topControl;
		TabItem[] items = folder.getItems();

		for (int i = 0; i < items.length; i++) {
			IMemoryViewTab tab =
				(IMemoryViewTab) items[i].getData();
			if (viewTab == tab) {

				boolean isEnabled = lastViewTab.isEnabled();

				// switch to that viewTab
				lastViewTab.setEnabled(false);
				folder.setSelection(i);
				fSelectionProvider.setSelection(
						new StructuredSelection(getTopMemoryTab().getMemoryBlock()));
				getTopMemoryTab().setEnabled(isEnabled && fVisible);
				break;
			}
		}
	}
	
	public void restoreViewPane() {
		
		// get current selection from debug view
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		if (MemoryViewUtil.isValidSelection(selection))
		{
			Object elem = ((IStructuredSelection)selection).getFirstElement();
	
			IDebugTarget debugTarget = ((IDebugElement)elem).getDebugTarget();
			IMemoryBlockRetrieval memRetrieval =(IMemoryBlockRetrieval) ((IDebugElement)elem).getAdapter(IMemoryBlockRetrieval.class);
	
			if (memRetrieval == null)
			{
				// if debug element returns null from getAdapter, assume its debug target is going to retrieve memory blocks
				memRetrieval = debugTarget;
			}	
			
			if (memRetrieval == null)
				return;
									
			if (fTabFolderForDebugView.containsKey(memRetrieval))
			{
				TabFolder toDisplay = (TabFolder)fTabFolderForDebugView.get(memRetrieval);
				
				if (toDisplay != null)
				{
					setTabFolder(toDisplay);
					fViewPaneCanvas.layout();
					
					// restore view tabs
					IMemoryBlock[] memoryBlocks = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(debugTarget);
					
					if (toDisplay.getItemCount() == 0)
					{
						restoreViewTabs(memoryBlocks);
					}
				}
			}
			
			// disable current storag block
		
			IMemoryViewTab top = getTopMemoryTab();
		
			if (top != null)
			{
				if (!top.isEnabled() && fVisible)
				{
					top.setEnabled(fVisible);
				}
			}
		}
	}

	public void dispose() 
	{
		fAddMemoryBlockAction.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractMemoryViewPane#getActions()
	 */
	public IAction[]  getActions() {
		ArrayList actions = new ArrayList();
		
		if (fAddMemoryBlockAction == null)
			fAddMemoryBlockAction = new AddMemoryBlockAction(this);
		actions.add(fAddMemoryBlockAction);

		if (fRemoveMemoryBlockAction == null)
			fRemoveMemoryBlockAction = new RemoveMemoryBlockAction();
		fRemoveMemoryBlockAction.setEnabled(false);
		actions.add(fRemoveMemoryBlockAction);
		
		if (fResetMemoryBlockAction == null)
			fResetMemoryBlockAction = new ResetMemoryBlockAction();
		fResetMemoryBlockAction.setEnabled(false);
		actions.add(fResetMemoryBlockAction);
		
		if (fCopyViewToClipboardAction == null)
			fCopyViewToClipboardAction = new CopyViewTabToClipboardAction();
		fCopyViewToClipboardAction.setEnabled(false);
		actions.add(fCopyViewToClipboardAction);
		
		if (fPrintViewTabAction == null)
			fPrintViewTabAction = new PrintViewTabAction();
		fPrintViewTabAction.setEnabled(false);
		actions.add(fPrintViewTabAction);
		
		return (IAction[])actions.toArray(new IAction[actions.size()]);
	}
	
	// enable/disable toolbar action 
	protected void updateToolBarActionsEnablement()
	{
		TabFolder folder = (TabFolder)fStackLayout.topControl;
		int index = folder.getSelectionIndex();
		if (index >= 0) {
			fRemoveMemoryBlockAction.setEnabled(true);
			fResetMemoryBlockAction.setEnabled(true);
			fCopyViewToClipboardAction.setEnabled(true);
			fPrintViewTabAction.setEnabled(true);
			
		} else {
			fRemoveMemoryBlockAction.setEnabled(false);
			fResetMemoryBlockAction.setEnabled(false);
			fCopyViewToClipboardAction.setEnabled(false);
			fPrintViewTabAction.setEnabled(false);
		}		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractMemoryViewPane#emptyFolder()
	 */
	protected void emptyFolder() {
		super.emptyFolder();
		updateToolBarActionsEnablement();
		fSelectionProvider.setSelection(new StructuredSelection(new Object[0]));
	}
}
