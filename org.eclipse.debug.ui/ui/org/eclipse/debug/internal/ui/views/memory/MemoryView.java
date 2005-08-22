/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IMemoryBlockListener;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingSynchronizationService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @since 3.0
 */
public class MemoryView extends ViewPart implements IMemoryRenderingSite {

	protected MemoryViewSelectionProvider fSelectionProvider;
	private MemoryViewPartListener fPartListener;
	
	private SashForm fSashForm;
	private Hashtable fViewPanes = new Hashtable();
	private Hashtable fViewPaneControls = new Hashtable();
	private ArrayList fVisibleViewPanes = new ArrayList();
	private boolean fVisible;
	
	private ArrayList fWeights = new ArrayList();
	
	private static final String VISIBILITY_PREF = IInternalDebugUIConstants.ID_MEMORY_VIEW+".viewPanesVisibility"; //$NON-NLS-1$
	private static final String ID_MEMORY_VIEW_CONTEXT = "org.eclipse.debug.ui.memoryview"; //$NON-NLS-1$
	private static final String ID_ADD_MEMORY_BLOCK_COMMAND = "org.eclipse.debug.ui.commands.addMemoryMonitor"; //$NON-NLS-1$
	
	private String[] defaultVisiblePaneIds ={MemoryBlocksTreeViewPane.PANE_ID, IInternalDebugUIConstants.ID_RENDERING_VIEW_PANE_1};
		
	private MemoryBlocksTreeViewPane fMemBlkViewer;
	
	private MemoryViewSynchronizationService fSyncService;
	
	private boolean fPinMBDisplay = true;	// pin memory block display, on by default
	private static int fViewCnt = 0;

	private IContextActivation fContext;
	
	private Set fRegisteredMemoryBlocks = new HashSet();
	private IMemoryBlockListener fMemoryBlockListener = new IMemoryBlockListener() {

		public void memoryBlocksAdded(IMemoryBlock[] memory) {
			// do nothing
		}

		public void memoryBlocksRemoved(IMemoryBlock[] memory) {
			// clean up registered memory blocks
			unRegisterMemoryBlocks(memory);
		}};
	private AbstractHandler fAddHandler;
		
	class MemoryViewSelectionProvider implements ISelectionProvider, ISelectionChangedListener
	{
		ArrayList fListeners = new ArrayList();
		
		IStructuredSelection fSelections = new StructuredSelection();

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
			return fSelections;
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
			if (selection instanceof IStructuredSelection)
			{		
				boolean fireChanged = false;
				
				// only fire change event if the selection has really changed
				if (fSelections.getFirstElement() != ((IStructuredSelection)selection).getFirstElement())
					fireChanged = true;
				
				fSelections = (IStructuredSelection)selection;
				
				if (fireChanged)
					fireChanged();
			}
		}
		
		public void fireChanged()
		{
			SelectionChangedEvent evt = new SelectionChangedEvent(this, getSelection());
			for (int i=0; i<fListeners.size(); i++)
			{
				((ISelectionChangedListener)fListeners.get(i)).selectionChanged(evt);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection viewPaneSelection = event.getSelection();
			
			if (viewPaneSelection instanceof IStructuredSelection)
			{
				Object selected = ((IStructuredSelection)viewPaneSelection).getFirstElement();
			
				if (selected != null)
				{
					StructuredSelection strucSelection = new StructuredSelection(new Object[]{selected});
					setSelection(strucSelection);
				}
				else
				{
					setSelection(viewPaneSelection);
				}
			}
		}
	}
	
	class MemoryViewPartListener implements IPartListener2
	{
		IMemoryRenderingSite fView = null;
		
		public MemoryViewPartListener(IMemoryRenderingSite view)
		{
			fView = view;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partActivated(IWorkbenchPartReference ref) {
			if (ref.getPart(false) == fView)
				activated();
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
			if (ref.getPart(false) == fView)
				deactivated();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partHidden(IWorkbenchPartReference ref) {
			
			IWorkbenchPart part = ref.getPart(false);
			
			if (part == fView)
			{
				setVisible(false);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partVisible(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			
			if (part == fView)
			{
				setVisible(true);
				restoreView();
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
	
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		fViewCnt ++;
		String secondaryId = site.getSecondaryId();
		if (secondaryId != null)
			MemoryViewIdRegistry.registerView(secondaryId);
		
		// only do this the first time
		// not sure if there is a bug in the UI... if the view is
		// not a primary view and if it's hidden, the view is not
		// init and created until it becomes visible.
		if (fViewCnt == 1)
		{
			// also try to find other views and register
			if (DebugUIPlugin.getActiveWorkbenchWindow() != null &&
				DebugUIPlugin.getActiveWorkbenchWindow().getActivePage() != null)
			{
				IViewReference references[] = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage().getViewReferences();
				for (int i=0; i<references.length; i++)
				{
					if (references[i].getSecondaryId() != null)
					{
						MemoryViewIdRegistry.registerView(references[i].getSecondaryId());
					}
				}
			}
		}
		
		fSyncService = new MemoryViewSynchronizationService();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".MemoryView_context"); //$NON-NLS-1$
		
		fSashForm = new SashForm(parent, SWT.HORIZONTAL);
		fSelectionProvider = new MemoryViewSelectionProvider();
		
		createMemoryBlocksTreeViewPane(fSashForm);
		createRenderingViewPane(IInternalDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
		createRenderingViewPane(IInternalDebugUIConstants.ID_RENDERING_VIEW_PANE_2);
		
		setVisible(true);
		
		// set up weights in sashform
		Integer[] weights = (Integer[])fWeights.toArray(new Integer[fWeights.size()]);
		int[] intWeights = new int[weights.length];
		for (int i=0; i<weights.length; i++)
		{
			intWeights[i] = weights[i].intValue();
		}
		fSashForm.setWeights(intWeights);

		loadViewPanesVisibility();
		
		// set up selection provider and listeners
		
		getSite().setSelectionProvider(fSelectionProvider);

		
		fPartListener = new MemoryViewPartListener(this);
		getSite().getPage().addPartListener(fPartListener);
		DebugPlugin.getDefault().getMemoryBlockManager().addListener(fMemoryBlockListener);
		
		// restore view pane after finishing creating all the view panes
		restoreView();
	}
	
    public void activated() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandSupport = (ICommandService)workbench.getAdapter(ICommandService.class);
		IContextService contextSupport = (IContextService)workbench.getAdapter(IContextService.class);
		
		if (commandSupport != null && contextSupport != null)
		{
			fContext = contextSupport.activateContext(ID_MEMORY_VIEW_CONTEXT);
			Command command = commandSupport.getCommand(ID_ADD_MEMORY_BLOCK_COMMAND);
			
			// dynamically change handler on Add Memory Monitor command based
			// on which Memory View is active
			if (fAddHandler == null)
			{
				fAddHandler = new AbstractHandler() {
						public Object execute(ExecutionEvent event) throws ExecutionException {
							IAdaptable context = DebugUITools.getDebugContext();
							if (context != null && MemoryViewUtil.isValidSelection(new StructuredSelection(context)))
							{
								AddMemoryBlockAction action = new AddMemoryBlockAction(MemoryView.this);
								action.run();
								action.dispose();
							}
							return null;
						}};
			}
			command.setHandler(fAddHandler);
		}
    }
    
    public void deactivated()
    {
    	IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandSupport = (ICommandService)workbench.getAdapter(ICommandService.class);
		IContextService contextSupport = (IContextService)workbench.getAdapter(IContextService.class);
		
		if (commandSupport != null && contextSupport != null)
		{
			// 	remove handler
			Command command = commandSupport.getCommand(ID_ADD_MEMORY_BLOCK_COMMAND);
			command.setHandler(null);
			
			if (fContext != null)
				contextSupport.deactivateContext(fContext);
		}
    }
	
	/**
	 * 
	 */
	private void createMemoryBlocksTreeViewPane(Composite parent) {
		
		fMemBlkViewer = new MemoryBlocksTreeViewPane(this);
		fViewPanes.put(MemoryBlocksTreeViewPane.PANE_ID, fMemBlkViewer);
		ViewForm viewerViewForm = new ViewForm(parent, SWT.FLAT);
		fViewPaneControls.put(MemoryBlocksTreeViewPane.PANE_ID, viewerViewForm);
		fWeights.add(new Integer(15));
		
		fMemBlkViewer.addSelectionListener(fSelectionProvider);
		
		Control viewerControl = fMemBlkViewer.createViewPane(viewerViewForm, MemoryBlocksTreeViewPane.PANE_ID, DebugUIMessages.MemoryView_Memory_monitors);
		viewerViewForm.setContent(viewerControl);
		
		ISelection selection = fMemBlkViewer.getSelectionProvider().getSelection();
		if (selection != null)
			fSelectionProvider.setSelection(selection);	

		ToolBarManager viewerToolBarMgr = new ToolBarManager(SWT.FLAT);	
		IAction[] actions = fMemBlkViewer.getActions();
		for (int i=0; i<actions.length; i++)
		{
			viewerToolBarMgr.add(actions[i]);
		}
		ToolBar viewerToolbar = viewerToolBarMgr.createControl(viewerViewForm);
		viewerViewForm.setTopRight(viewerToolbar);
		
		Label viewerLabel = new Label(viewerViewForm, SWT.WRAP);
		viewerLabel.setText(DebugUIMessages.MemoryView_Memory_monitors); 
		viewerViewForm.setTopLeft(viewerLabel);
	}

	/**
	 * 
	 */
	public void createRenderingViewPane(String paneId) {
		RenderingViewPane renderingPane = new RenderingViewPane(this); 
		fViewPanes.put(paneId, renderingPane);
		ViewForm renderingViewForm = new ViewForm(fSashForm, SWT.FLAT);
		fViewPaneControls.put(paneId, renderingViewForm);
		fWeights.add(new Integer(40));
		
		Control renderingControl = renderingPane.createViewPane(renderingViewForm, paneId, DebugUIMessages.MemoryView_Memory_renderings); 
		renderingViewForm.setContent(renderingControl);
		renderingPane.addSelectionListener(fSelectionProvider);
		
		ToolBarManager renderingViewMgr = new ToolBarManager(SWT.FLAT);
		IAction[] renderingActions = renderingPane.getActions();
		for (int i=0; i<renderingActions.length; i++)
		{
			renderingViewMgr.add(renderingActions[i]);
		}
		ToolBar renderingToolbar = renderingViewMgr.createControl(renderingViewForm);
		renderingViewForm.setTopRight(renderingToolbar);
		
		Label renderingLabel = new Label(renderingViewForm, SWT.NONE);
		renderingLabel.setText(renderingPane.getLabel());
		renderingViewForm.setTopLeft(renderingLabel);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	
	public void dispose() {
	    getSite().getPage().removePartListener(fPartListener);
	    DebugPlugin.getDefault().getMemoryBlockManager().removeListener(fMemoryBlockListener);
	    
		Enumeration enumeration = fViewPanes.elements();
		while (enumeration.hasMoreElements())
		{
			Object element = enumeration.nextElement();
			if (element instanceof IMemoryViewPane)
			{
				((IMemoryViewPane)element).dispose();
			}
		}
		
		fViewPaneControls.clear();
		
		IViewSite viewSite = getViewSite();
		String secondaryId = viewSite.getSecondaryId();
		if (secondaryId != null)
			MemoryViewIdRegistry.deregisterView(secondaryId);
		
		fSyncService.shutdown();
		
		fRegisteredMemoryBlocks.clear();
		
		super.dispose();
	}
	
	private void setVisible(boolean visible)
	{
		IMemoryViewPane[] viewPanes = getViewPanes();
		
		for (int i=0; i<viewPanes.length; i++)
		{
			// if currently visible, take view pane's visibility into account
			// else force view pane to be visible if it is listed in
			// "visible view panes" array list.
			if (fVisible)
				viewPanes[i].setVisible(visible && viewPanes[i].isVisible());
			else
			{
				if (isViewPaneVisible(viewPanes[i].getId()))
					viewPanes[i].setVisible(visible);
			}	
		}
		
		fVisible = visible;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#getViewPane(java.lang.String)
	 */
	public IMemoryViewPane getViewPane(String paneId) {
		return (IMemoryViewPane)fViewPanes.get(paneId);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#getViewPanes()
	 */
	public IMemoryViewPane[] getViewPanes() {
		IMemoryViewPane[] viewPanes = new IMemoryViewPane[fViewPanes.size()];

		Enumeration enumeration = fViewPanes.elements();
		int i=0;
		while (enumeration.hasMoreElements())
		{
			viewPanes[i] = (IMemoryViewPane)enumeration.nextElement();
			i++;
		}
		
		return viewPanes;
	}
	
	/**
	 * Restore each view pane from the memory view based on current
	 * debug selection
	 */
	private void restoreView() {
		IMemoryViewPane[] viewPanes = getViewPanes();
		for (int i=0; i<viewPanes.length; i++)
		{
			viewPanes[i].restoreViewPane();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#showViewPane(boolean, java.lang.String)
	 */
	public void showViewPane(boolean show, String paneId) {
		
		Control viewPaneControl = (Control)fViewPaneControls.get(paneId);
		
		if (viewPaneControl != null)
		{
			Control children[] = fSashForm.getChildren();
			
			for (int i=0; i<children.length; i++)
			{
				if (children[i] == viewPaneControl)
				{
					children[i].setVisible(show);
					IMemoryViewPane viewPane = (IMemoryViewPane)fViewPanes.get(paneId);
					if (viewPane != null)
						viewPane.setVisible(show);
				}
			}
			fSashForm.layout();
		}
		
		storeViewPaneVisibility();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#isViewPaneVisible(java.lang.String)
	 */
	public boolean isViewPaneVisible(String paneId) {
		return fVisibleViewPanes.contains(paneId);
	}
	
	private void storeViewPaneVisibility()
	{		
		fVisibleViewPanes.clear();
		Preferences prefs = DebugUIPlugin.getDefault().getPluginPreferences();
		StringBuffer visibleViewPanes= new StringBuffer();
		
		Enumeration enumeration = fViewPaneControls.keys();
		
		while (enumeration.hasMoreElements())
		{
			String paneId = (String)enumeration.nextElement();
			
			Control control = (Control)fViewPaneControls.get(paneId);
			if (control.isVisible())
			{
				visibleViewPanes.append(paneId);
				visibleViewPanes.append(","); //$NON-NLS-1$
				fVisibleViewPanes.add(paneId);
			}
		}
		
		prefs.setValue(getVisibilityPrefId(), visibleViewPanes.toString());		 
	}
	
	private void loadViewPanesVisibility()
	{
		Preferences prefs = DebugUIPlugin.getDefault().getPluginPreferences();
		String visiblePanes = prefs.getString(getVisibilityPrefId());
		
		if (visiblePanes != null && visiblePanes.length() > 0)
		{
			StringTokenizer tokenizer = new StringTokenizer(visiblePanes, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens())
			{
				String paneId = tokenizer.nextToken();
				fVisibleViewPanes.add(paneId);
			}
		}
		else
		{
			for (int i=0 ;i<defaultVisiblePaneIds.length; i++)
			{
				fVisibleViewPanes.add(defaultVisiblePaneIds[i]);
			}
		}
		
		Enumeration enumeration = fViewPaneControls.keys();
		while (enumeration.hasMoreElements())
		{
			String paneId = (String)enumeration.nextElement();
			boolean visible = false;
			if(fVisibleViewPanes.contains(paneId))
				visible = true;
			
			Control control = (Control)fViewPaneControls.get(paneId);
			control.setVisible(visible);
			
			IMemoryViewPane viewPane = (IMemoryViewPane)fViewPanes.get(paneId);
			viewPane.setVisible(visible);
		}
		
		fSashForm.layout();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingSite#getSynchronizationService()
	 */
	public IMemoryRenderingSynchronizationService getSynchronizationService() {
		return fSyncService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingSite#getMemoryRenderingContainers()
	 */
	public IMemoryRenderingContainer[] getMemoryRenderingContainers() {
		Enumeration enumeration = fViewPanes.elements();
		ArrayList containers = new ArrayList();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof IMemoryRenderingContainer)
				containers.add(obj);
		}
		
		return (IMemoryRenderingContainer[])containers.toArray(new IMemoryRenderingContainer[containers.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingSite#getContainer(java.lang.String)
	 */
	public IMemoryRenderingContainer getContainer(String id) {
		Object viewPane =  fViewPanes.get(id);
		
		if (viewPane instanceof IMemoryRenderingContainer)
			return (IMemoryRenderingContainer)viewPane;
		
		return null;
	}

	public boolean isPinMBDisplay() {
		return fPinMBDisplay;
	}
	

	public void setPinMBDisplay(boolean pinMBDisplay) {
		fPinMBDisplay = pinMBDisplay;
	}
	
	private String getVisibilityPrefId()
	{
		IViewSite vs = getViewSite();
		String viewId = vs.getSecondaryId();
		
		if (viewId != null)
			return VISIBILITY_PREF + "." + viewId; //$NON-NLS-1$

		return VISIBILITY_PREF;
	}
	
	public void registerMemoryBlocks(IMemoryBlock[] memoryBlocks)
	{
		for (int i=0; i<memoryBlocks.length; i++)
			fRegisteredMemoryBlocks.add(memoryBlocks[i]);
	}
	
	public void unRegisterMemoryBlocks(IMemoryBlock[] memoryBlocks)
	{
		for (int i=0; i<memoryBlocks.length; i++)
			fRegisteredMemoryBlocks.remove(memoryBlocks[i]);
	}
	
	public boolean isMemoryBlockRegistered(IMemoryBlock memoryBlock)
	{
		return fRegisteredMemoryBlocks.contains(memoryBlock);
	}
}
