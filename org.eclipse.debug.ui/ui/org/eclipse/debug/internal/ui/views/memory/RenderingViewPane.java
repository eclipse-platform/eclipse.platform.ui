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
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

public class RenderingViewPane extends AbstractMemoryViewPane implements IMemoryRenderingListener{

	public static final String RENDERING_VIEW_PANE_ID = DebugUIPlugin.getUniqueIdentifier() + ".MemoryView.RenderingViewPane"; //$NON-NLS-1$
	
	private static final String VIEW_TAB_FACTORY = "viewTabFactory"; //$NON-NLS-1$
	private static final String RENDERER = "renderer"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$
	
	private Hashtable fTabFolderForMemoryBlock = new Hashtable();
	private Hashtable fMemoryBlockFromTabFolder = new Hashtable();

	private IAction fAddMemoryBlockAction;

	private IAction fRemoveMemoryRenderingAction;
	
	/**
	 * @param parent
	 */
	public RenderingViewPane(IViewPart parent) {
		super(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IMemoryBlockListener#MemoryBlockAdded(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void memoryBlocksAdded(final IMemoryBlock[] memoryBlocks) {
		for (int i=0; i<memoryBlocks.length; i++)
		{
			IMemoryBlock memory = memoryBlocks[i];
		
			// create a tab folder when there is a new memory block
			if (fTabFolderForMemoryBlock.containsKey(memory)) {
				if (fStackLayout.topControl != (TabFolder)fTabFolderForMemoryBlock.get(memory)) {
					setTabFolder((TabFolder)fTabFolderForMemoryBlock.get(memory));
					fViewPaneCanvas.layout();
				}
			} 
			else {	//otherwise, add a new one
				TabFolder folder =  new TabFolder(fViewPaneCanvas, SWT.NULL);
				TabItem newItem = new TabItem(folder, SWT.NULL);
				
				CreateRenderingTab createTab = new CreateRenderingTab(memory, newItem);
				
				folder.setSelection(0);
				
				fTabFolderForMemoryBlock.put(memory, folder);
				fMemoryBlockFromTabFolder.put(folder, memory);
				fTabFolderForDebugView.put(getMemoryBlockRetrieval(memory), folder);
				
				setTabFolder((TabFolder)fTabFolderForMemoryBlock.get(memory));
				setRenderingSelection(createTab);
				
				MenuManager menuMgr = createContextMenuManager();
				Menu menu = menuMgr.createContextMenu(folder);
				folder.setMenu(menu);
				
				fMenuMgr.put(folder, menuMgr);
				
				fViewPaneCanvas.layout();
			}	
			
			updateToolBarActionsEnablement();
		}
	}
	
	private IMemoryBlockRetrieval getMemoryBlockRetrieval(IMemoryBlock memoryBlock)
	{
		IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)memoryBlock.getAdapter(IMemoryBlockRetrieval.class);
		
		if (retrieval == null)
		{
			retrieval = memoryBlock.getDebugTarget();
		}
		
		return retrieval;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IMemoryBlockListener#MemoryBlockRemoved(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void memoryBlocksRemoved(final IMemoryBlock[] memoryBlocks) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				
				for (int j=0; j<memoryBlocks.length; j++)
				{
					IMemoryBlock memory = memoryBlocks[j];
					if (fTabFolderForMemoryBlock == null)
					{
						return;
					}
					
					// get all renderings from this memroy block and remove them from the view
					IMemoryRendering[] renderings = MemoryRenderingManager.getMemoryRenderingManager().getRenderingsFromMemoryBlock(memory);
					
					for (int k=0; k<renderings.length; k++)
					{
						MemoryBlockRenderingRemoved(renderings[k]);
					}
					
					// remove a the tab folder if the memory block is removed
					TabFolder tabFolder =
						(TabFolder) fTabFolderForMemoryBlock.get(memory);
					fTabFolderForMemoryBlock.remove(memory);
					fMemoryBlockFromTabFolder.remove(tabFolder);
					fTabFolderForDebugView.remove(getMemoryBlockRetrieval(memory));
					
					if (!tabFolder.isDisposed()) {
						
						IMemoryBlockRetrieval retrieve = (IMemoryBlockRetrieval)memory.getAdapter(IMemoryBlockRetrieval.class);
						if (retrieve == null)
						{	
							retrieve = memory.getDebugTarget();
						}
						
						if (fTabFolderForDebugView.contains(tabFolder))
						{					
							fTabFolderForDebugView.remove(retrieve);
						}
						
						// dispose all view tabs belonging to the tab folder
						TabItem[] items = tabFolder.getItems();
						
						for (int i=0; i<items.length; i++)
						{	
							if (items[i].getData() instanceof IMemoryViewTab)
								disposeViewTab((IMemoryViewTab)items[i].getData(), items[i]);
						}
						
						MenuManager menuMgr = (MenuManager)fMenuMgr.get(tabFolder);
						if (menuMgr != null)
							menuMgr.dispose();
						
						// dispose the tab folder
						tabFolder.dispose();						
						
						// if this is the top control
						if (tabFolder == fStackLayout.topControl)
						{	
							
							// if memory view is visible and have a selection
							// follow memory view's selection
							
							ISelection selection = DebugUIPlugin.getActiveWorkbenchWindow().getSelectionService().getSelection(IInternalDebugUIConstants.ID_MEMORY_VIEW);
							
							if (selection != null)
							{	
								if (!selection.isEmpty())
								{
									if (selection instanceof IStructuredSelection && ((IStructuredSelection)selection).getFirstElement() instanceof IMemoryBlock)
									{	
										IMemoryBlock blk = (IMemoryBlock)((IStructuredSelection)selection).getFirstElement();
										
										// memory view may not have got the event and is still displaying
										// the deleted memory block
										if (blk != memory)
											handleMemoryBlockSelection(null, blk);
										else if ((MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(memory.getDebugTarget()).length > 0))
										{
											blk = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(memory.getDebugTarget())[0];
											handleMemoryBlockSelection(null, blk);										
										}
										else
										{
											emptyFolder();
										}
									}
									else
									{
										emptyFolder();
									}
								}
								else if (MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(memory.getDebugTarget()).length > 0)
								{	// get to the next folder
									IMemoryBlock blk = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(memory.getDebugTarget())[0];
									handleMemoryBlockSelection(null, blk);
								}
								else
								{
									emptyFolder();
								}
							}
							// otheriwse, just pick the next one to display
							else if (MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(memory.getDebugTarget()).length > 0)
							{	// get to the next folder
								IMemoryBlock blk = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(memory.getDebugTarget())[0];
								handleMemoryBlockSelection(null, blk);
							}
							else
							{
								// empty the folder if there is no more stoarage block
								emptyFolder();
							}						
						}	
						
						// if not the top control
						// no need to do anything
					}
					
					updateToolBarActionsEnablement();
				}
			}
		});

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		try {
			
			if(part == this)
				return;
			
			if (!(selection instanceof IStructuredSelection))
				return;
			
			if (selection == null || selection.isEmpty())
			{
				// if the event comes from Memory View
				// pick empty tab folder as the memory view is no longer displaying anything
				if (part.getSite().getId().equals(IInternalDebugUIConstants.ID_MEMORY_VIEW))
				{
					IMemoryViewTab lastViewTab = getTopMemoryTab();
					
					if (lastViewTab != null)
						lastViewTab.setEnabled(false);
					
					emptyFolder();
				}
				
				// do not do anything if there is no selection
				// In the case when a debug adpater fires a debug event incorrectly, Launch View sets
				// selection to nothing.  If the view tab is disabled, it erases all the "delta" information
				// in the content.  This may not be desirable as it will cause memory to show up as
				// unchanged when it's actually changed.  Do not disable the view tab until there is a 
				// valid selection.
				
				return;
			}
			
			// back up current view tab
			IMemoryViewTab lastViewTab = getTopMemoryTab();
			
			if (!(selection instanceof IStructuredSelection))
				return;

			Object elem = ((IStructuredSelection)selection).getFirstElement();
			
			if (elem instanceof IMemoryBlock)
			{	
					// find the folder associated with the given IMemoryBlockRetrieval
				IMemoryBlock memBlock = (IMemoryBlock)elem;
				
				// should never get here... added code for safety
				if (fTabFolderForMemoryBlock == null)
				{
					if (lastViewTab != null)
						lastViewTab.setEnabled(false);
					
					emptyFolder();
					return;				
				}

				handleMemoryBlockSelection(lastViewTab, memBlock);				
			}
			else if (elem instanceof IDebugElement)
			{	
				handleDebugElementSelection(lastViewTab, (IDebugElement)elem);
			}
			else
			{
				if (part.getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW))
				{
					if (lastViewTab != null)
						lastViewTab.setEnabled(false);
					emptyFolder();
				}
				
				updateToolBarActionsEnablement();
				return;
				
			}
		}
		catch(SWTException se)
		{
			DebugUIPlugin.log(se);
		}

	}
	
	public void handleMemoryBlockSelection(final IMemoryViewTab lastViewTab, final IMemoryBlock memBlock) {
	
	Display.getDefault().syncExec(new Runnable()
	{
		public void run()
		{
						
		// don't do anything if the debug target is already terminated
		if (memBlock.getDebugTarget().isDisconnected() ||
			memBlock.getDebugTarget().isTerminated())
			{
				emptyFolder();
				return;
			}
		
		// check current memory block
		TabFolder currentFolder = (TabFolder)fStackLayout.topControl;
		if (currentFolder != null && !currentFolder.isDisposed())
		{
			IMemoryBlock currentBlk = (IMemoryBlock)fMemoryBlockFromTabFolder.get(currentFolder);
			if (currentBlk != null)
			{
				if (currentBlk == memBlock)
					return;
			}
		}
		
		if (getTopMemoryTab() != null)
		{	
			if (getTopMemoryTab().getMemoryBlock() == memBlock)
			{	
				return;
			}
		}
		
		//if we've got a tabfolder to go with the IMemoryBlockRetrieval, display it
		if (fTabFolderForMemoryBlock.containsKey(memBlock)) {
			if (fStackLayout.topControl != (TabFolder)fTabFolderForMemoryBlock.get(memBlock)) {
				setTabFolder((TabFolder)fTabFolderForMemoryBlock.get(memBlock));
				fViewPaneCanvas.layout();
			}
		} else {	//otherwise, add a new one
			TabFolder folder = new TabFolder(fViewPaneCanvas, SWT.NULL);
			fTabFolderForMemoryBlock.put(memBlock, folder);
			fMemoryBlockFromTabFolder.put(folder, memBlock);
			setTabFolder((TabFolder)fTabFolderForMemoryBlock.get(memBlock));
			fViewPaneCanvas.layout();
		}
		
		// restore view tabs
		IMemoryRendering[] renderings = MemoryRenderingManager.getMemoryRenderingManager().getRenderingsFromMemoryBlock(memBlock);
		TabFolder toDisplay = (TabFolder)fStackLayout.topControl;
		
		// remember tab folder for current debug target
		fTabFolderForDebugView.put(getMemoryBlockRetrieval(memBlock), toDisplay);
						
		if (toDisplay.getItemCount() == 0)
		{
			restoreViewTabs(renderings);
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
					newViewTab.setEnabled(true);
				}					
			}
		 }			
		 
		 IMemoryViewTab viewTab = getTopMemoryTab();
		 
		 setRenderingSelection(viewTab);
		 
		 if (viewTab == null)
		 {
			TabItem newItem = new TabItem(toDisplay, SWT.NULL);
			CreateRenderingTab createTab = new CreateRenderingTab(memBlock, newItem);
			setRenderingSelection(createTab);	
		 }
		
		//set toolbar actions enabled/disabled
		updateToolBarActionsEnablement();
		 
					}});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IMemoryRenderingListener#MemoryBlockRenderingAdded(org.eclipse.debug.internal.core.memory.IMemoryRendering)
	 */
	public void MemoryBlockRenderingAdded(IMemoryRendering rendering) {
		String renderingId = rendering.getRenderingId();
		IMemoryBlock  memoryblk = rendering.getBlock();
		
		IMemoryViewTabFactory viewTabFactory = getViewTabFactory(renderingId);
		
		if (viewTabFactory != null)
		{	
			// disable current view tab
			if (getTopMemoryTab() != null)
			{	
				getTopMemoryTab().setEnabled(false);
			}
			
			if (fTabFolderForMemoryBlock.containsKey(memoryblk)) {
				if (fStackLayout.topControl != (TabFolder)fTabFolderForMemoryBlock.get(memoryblk)) {
					setTabFolder((TabFolder)fTabFolderForMemoryBlock.get(memoryblk));
					fViewPaneCanvas.layout();
				}
			} else {	//otherwise, add a new one
				TabFolder folder =  new TabFolder(fViewPaneCanvas, SWT.NULL);
				fTabFolderForMemoryBlock.put(memoryblk, folder);
				fMemoryBlockFromTabFolder.put(folder, memoryblk);
				setTabFolder((TabFolder)fTabFolderForMemoryBlock.get(memoryblk));
				
				fViewPaneCanvas.layout();
			}	
			
			TabFolder tabFolder = (TabFolder) fStackLayout.topControl;
			
			if (tabFolder != null && tabFolder.getItemCount() >= 1)
			{
				// remove "Create rendering tab"
				TabItem item = tabFolder.getItem(0);
				if (item != null && item.getData() instanceof CreateRenderingTab)
				{
					((CreateRenderingTab)item.getData()).dispose();
					item.dispose();
				}
			}
			
			TabItem tab = new TabItem(tabFolder, SWT.NULL);
			
			// each view tab has its own menu manager
			// the menu manager gets disposed when the view tab is disposed
			MenuManager menuMgr = createContextMenuManager();
			
			AbstractMemoryRenderer renderer = getRenderer(renderingId);
			IMemoryViewTab viewTab = viewTabFactory.createViewTab(memoryblk, tab, menuMgr, rendering, renderer);
			
			if (viewTab != null)
			{	
				// bring new tab to the front
				tabFolder.setSelection(tabFolder.indexOf(tab));
				
				fMenuMgr.put(viewTab, menuMgr);
				
				setRenderingSelection(viewTab);
			}
			else
			{	
				// create an empty text view tab in case of error
				// error message is displayed in the view tab
				viewTab = new EmptyViewTab(memoryblk, tab, menuMgr, rendering);
				
				tabFolder.setSelection(tabFolder.indexOf(tab));
				
				fMenuMgr.put(viewTab, menuMgr);
				
				setRenderingSelection(viewTab);
			}
		}
		else
		{

			// log the error
			Status stat = new Status(
					IStatus.ERROR,DebugUIPlugin.getUniqueIdentifier(),
					DebugException.INTERNAL_ERROR, DebugUIMessages.getString("RenderingViewPane.NoViewTabFactory") + renderingId, null  //$NON-NLS-1$
			);
			
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				DebugUIPlugin.errorDialog(window.getShell(), DebugUIMessages.getString("RenderingViewPane.Failed_To_Create_Rendering"), DebugUIMessages.getString("RenderingViewPane.Failed_To_Create_Selected_Rendering"), stat); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			DebugUIPlugin.log(stat);
			
			// remove the rendering if view tab factory is not defined
			// otherwise, we will keep getting the error
			MemoryRenderingManager.getMemoryRenderingManager().removeMemoryBlockRendering(rendering.getBlock(), rendering.getRenderingId());
		}
		updateToolBarActionsEnablement();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IMemoryRenderingListener#MemoryBlockRenderingRemoved(org.eclipse.debug.internal.core.memory.IMemoryRendering)
	 */
	public void MemoryBlockRenderingRemoved(final IMemoryRendering rendering) {
		final IMemoryBlock memory = rendering.getBlock();
		
		// need to run the following code on the UI Thread to avoid invalid thread access exception
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				TabFolder tabFolder = (TabFolder) fStackLayout.topControl;
				
				if (tabFolder.isDisposed())
					return;
				
				TabItem[] tabs = tabFolder.getItems();
				boolean foundTab = false;
				for (int i = 0; i < tabs.length; i++)
				{
					IMemoryViewTab viewTab = (IMemoryViewTab) tabs[i].getData();
					
					if (tabs[i].isDisposed())
						continue;
					
					if (viewTab.getMemoryBlock() == memory)
					{
						if (viewTab.getRendering() == rendering)
						{
							foundTab = true;
							disposeViewTab(viewTab, tabs[i]);
							break;
						}
						
					}
				}

				// if a tab is not found in the current top control
				// this deletion is a result of a debug target termination
				// find memory from other folder and dispose the view tab
				if (!foundTab)
				{
					Enumeration enumeration = fTabFolderForMemoryBlock.elements();
					while (enumeration.hasMoreElements())
					{
						TabFolder otherTabFolder = (TabFolder) enumeration.nextElement();
						tabs = otherTabFolder.getItems();
						IMemoryViewTab viewTab = null;
						for (int i = 0; i < tabs.length; i++)
						{
							viewTab = (IMemoryViewTab) tabs[i].getData();
							if (viewTab.getMemoryBlock() == memory)
							{
								if (viewTab.getRendering() == rendering)
								{
									foundTab = true;
									disposeViewTab(viewTab, tabs[i]);
									break;
								}
							}
						}
					}
				}
				IMemoryViewTab top = getTopMemoryTab();
				
				// update selection
				if (top != null)
					setRenderingSelection(top);
				else
				{
					if (tabFolder != fEmptyTabFolder)
					{
						TabItem newItem = new TabItem(tabFolder, SWT.NULL);
						CreateRenderingTab createTab = new CreateRenderingTab(memory, newItem);
						tabFolder.setSelection(0);
						setRenderingSelection(createTab);
					}
				}
					
				updateToolBarActionsEnablement();
			}
		});
		
	}
	
	public IMemoryViewTabFactory getViewTabFactory(String renderingId)
	{	
		if (fRenderingInfoTable == null)
			fRenderingInfoTable = new Hashtable();
		
		IMemoryRenderingType info;
		
		info = (IMemoryRenderingType)fRenderingInfoTable.get(renderingId);
		
		// ask manager for new info
		if (info == null)
		{
			info = MemoryRenderingManager.getMemoryRenderingManager().getRenderingTypeById(renderingId);
			fRenderingInfoTable.put(renderingId, info);
		}
		
		if (info == null)
			return null;
		
		IConfigurationElement element = info.getPropertyConfigElement(VIEW_TAB_FACTORY);
		
		if (element == null)
			return null;
		
		try {
			if (element != null)
			{	
				// create the view tab factory
				Object obj = element.createExecutableExtension(VALUE);
				
				if (obj instanceof IMemoryViewTabFactory)
					return (IMemoryViewTabFactory)obj;
                return null;
			}
            return null;
		} catch (CoreException e1) {
			return null;
		}
	}

	public AbstractMemoryRenderer getRenderer(String renderingId){
		
		if (fRenderingInfoTable == null)
			fRenderingInfoTable = new Hashtable();
		
		IMemoryRenderingType info;
		info = (IMemoryRenderingType)fRenderingInfoTable.get(renderingId);
		
		// ask manager for new info
		if (info == null)
		{
			info = MemoryRenderingManager.getMemoryRenderingManager().getRenderingTypeById(renderingId);
			fRenderingInfoTable.put(renderingId, info);
		}
		
		if (info == null)
			return null;
		
		IConfigurationElement element = info.getPropertyConfigElement(RENDERER);
		
		if (element == null)
			return null;
		
		try {
			if (element != null)
			{	
				// create the view tab factory
				Object obj = element.createExecutableExtension(VALUE);
				
				if (obj instanceof AbstractMemoryRenderer)
					return (AbstractMemoryRenderer)obj;
                return null;
			}
            return null;
		} catch (CoreException e1) {
			return null;
		}
	}
	
	/**
	 * @param viewTab
	 */
	private void setRenderingSelection(IMemoryViewTab viewTab) {
		if(viewTab != null)
		 {
		 	IMemoryRendering rendering = viewTab.getRendering();
		 	
		 	if (rendering != null)
		 	{	
		 		fSelectionProvider.setSelection(new StructuredSelection(rendering));
		 	}
		 }
	}
	
	private void restoreViewTabs(IMemoryRendering[] renderings)
	{
		for (int i=0; i<renderings.length; i++)
		{
			MemoryBlockRenderingAdded(renderings[i]);
			
//			// disable after done
//			if (renderings[i].getBlock() instanceof IMemoryBlockExtension)
//			{
//				((IMemoryBlockExtension)renderings[i].getBlock()).disconnect(this);
//			}
		}

		// enable memory block
		IMemoryViewTab viewTab = getTopMemoryTab();
		if (viewTab != null)
		{
			if (viewTab.getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				((IMemoryBlockExtension)viewTab.getMemoryBlock()).connect(this);
			}
		}		
	}
	
	private void handleDebugElementSelection(final IMemoryViewTab lastViewTab, final IDebugElement element)
	{
		if (element.getDebugTarget() == null)
			return;
		
		// don't do anything if the debug target is already terminated
		if (element.getDebugTarget().isDisconnected() ||
			element.getDebugTarget().isTerminated())
		{
			emptyFolder();
			return;
		}
		
		// get current memory block retrieval and debug target
		IMemoryBlockRetrieval currentRetrieve = null;
		
		// get tab folder
		TabFolder tabFolder = (TabFolder) fStackLayout.topControl;
		
		// get memory block
		IMemoryBlock currentBlock = (IMemoryBlock)fMemoryBlockFromTabFolder.get(tabFolder);
		
		if (currentBlock != null)
		{	
			currentRetrieve = (IMemoryBlockRetrieval)currentBlock.getAdapter(IMemoryBlockRetrieval.class);
			
			if (currentRetrieve == null)
			{
				currentRetrieve = currentBlock.getDebugTarget();
			}
		}
		
		// find the folder associated with the given IMemoryBlockRetrieval
		IMemoryBlockRetrieval retrieve = (IMemoryBlockRetrieval)element.getAdapter(IMemoryBlockRetrieval.class);
		IDebugTarget debugTarget = element.getDebugTarget();
		
		// if IMemoryBlockRetrieval is null, use debugtarget
		if (retrieve == null)
			retrieve = debugTarget;

		if (debugTarget == null ||debugTarget.isTerminated() || debugTarget.isDisconnected()) {
			emptyFolder();
			return;
		}

		// if debug target has changed
		// switch to that tab folder
		if (retrieve != currentRetrieve)
		{	
			TabFolder folder = (TabFolder)fTabFolderForDebugView.get(retrieve);
			
			if (folder != null)
			{	
				setTabFolder(folder);
				fTabFolderForDebugView.put(retrieve, folder);
				fViewPaneCanvas.layout();
			}
			else
			{	
				// find out if there is any memory block for this debug target
				// and set up tab folder for the memory blocks
				IMemoryBlock blocks[] = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(retrieve);
				
				if (blocks.length > 0)
				{	
					handleMemoryBlockSelection(null, blocks[0]);
				}
				else
				{	
					emptyFolder();
					fViewPaneCanvas.layout();
				}
			}
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
					newViewTab.setEnabled(true);
				}					
			}
			
			setRenderingSelection(newViewTab);
		}			
		
		//set toolbar actions enabled/disabled
		updateToolBarActionsEnablement();
	}
	
	protected void addListeners() {
		super.addListeners();
		MemoryRenderingManager.getMemoryRenderingManager().addListener(this);
	}
	protected void removeListeners() {
		super.removeListeners();
		MemoryRenderingManager.getMemoryRenderingManager().removeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		
		if (getTopMemoryTab() == null)
			return;
		
		IMemoryRendering rendering = getTopMemoryTab().getRendering();
		
		if (rendering != null)
		{
			fSelectionProvider.setSelection(new StructuredSelection(rendering));
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	public Object getCurrentSelection() {
		if (getTopMemoryTab() != null)
			if (getTopMemoryTab().getRendering() != null)
				return getTopMemoryTab().getRendering();
		return new Object();
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryView#getAllViewTabs()
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
	 * @see com.ibm.debug.extended.ui.IMemoryView#moveToTop(com.ibm.debug.extended.ui.IMemoryViewTab)
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
				
				setRenderingSelection(tab);
				
				getTopMemoryTab().setEnabled(isEnabled);
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractMemoryViewPane#getPaneId()
	 */
	public String getPaneId() {
		return RENDERING_VIEW_PANE_ID;
	}
	public void restoreViewPane() {
		
		IMemoryBlock memoryBlock = null;
		// get current selection from memory view
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IInternalDebugUIConstants.ID_MEMORY_VIEW);
		if (MemoryViewUtil.isValidSelection(selection))
		{
			Object elem = ((IStructuredSelection)selection).getFirstElement();

			if (!(elem instanceof IMemoryBlock))
				return;
									
			memoryBlock = (IMemoryBlock)elem;																	
		}
		
		if (memoryBlock == null)
		{	
			// get selection from this view
			selection = fSelectionProvider.getSelection();
			
			if (MemoryViewUtil.isValidSelection(selection))
			{	
				Object elem = ((IStructuredSelection)selection).getFirstElement();

				if (!(elem instanceof IMemoryBlock))
					return;
				
				memoryBlock = (IMemoryBlock)elem;								
			}
		}
		
		if (memoryBlock != null)
		{	
			if (fTabFolderForMemoryBlock.containsKey(memoryBlock))
			{
				TabFolder toDisplay = (TabFolder)fTabFolderForMemoryBlock.get(memoryBlock);
				
				if (toDisplay != null)
				{
					setTabFolder(toDisplay);
					fViewPaneCanvas.layout();
					
					// restore view tabs
					IMemoryRendering[] renderings = MemoryRenderingManager.getMemoryRenderingManager().getRenderingsFromMemoryBlock(memoryBlock);
					
					if (toDisplay.getItemCount() == 0)
					{
						restoreViewTabs(renderings);
					}
				}
			}
			
			// disable current storag block
		
			IMemoryViewTab top = getTopMemoryTab();
		
			if (top != null)
			{
				if (!top.isEnabled())
				{
					top.setEnabled(true);
				}
			}
			else
			{
				TabFolder folder = (TabFolder)fStackLayout.topControl;
				TabItem newItem = new TabItem(folder, SWT.NULL);
				new CreateRenderingTab(memoryBlock, newItem);
				folder.setSelection(0);						
			}
		}
	}
	
	public void dispose() {
		super.dispose();
		
		fTabFolderForMemoryBlock.clear();
		fTabFolderForMemoryBlock = null;
		
		fMemoryBlockFromTabFolder.clear();
		fMemoryBlockFromTabFolder = null;
	}	
	
	public Control createViewPane(Composite parent) {
		Control control =  super.createViewPane(parent);
		WorkbenchHelp.setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".MemoryRenderingView_context"); //$NON-NLS-1$
		return control;
	}
	
	IAction[] getActions() {
		ArrayList actions = new ArrayList();
		
		if (fAddMemoryBlockAction == null)
			fAddMemoryBlockAction = new AddMemoryRenderingAction();
		actions.add(fAddMemoryBlockAction);
		
		if (fRemoveMemoryRenderingAction == null)
			fRemoveMemoryRenderingAction = new RemoveMemoryRenderingAction();
		
		fRemoveMemoryRenderingAction.setEnabled(false);
		actions.add(fRemoveMemoryRenderingAction);
		
		return (IAction[])actions.toArray(new IAction[actions.size()]);
	}
	
	// enable/disable toolbar action 
	protected void updateToolBarActionsEnablement()
	{
		IDebugTarget target = getSelectedDebugTarget();
		if (target != null)
		{	
			IMemoryBlock[] blocks = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(target);
			
			if (blocks.length > 0)
				fRemoveMemoryRenderingAction.setEnabled(true);
			else
				fRemoveMemoryRenderingAction.setEnabled(false);

		}
		else
		{	
			fRemoveMemoryRenderingAction.setEnabled(false);
		}
	}
	
	private IDebugTarget getSelectedDebugTarget()
	{
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		if (selection instanceof IStructuredSelection && !selection.isEmpty())
		{
			Object elem = ((IStructuredSelection)selection).getFirstElement();
			if (elem != null)
			{	
				if (elem instanceof IDebugElement)
				{	
					return ((IDebugElement)elem).getDebugTarget();
				}
			}		
			return null;
		}
        return null;
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
