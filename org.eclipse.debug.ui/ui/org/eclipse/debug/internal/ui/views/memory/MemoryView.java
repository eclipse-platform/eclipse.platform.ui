/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Ted Williams - [Memory View] Memory View: Workflow Enhancements (Bug 215432)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite2;
import org.eclipse.debug.ui.memory.IMemoryRenderingSynchronizationService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @since 3.0
 */
public class MemoryView extends ViewPart implements IMemoryRenderingSite2 {

	protected MemoryViewSelectionProvider fSelectionProvider;
	private MemoryViewPartListener fPartListener;
	
	private SashForm fSashForm;
	private Hashtable fViewPanes = new Hashtable();
	private Hashtable fViewPaneControls = new Hashtable();
	private ArrayList fVisibleViewPanes = new ArrayList();
	private boolean fVisible;
	
	private ArrayList fWeights = new ArrayList();
	
	private static final String VISIBILITY_PREF = IDebugUIConstants.ID_MEMORY_VIEW+".viewPanesVisibility"; //$NON-NLS-1$
	private static final String ID_MEMORY_VIEW_CONTEXT = "org.eclipse.debug.ui.memoryview"; //$NON-NLS-1$
	private static final String ID_ADD_MEMORY_BLOCK_COMMAND = "org.eclipse.debug.ui.commands.addMemoryMonitor"; //$NON-NLS-1$
	private static final String ID_TOGGLE_MEMORY_MONITORS_PANE_COMMAND = "org.eclipse.debug.ui.commands.toggleMemoryMonitorsPane"; //$NON-NLS-1$
	private static final String ID_NEXT_MEMORY_BLOCK_COMMAND = "org.eclipse.debug.ui.commands.nextMemoryBlock"; //$NON-NLS-1$
	private static final String ID_NEW_RENDERING_COMMAND = "org.eclipse.debug.ui.commands.newRendering"; //$NON-NLS-1$
	private static final String ID_CLOSE_RENDERING_COMMAND = "org.eclipse.debug.ui.commands.closeRendering"; //$NON-NLS-1$
	
	public static final String VIEW_PANE_ORIENTATION_PREF = IDebugUIConstants.ID_MEMORY_VIEW+".orientation"; //$NON-NLS-1$
	public static final int HORIZONTAL_VIEW_ORIENTATION = 0;
	public static final int VERTICAL_VIEW_ORIENTATION =1;

	private String[] defaultVisiblePaneIds ={MemoryBlocksTreeViewPane.PANE_ID, IDebugUIConstants.ID_RENDERING_VIEW_PANE_1};
		
	private MemoryBlocksTreeViewPane fMemBlkViewer;
	
	private MemoryViewSynchronizationService fSyncService;
	
	private boolean fPinMBDisplay = false;	// pin memory block display, on by default
	private static int fViewCnt = 0;

	private AbstractHandler fAddHandler;
	private AbstractHandler fToggleMonitorsHandler;
	private AbstractHandler fNextMemoryBlockHandler;
	private AbstractHandler fNewRenderingHandler;
	private AbstractHandler fCloseRenderingHandler;
	
	private ViewPaneOrientationAction[] fOrientationActions;
	private int fViewOrientation = HORIZONTAL_VIEW_ORIENTATION;
	
	private String fActivePaneId;
		
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
		fSashForm = new SashForm(parent, SWT.HORIZONTAL);
		
		fSelectionProvider = new MemoryViewSelectionProvider();
		
		// set up selection provider and listeners
		getSite().setSelectionProvider(fSelectionProvider);

		createMemoryBlocksTreeViewPane(fSashForm);
		createRenderingViewPane(IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
		createRenderingViewPane(IDebugUIConstants.ID_RENDERING_VIEW_PANE_2);
		
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
		
		createOrientationActions();
		loadOrientation();
		
		fPartListener = new MemoryViewPartListener(this);
		getSite().getPage().addPartListener(fPartListener);
		activateHandlers();
		// restore view pane after finishing creating all the view panes
		restoreView();
	}

	
    public void activateHandlers() {
		ICommandService commandSupport = (ICommandService)getSite().getService(ICommandService.class);		
		IHandlerService handlerService = (IHandlerService)getSite().getService(IHandlerService.class);
		IContextService contextSupport = (IContextService)getSite().getService(IContextService.class);
		
		if (commandSupport != null && handlerService != null && contextSupport != null)
		{
			contextSupport.activateContext(ID_MEMORY_VIEW_CONTEXT);
				
			fAddHandler = new AbstractHandler() {
					public Object execute(ExecutionEvent event) throws ExecutionException {
						IAdaptable context = DebugUITools.getPartDebugContext(getSite());
						if (context != null && MemoryViewUtil.isValidSelection(new StructuredSelection(context)))
						{
							RetargetAddMemoryBlockAction action = new RetargetAddMemoryBlockAction(MemoryView.this);
							action.run();
							action.dispose();
						}
						return null;
					}};
			handlerService.activateHandler(ID_ADD_MEMORY_BLOCK_COMMAND, fAddHandler);
			
			fToggleMonitorsHandler = new AbstractHandler() {
					public Object execute(ExecutionEvent event) throws ExecutionException {
						ToggleMemoryMonitorsAction action = new ToggleMemoryMonitorsAction();
						action.init(MemoryView.this);
						action.run();
						action.dispose();
						return null;
					}
				};
			
			handlerService.activateHandler(ID_TOGGLE_MEMORY_MONITORS_PANE_COMMAND, fToggleMonitorsHandler);
			
			fNextMemoryBlockHandler = new AbstractHandler() {

				public Object execute(ExecutionEvent event)
						throws ExecutionException {
					SwitchMemoryBlockAction action = new SwitchMemoryBlockAction();
					action.init(MemoryView.this);
					action.run();
					action.dispose();
					return null;
				}
			};
			handlerService.activateHandler(ID_NEXT_MEMORY_BLOCK_COMMAND, fNextMemoryBlockHandler);
			
			fCloseRenderingHandler = new AbstractHandler() {

				public Object execute(ExecutionEvent event)
						throws ExecutionException {
					
					IMemoryRenderingContainer container = getContainer(fActivePaneId);
					if (container != null)
					{
						if (container instanceof RenderingViewPane) {
							if (!((RenderingViewPane) container).canRemoveRendering())
								return null;
						}
						IMemoryRendering activeRendering = container.getActiveRendering();
						if (activeRendering != null)
						{
							container.removeMemoryRendering(activeRendering);
						}
					}

					return null;
				}
			};
			handlerService.activateHandler(ID_CLOSE_RENDERING_COMMAND, fCloseRenderingHandler);
			
			fNewRenderingHandler = new AbstractHandler() {

				public Object execute(ExecutionEvent event)
						throws ExecutionException {

					IMemoryRenderingContainer container = getContainer(fActivePaneId);							
					
					if (container != null && container instanceof RenderingViewPane)
					{
						RenderingViewPane pane = (RenderingViewPane)container;
						if (pane.canAddRendering())
							pane.showCreateRenderingTab();
					}
					return null;
				}
			};
			handlerService.activateHandler(ID_NEW_RENDERING_COMMAND, fNewRenderingHandler);
		}
    }
	
	/**
	 * 
	 */
	private void createMemoryBlocksTreeViewPane(Composite parent) {
		
		fMemBlkViewer = new MemoryBlocksTreeViewPane(this);
		fViewPanes.put(MemoryBlocksTreeViewPane.PANE_ID, fMemBlkViewer);
		ViewForm viewerViewForm = new ViewForm(parent, SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewerViewForm, IDebugUIConstants.PLUGIN_ID + ".MemoryView_context"); //$NON-NLS-1$
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
		
		fMemBlkViewer.getControl().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				fActivePaneId = fMemBlkViewer.getId();
			}});
	}

	/**
	 * @return an activate listener for the given view pane
	 * 
	 */
	private Listener createDeactivateListener(final IMemoryViewPane viewPane) {
		Listener deactivateListener = new Listener() {
			private String id=viewPane.getId();
			public void handleEvent(Event event) {
				if (fActivePaneId.equals(id))
					viewPane.removeSelctionListener(fSelectionProvider);
			}};
		return deactivateListener;
	}

	/**
	 * @return a deactivate listener for the given view pane
	 */
	private Listener createActivateListener(final IMemoryViewPane viewPane) {
		Listener activateListener = new Listener() {
			private String id=viewPane.getId(); 
			public void handleEvent(Event event) {
				fActivePaneId = id;
				viewPane.addSelectionListener(fSelectionProvider);
				fSelectionProvider.setSelection(viewPane.getSelectionProvider().getSelection());
			}};
		return activateListener;
	}

	/**
	 * 
	 */
	public void createRenderingViewPane(final String paneId) {
		final RenderingViewPane renderingPane = new RenderingViewPane(this); 
		fViewPanes.put(paneId, renderingPane);
		ViewForm renderingViewForm = new ViewForm(fSashForm, SWT.NONE);
		fViewPaneControls.put(paneId, renderingViewForm);
		fWeights.add(new Integer(40));
		
		Control renderingControl = renderingPane.createViewPane(renderingViewForm, paneId, DebugUIMessages.MemoryView_Memory_renderings, true, true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(renderingControl, IDebugUIConstants.PLUGIN_ID + ".MemoryView_context"); //$NON-NLS-1$
		renderingViewForm.setContent(renderingControl);
		
		Listener renderingActivateListener = createActivateListener(renderingPane);
		renderingControl.addListener(SWT.Activate, renderingActivateListener);
		
		Listener renderingDeactivateListener = createDeactivateListener(renderingPane);
		renderingControl.addListener(SWT.Deactivate, renderingDeactivateListener);

	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (fActivePaneId == null)
			fActivePaneId = fMemBlkViewer.getId();
		
		IMemoryViewPane pane = getViewPane(fActivePaneId);
		pane.getControl().setFocus();
	}
	
	public void dispose() {
	    getSite().getPage().removePartListener(fPartListener);
	    
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
		
		if (fAddHandler != null)
			fAddHandler.dispose();
		
		if (fToggleMonitorsHandler != null)
			fToggleMonitorsHandler.dispose();
		
		if (fNextMemoryBlockHandler != null)
			fNextMemoryBlockHandler.dispose();
		
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
	
	private void loadOrientation()
	{
		Preferences prefs = DebugUIPlugin.getDefault().getPluginPreferences();
		fViewOrientation = prefs.getInt(getOrientationPrefId());
		
		for (int i=0; i<fOrientationActions.length; i++)
		{
			if (fOrientationActions[i].getOrientation() == fViewOrientation)
			{
				fOrientationActions[i].run();
			}
		}
		updateOrientationActions();
	}
	
	private void saveOrientation()
	{
		Preferences prefs = DebugUIPlugin.getDefault().getPluginPreferences();
		prefs.setValue(getOrientationPrefId(), fViewOrientation);
	}
	
	private void updateOrientationActions()
	{
		for (int i=0; i<fOrientationActions.length; i++)
		{
			if (fOrientationActions[i].getOrientation() == fViewOrientation)
			{
				fOrientationActions[i].setChecked(true);
			}
			else
			{
				fOrientationActions[i].setChecked(false);
			}
			
		}
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
	
	private String getOrientationPrefId()
	{
		IViewSite vs = getViewSite();
		String viewId = vs.getSecondaryId();
		
		if (viewId != null)
			return VIEW_PANE_ORIENTATION_PREF + "." + viewId; //$NON-NLS-1$

		return VIEW_PANE_ORIENTATION_PREF;
	}
	
	private void createOrientationActions()
	{
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();
		
		fOrientationActions = new ViewPaneOrientationAction[2];
		fOrientationActions[0] = new ViewPaneOrientationAction(this, HORIZONTAL_VIEW_ORIENTATION);
		fOrientationActions[1] = new ViewPaneOrientationAction(this, VERTICAL_VIEW_ORIENTATION);
		
		viewMenu.add(new Separator());
		MenuManager layoutSubMenu = new MenuManager(VariablesViewMessages.VariablesView_40);
		layoutSubMenu.add(fOrientationActions[0]);
		layoutSubMenu.add(fOrientationActions[1]);
		viewMenu.add(layoutSubMenu);
		viewMenu.add(new Separator());
	}
	
	public void setViewPanesOrientation(int orientation)
	{
		fViewOrientation = orientation;
		if (fViewOrientation == VERTICAL_VIEW_ORIENTATION)
			fSashForm.setOrientation(SWT.VERTICAL);
		else
			fSashForm.setOrientation(SWT.HORIZONTAL);
		
		saveOrientation();
		updateOrientationActions();
	}
	
	public int getViewPanesOrientation()
	{
		return fViewOrientation;
	}

	public void setContainerVisible(String id, boolean visible) {
		showViewPane(visible, id);
	}
}
