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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.core.memory.IMemoryBlockListener;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.debug.internal.core.memory.MemoryBlockManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * 
 * @since 3.0
 */
public class MemoryView extends PageBookView implements IDebugView, IMemoryBlockListener, ISelectionListener, IMemoryView {

	private Composite parent;
	protected StackLayout stackLayout;
	private TabFolder emptyTabFolder;
	protected Hashtable tabFolderHashtable;
	
	private Action addMemoryBlockAction;
	private Action removeMemoryBlockAction;
	private Action resetMemoryBlockAction;
	private Action copyViewToClipboardAction;
	private Action printViewTabAction;
	
	protected IMemoryBlockRetrieval key;  // store the key for current tab folder
	
	private MemoryViewPartListener fListener = null;
	private boolean fVisible;
	protected MemoryViewSelectionProvider fSelectionProvider;
	protected ViewTabEnablementManager fViewTabEnablementManager;

	protected Hashtable fMenuMgr;

	/**
	 * A page in this view's page book that contains this view's viewer.
	 */
	class ViewerPage extends Page {
		/** @see IPage#createControl(Composite) */
		public void createControl(Composite parent) { }

		/** @see IPage#getControl() */
		public Control getControl() { return null; }

		/** @see IPage#setFocus() */
		public void setFocus() {
			Viewer viewer= getViewer();
			if (viewer != null) {
				Control c = viewer.getControl();
				if (!c.isFocusControl()) {
					c.setFocus();
				}
			}
		}
	}
	
	class MemoryViewPartListener implements IPartListener2
	{
		MemoryView fView = null;
		
		public MemoryViewPartListener(MemoryView view)
		{
			fView = view;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partActivated(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partClosed(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partHidden(IWorkbenchPartReference ref) {
			
			IWorkbenchPart part = ref.getPart(false);
			
			if (part == fView)
			{
				fVisible = false;
				// disable current storag block
				
				IMemoryViewTab top = getTopMemoryTab();
				
				if (top != null)
				{
					top.setEnabled(false);
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partVisible(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			
			if (part == fView)
			{
				fVisible = true;

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
											
					if (tabFolderHashtable.containsKey(memRetrieval))
					{
						TabFolder toDisplay = (TabFolder)tabFolderHashtable.get(memRetrieval);
						
						if (toDisplay != null)
						{
							setTabFolder(toDisplay);
							parent.layout();
							
							// restore view tabs
							IMemoryBlock[] memoryBlocks = MemoryBlockManager.getMemoryBlockManager().getMemoryBlocks(debugTarget);
							
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
						if (!top.isEnabled())
						{
							top.setEnabled(true);
						}
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partInputChanged(IWorkbenchPartReference ref) {
		}

		public void partOpened(IWorkbenchPartReference ref)
		{	
		}
	}
	
	// TODO:  Handle selection change in table if possible
	class MemoryViewSelectionProvider implements ISelectionProvider, SelectionListener
	{
		ArrayList fListeners = new ArrayList();
		ISelection selectedMemoryBlock;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener)
		{
			if (!fListeners.contains(listener))
				fListeners.add(listener);
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		public ISelection getSelection()
		{
			return selectedMemoryBlock;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener)
		{
			if (fListeners.contains(listener))
				fListeners.remove(listener);
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
		 */
		public void setSelection(ISelection selection)
		{
			selectedMemoryBlock = selection;
			
			fireChanged();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e)
		{
			if (getTopMemoryTab() == null)
				return;
			
			IMemoryBlock blk = getTopMemoryTab().getMemoryBlock();
			
			if (blk != null)
			{
				setSelection(new StructuredSelection(blk));
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e)
		{
 
			
		}
		
		public void fireChanged()
		{
			SelectionChangedEvent evt = new SelectionChangedEvent(this, getSelection());
			for (int i=0; i<fListeners.size(); i++)
			{
				((ISelectionChangedListener)fListeners.get(i)).selectionChanged(evt);
			}
		}
	}	
	
	/**
	 * The constructor.
	 */
	public MemoryView() {
		
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		super.createPartControl(parent);
		this.parent = parent;
		stackLayout = new StackLayout();
		parent.setLayout(stackLayout);
		
		WorkbenchHelp.setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".MemoryView_context"); //$NON-NLS-1$
		
		fSelectionProvider = new MemoryViewSelectionProvider();
		fViewTabEnablementManager = new ViewTabEnablementManager();
		
		// contribute actions
		makeActions();
		contributeToActionBars();
		
		getViewSite().getActionBars().updateActionBars();
		
		emptyTabFolder = new TabFolder(parent, SWT.NULL);
		setTabFolder(emptyTabFolder);

		tabFolderHashtable = new Hashtable(3);
		fMenuMgr = new Hashtable();
		
		addListeners();
		
		// check current selection and create folder if something is already selected from debug view
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);

		if (MemoryViewUtil.isValidSelection(selection))
		{
			createFolder(selection);
		}
		
		fVisible = true;
		
		getSite().setSelectionProvider(this.fSelectionProvider);
	}
	
	protected void addListeners()
	{
		fListener = new MemoryViewPartListener(this);
		getSite().getPage().addPartListener(fListener);
		
		MemoryBlockManager.getMemoryBlockManager().addListener(this);
		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
//		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(IMemoryViewConstants.MEMORY_RENDERING_VIEW_ID, this);
		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	private MenuManager createContextMenuManager() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				
				IMemoryViewTab top = getTopMemoryTab();
				
				if (top != null)
				{
					top.fillContextMenu(manager);
				}
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});

		getSite().registerContextMenu(menuMgr, fSelectionProvider);
		return menuMgr;
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		
		bars.getMenuManager().add(new SetDefaultColumnSizePrefAction());
		bars.updateActionBars();
	}


	protected void fillContextMenu(IMenuManager manager) {
		manager.add(addMemoryBlockAction);
		manager.add(removeMemoryBlockAction);
		manager.add(resetMemoryBlockAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator("Additions")); //$NON-NLS-1$
	}
	
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(addMemoryBlockAction);
		manager.add(removeMemoryBlockAction);
		
		manager.add(new Separator());
		
		manager.add(resetMemoryBlockAction);
		manager.add(copyViewToClipboardAction);
		manager.add(printViewTabAction);
	}

	private void makeActions() {
		addMemoryBlockAction = new AddMemoryBlockAction();

		removeMemoryBlockAction = new RemoveMemoryBlockAction();
		removeMemoryBlockAction.setEnabled(false);
		
		resetMemoryBlockAction = new ResetMemoryBlockAction();
		resetMemoryBlockAction.setEnabled(false);
		
		copyViewToClipboardAction = new CopyViewTabToClipboardAction();
		copyViewToClipboardAction.setEnabled(false);
		
		printViewTabAction = new PrintViewTabAction();
		printViewTabAction.setEnabled(false);
		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		stackLayout.topControl.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockListener#MemoryBlockAdded(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void MemoryBlockAdded(IMemoryBlock memoryblk) {

		final IMemoryBlock memory = memoryblk;
		
		// disable current view tab
		if (getTopMemoryTab() != null)
			getTopMemoryTab().setEnabled(false);
		TabFolder tabFolder = (TabFolder) stackLayout.topControl;
		TabItem tab = new TabItem(tabFolder, SWT.NULL);
		// create memory tab with new memory
		MemoryViewTab memoryTab;
		
		// create a new menu manager for each view tab
		// menu manager will be cleaned up when the view tab is disposed
		// or when the view is disposed
		MenuManager menuMgr = createContextMenuManager();
		
		HexRendering hexRendering = new HexRendering(memory, IMemoryViewConstants.RENDERING_RAW_MEMORY);
		
		memoryTab = new MemoryViewTab(memory, tab, menuMgr, hexRendering, new HexRenderer());
		
		// put to hashtable to be cleaned up later
		fMenuMgr.put(memoryTab, menuMgr);
		
		// bring new tab to the front
		tabFolder.setSelection(tabFolder.indexOf(memoryTab.getTab()));
		// bringing the tab to the front causes the view tab's cursor to lose focus
		// this is a work-around to force focus to the cursor when the tab is created
//		memoryTab.setCursorFocus();

		updateToolBarActionsEnablement();

		fSelectionProvider.setSelection(new StructuredSelection(memoryTab.getMemoryBlock()));
		
//		ask debug target about memeory retrieval
		 IDebugTarget debugTarget = memory.getDebugTarget();
		 IMemoryBlockRetrieval standardMemRetrieval = (IMemoryBlockRetrieval)memory.getAdapter(IMemoryBlockRetrieval.class);
	
		 if (standardMemRetrieval == null)
		 {
			 // if getAdapter returns null, assume we only have one language under the debug target
			 // make use of the debug target to get IMemoryBlock
			 standardMemRetrieval = debugTarget;
		 }
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockListener#MemoryBlockRemoved(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void MemoryBlockRemoved(IMemoryBlock memoryblk)
	{
		final IMemoryBlock memory = memoryblk;
		
		// need to run the following code on the UI Thread to avoid invalid thread access exception
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				TabFolder tabFolder = (TabFolder) stackLayout.topControl;
				
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
						removeMemoryBlockAction.setEnabled(false);
						resetMemoryBlockAction.setEnabled(false);
						copyViewToClipboardAction.setEnabled(false);
						printViewTabAction.setEnabled(false);
						
						// if there is no more item in the top tab folder
						// set selection as empty
						fSelectionProvider.setSelection(new StructuredSelection(new Object[0]));
						
						// if there is no item left in the folder and if the debug target
						// for the last memory block has been terminated
						// Clean up the tab folder and use the EmptyTabFolder for display
						IDebugTarget dt = memory.getDebugTarget();
						if (dt.isTerminated() || dt.isDisconnected())
						{
							if (key != null)
							{
								tabFolderHashtable.remove(key);
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
					Enumeration enumeration = tabFolderHashtable.elements();
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
									Enumeration keyEnum = tabFolderHashtable.keys();
									Object tabKey = null;
									while (keyEnum.hasMoreElements())
									{
										tabKey = keyEnum.nextElement();
										if (tabFolderHashtable.get(tabKey) == tabFolder)
										{
											break;
										}
									}
									// dispose of the folder if it no longer contains anything
									if (!tabFolder.isDisposed())
										tabFolder.dispose();
									// remove the folder from the hashtable
									if (tabKey != null)
										tabFolderHashtable.remove(tabKey);
									// use empty folder for display
									emptyFolder();
								}
							}
							break;
						}
					}
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		
		removeListeners();
		
		// dispose empty folders
		emptyTabFolder.dispose();
		
		// dispose all other folders
		try {
			
			if (tabFolderHashtable != null) {
				Enumeration enumeration = tabFolderHashtable.elements();
				
				while (enumeration.hasMoreElements())
				{
					TabFolder tabFolder = (TabFolder)enumeration.nextElement();
					
					if (tabFolder.isDisposed())
						continue;
					
					// if tab folder is not empty, dipose view tabs
					TabItem[] tabs = tabFolder.getItems();
					
					for (int i=0; i<tabs.length; i++)
					{
						IMemoryViewTab viewTab = (IMemoryViewTab)tabs[i].getData();
						
						if (!tabs[i].isDisposed())
							disposeViewTab(viewTab, tabs[i]);
					}
					
					tabFolder.dispose();
				}
				
				// set to null so that clean up is only done once
				tabFolderHashtable = null;
			}
		} catch (Exception e) {		
			
			Status status = new Status(IStatus.ERROR, 
				DebugUIPlugin.getUniqueIdentifier(),
				0, "Exception occurred when the Memory  View is disposed", e); //$NON-NLS-1$
			DebugUIPlugin.log(status);
		}
		
		super.dispose();
	}

	/**
	 * 
	 */
	private void removeListeners() {
		// remove listeners
		MemoryBlockManager.getMemoryBlockManager().removeListener(this);
		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW,this);
//		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(IMemoryViewConstants.MEMORY_RENDERING_VIEW_ID, this);
		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		
		getSite().getPage().removePartListener(fListener);
		fListener = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
	 */
	protected IPage createDefaultPage(PageBook book) {
		MemoryView.ViewerPage page = new MemoryView.ViewerPage();
		page.createControl(book);
		initPage(page);
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	protected PageRec doCreatePage(IWorkbenchPart part) {
		 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.part.PageBookView.PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		 
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
	 */
	protected IWorkbenchPart getBootstrapPart() {
		 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean isImportant(IWorkbenchPart part) {
		 
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getViewer()
	 */
	public Viewer getViewer() {
		 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getPresentation(java.lang.String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#setAction(java.lang.String, org.eclipse.jface.action.IAction)
	 */
	public void setAction(String actionID, IAction action) {
		 
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#add(org.eclipse.ui.texteditor.IUpdate)
	 */
	public void add(IUpdate updatable) {
		 
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#remove(org.eclipse.ui.texteditor.IUpdate)
	 */
	public void remove(IUpdate updatable) {
		 
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getAction(java.lang.String)
	 */
	public IAction getAction(String actionID) {
		 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getContextMenuManager()
	 */
	public IMenuManager getContextMenuManager() {
		return null;
	}
	
	public IMemoryViewTab getTopMemoryTab() {
		
		if (stackLayout.topControl instanceof TabFolder)
		{
			TabFolder folder = (TabFolder)stackLayout.topControl;
			int index = folder.getSelectionIndex();
			if (index >= 0) {
				TabItem tab = folder.getItem(index);
				return (IMemoryViewTab)tab.getData();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	
		try {
			
			if (part == this)
				return;
			
			if (!(selection instanceof IStructuredSelection))
				return;
			
			// back up current view tab
			IMemoryViewTab lastViewTab = getTopMemoryTab();
						
			//only single selection of IDebugElement is allowed for this action
			if (selection == null || selection.isEmpty() || ((IStructuredSelection)selection).size() > 1)
			{
				// do not do anything if there is no selection or if there is more than one selection
				// In the case when a debug adpater fires a debug event incorrectly, Launch View sets
				// selection to nothing.  If the view tab is disabled, it erases all the "delta" information
				// in the content.  This may not be desirable as it will cause memory to show up as
				// unchanged when it's actually changed.  Do not disable the view tab until there is a 
				// valid selection.
				return;
			}

			Object elem = ((IStructuredSelection)selection).getFirstElement();
			
			if (elem instanceof IMemoryRendering){
				handleMemoryBlockSelection(lastViewTab, ((IMemoryRendering)elem).getBlock());
			}
			else if (elem instanceof IMemoryBlock)
			{
				handleMemoryBlockSelection(lastViewTab, (IMemoryBlock)elem);
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
				key = null;
				return;				
			}
		}
		catch(SWTException se)
		{
			Status status = new Status(IStatus.ERROR, 
					DebugUIPlugin.getUniqueIdentifier(),
					0, "SWT Exception occurred in Memory View selection changed", se); //$NON-NLS-1$
			DebugUIPlugin.log(status);
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
			TabFolder folder = (TabFolder) stackLayout.topControl;
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
					getTopMemoryTab().setEnabled(isEnabled);
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
		
		key = retrieve;	

		if (debugTarget == null ||debugTarget.isTerminated() || debugTarget.isDisconnected()) {
			emptyFolder();
			
			if (tabFolderHashtable != null)
			{
				if (tabFolderHashtable.containsKey(key))
				{
					TabFolder deleteFolder = (TabFolder)tabFolderHashtable.get(key);
					
					// dispose folder if not already disposed
					if (!deleteFolder.isDisposed())
						deleteFolder.dispose();
					
					tabFolderHashtable.remove(key);
				}
			}
			
			key = null;
			return;
		}
		
		// should never get here... added code for safety
		if (tabFolderHashtable == null)
		{
			emptyFolder();
			key = null;
			return;				
		}

		//if we've got a tabfolder to go with the IMemoryBlockRetrieval, display it
		if (tabFolderHashtable.containsKey(retrieve)) {
			if (stackLayout.topControl != (TabFolder)tabFolderHashtable.get(retrieve)) {
				setTabFolder((TabFolder)tabFolderHashtable.get(retrieve));
				parent.layout();
			}
		} else {	//otherwise, add a new one
			tabFolderHashtable.put(retrieve, new TabFolder(parent, SWT.NULL));
			setTabFolder((TabFolder)tabFolderHashtable.get(retrieve));
			parent.layout();
		}
		
		// restore view tabs based on memory block retrieval
		IMemoryBlock[] memoryBlocks = MemoryBlockManager.getMemoryBlockManager().getMemoryBlocks(retrieve);
		TabFolder toDisplay = (TabFolder)stackLayout.topControl;
		
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
					newViewTab.setEnabled(true);
					fSelectionProvider.setSelection(new StructuredSelection(newViewTab.getMemoryBlock()));
				}					
			}
		}			
		
		//set toolbar actions enabled/disabled
		updateToolBarActionsEnablement();		
	}
	
	protected void emptyFolder()
	{		
		setTabFolder(emptyTabFolder);
		if (!parent.isDisposed()) {
			parent.layout();
		}
		
		// this folder will always remain empty
		// remove button should always be disabled
		removeMemoryBlockAction.setEnabled(false);
		resetMemoryBlockAction.setEnabled(false);
		copyViewToClipboardAction.setEnabled(false);
		printViewTabAction.setEnabled(false);
		
		fSelectionProvider.setSelection(new StructuredSelection(new Object[0]));
	}
	
	private void createFolder(ISelection selection)
	{
		if (!(selection instanceof IStructuredSelection))
			return;

		//only single selection of PICLDebugElements is allowed for this action
		if (selection == null || selection.isEmpty() || ((IStructuredSelection)selection).size() > 1)
		{
			return;
		}

		Object elem = ((IStructuredSelection)selection).getFirstElement();

		// if not debug element
		if (!(elem instanceof IDebugElement))
			return;

		IDebugTarget debugTarget = ((IDebugElement)elem).getDebugTarget();
		IMemoryBlockRetrieval memRetrieval =(IMemoryBlockRetrieval) ((IDebugElement)elem).getAdapter(IMemoryBlockRetrieval.class);
		
		if (memRetrieval == null)
		{
			// if debug element returns null from getAdapter, assume its debug target is going to retrieve memory blocks
			memRetrieval = debugTarget;
		}	
		
		//if we've got a tabfolder to go with the IMemoryBlockRetrieval, display it
		if (tabFolderHashtable.containsKey(memRetrieval)) {
			if (stackLayout.topControl != (TabFolder)tabFolderHashtable.get(memRetrieval)) {
				setTabFolder((TabFolder)tabFolderHashtable.get(memRetrieval));
				parent.layout();
			}
		} else {	//otherwise, add a new one
			tabFolderHashtable.put(memRetrieval, new TabFolder(parent, SWT.NULL));
			setTabFolder((TabFolder)tabFolderHashtable.get(memRetrieval));
			parent.layout();
		}
			
		//set toolbar actions enabled/disabled
		updateToolBarActionsEnablement();
	}
	
	// enable/disable toolbar action 
	protected void updateToolBarActionsEnablement()
	{
		TabFolder folder = (TabFolder)stackLayout.topControl;
		int index = folder.getSelectionIndex();
		if (index >= 0) {
			removeMemoryBlockAction.setEnabled(true);
			resetMemoryBlockAction.setEnabled(true);
			copyViewToClipboardAction.setEnabled(true);
			printViewTabAction.setEnabled(true);
			
		} else {
			removeMemoryBlockAction.setEnabled(false);
			resetMemoryBlockAction.setEnabled(false);
			copyViewToClipboardAction.setEnabled(false);
			printViewTabAction.setEnabled(false);
		}		
	}
	
	private void restoreViewTabs(IMemoryBlock[] memoryBlocks)
	{
		for (int i=0; i<memoryBlocks.length; i++)
		{
			// enable to get latest data
			if (memoryBlocks[i] instanceof IExtendedMemoryBlock)
			{
				((IExtendedMemoryBlock)memoryBlocks[i]).enable();
			}
			MemoryBlockAdded(memoryBlocks[i]);
			
			// disable after done
			if (memoryBlocks[i] instanceof IExtendedMemoryBlock)
			{
				((IExtendedMemoryBlock)memoryBlocks[i]).disable();
			}
		}

		// enable memory block
		IMemoryViewTab viewTab = getTopMemoryTab();
		if (viewTab != null)
		{
			if (viewTab.getMemoryBlock() instanceof IExtendedMemoryBlock)
			{
				((IExtendedMemoryBlock)viewTab.getMemoryBlock()).enable();
			}
		}		
	}
	
	private void setTabFolder(TabFolder folder)
	{
		if (stackLayout.topControl != null)
		{
			TabFolder old = (TabFolder)stackLayout.topControl;
			
			if (!old.isDisposed())
			{	
				old.removeSelectionListener(fSelectionProvider);
				old.removeSelectionListener(fViewTabEnablementManager);
			}
		}
		
		stackLayout.topControl = folder;
		
		if (folder.getItemCount() > 0)
		{
			TabItem[] selectedItem = folder.getSelection();
			
			if (selectedItem.length > 0)
			{
				IMemoryViewTab viewTab = (IMemoryViewTab)selectedItem[0].getData();
				fSelectionProvider.setSelection(new StructuredSelection(viewTab.getMemoryBlock()));
			}
		}
		else
		{
			fSelectionProvider.setSelection(new StructuredSelection());
		}
		
		folder.addSelectionListener(fSelectionProvider);
		folder.addSelectionListener(fViewTabEnablementManager);
	}
	
	private void disposeViewTab(IMemoryViewTab viewTab, TabItem tabItem)
	{
		if (viewTab == null)
			return;
		
		// get menu manager and clean up
		IMenuManager menuMgr = (IMenuManager)fMenuMgr.get(viewTab);
		
		if (menuMgr != null)
		{
			menuMgr.dispose();
		}
		
		viewTab.dispose();

		if (tabItem == null)
			return;
		
		// dispose the tab item in case the view tab has not 
		// cleaned up the tab item
		if (!tabItem.isDisposed())
		{	
			tabItem.dispose();
		}			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryView#getAllViewTabs()
	 */
	public IMemoryViewTab[] getAllViewTabs() {
		
		// otherwise, find the view tab to display
		TabFolder folder = (TabFolder) stackLayout.topControl;
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
		TabFolder folder = (TabFolder) stackLayout.topControl;
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
				getTopMemoryTab().setEnabled(isEnabled);
				break;
			}
		}
	}
	
}
