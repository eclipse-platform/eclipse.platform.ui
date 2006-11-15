/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;


import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.AddToFavoritesAction;
import org.eclipse.debug.internal.ui.actions.EditLaunchConfigurationAction;
import org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.DisconnectCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.DropToFrameCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.ResumeCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepIntoCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepOverCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepReturnCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.SuspendCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.TerminateAllAction;
import org.eclipse.debug.internal.ui.commands.actions.TerminateAndRelaunchAction;
import org.eclipse.debug.internal.ui.commands.actions.TerminateAndRemoveAction;
import org.eclipse.debug.internal.ui.commands.actions.TerminateCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.ToggleStepFiltersAction;
import org.eclipse.debug.internal.ui.sourcelookup.EditSourceLookupPathAction;
import org.eclipse.debug.internal.ui.sourcelookup.LookupSourceAction;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.VirtualFindAction;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.AbstractDebugContextProvider;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.progress.UIJob;

public class LaunchView extends AbstractDebugView implements ISelectionChangedListener, IPerspectiveListener2, IPageListener, IShowInTarget, IShowInSource, IShowInTargetList, IPartListener2 {
	
	public static final String ID_CONTEXT_ACTIVITY_BINDINGS = "contextActivityBindings"; //$NON-NLS-1$

    private static final String TERMINATE = "terminate"; //$NON-NLS-1$

    private static final String DISCONNECT = "disconnect"; //$NON-NLS-1$

    private static final String SUSPEND = "suspend"; //$NON-NLS-1$

    private static final String RESUME = "resume"; //$NON-NLS-1$

    private static final String STEP_RETURN = "step_return"; //$NON-NLS-1$

    private static final String STEP_OVER = "step_over"; //$NON-NLS-1$

    private static final String DROP_TO_FRAME = "drop_to_frame"; //$NON-NLS-1$

    private static final String STEP_INTO = "step_into"; //$NON-NLS-1$

    private static final String TERMINATE_AND_REMOVE = "terminate_and_remove"; //$NON-NLS-1$
    
    private static final String TERMINATE_ALL = "terminate_all"; //$NON-NLS-1$

    private static final String TERMINATE_AND_RELAUNCH = "terminate_relaunch"; //$NON-NLS-1$
    
    private static final String TOGGLE_STEP_FILTERS = "toggle_step_filters"; //$NON-NLS-1$
			
	/**
	 * Whether this view is in the active page of a perspective.
	 */
	private boolean fIsActive = true; 	
		
	/**
	 * Model presentation or <code>null</code> if none
	 */
	private IDebugModelPresentation fPresentation = null;
	
	private EditLaunchConfigurationAction fEditConfigAction = null;
	private AddToFavoritesAction fAddToFavoritesAction = null;
	private EditSourceLookupPathAction fEditSourceAction = null;
	private LookupSourceAction fLookupAction = null;

	class ContextProvider extends AbstractDebugContextProvider implements IModelChangedListener {
		
		private ISelection fContext = null;
		private TreeModelViewer fViewer = null;
		private Visitor fVisitor = new Visitor();
		
		class Visitor implements IModelDeltaVisitor {
			public boolean visit(IModelDelta delta, int depth) {
				Object element = delta.getElement();
				if ((delta.getFlags() & IModelDelta.STATE) > 0) {
					if ((delta.getFlags() & (IModelDelta.CONTENT | IModelDelta.SELECT)) == 0) {
						// state change without content/select - possible state change of active context
						possibleChange(element, DebugContextEvent.STATE);
					}
				} else if ((delta.getFlags() & IModelDelta.CONTENT) > 0) {
					if ((delta.getFlags() & IModelDelta.SELECT) == 0) {
						// content change without select > possible activation
						possibleChange(element, DebugContextEvent.ACTIVATED);
					}
				}
				return true;
			}	
		}
		
		public ContextProvider(TreeModelViewer viewer) {
			super(LaunchView.this);
			fViewer = viewer;
			fViewer.addModelChangedListener(this);
		}
		
		protected void dispose() { 
			fContext = null;
			fViewer.removeModelChangedListener(this);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.contexts.IDebugContextProvider#getActiveContext()
		 */
		public synchronized ISelection getActiveContext() {
			return fContext;
		}	
		
		protected void activate(ISelection selection) {
			synchronized (this) {
				fContext = selection;
			}
			fire(new DebugContextEvent(this, selection, DebugContextEvent.ACTIVATED));
		}
		
		protected void possibleChange(Object element, int type) {
			synchronized (this) {
				if (fContext instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) fContext;
					if (!(ss.size() == 1 && ss.getFirstElement().equals(element))) {
						return;
					}
				} else {
					return;
				}
			}
			DebugContextEvent event = new DebugContextEvent(this, fContext, type);
			if (getControl().getDisplay().getThread() == Thread.currentThread()) {
				fire(event);
			} else {
				final DebugContextEvent finalEvent = event;
				Job job = new UIJob("context change") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						fire(finalEvent);
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta)
		 */
		public void modelChanged(IModelDelta delta, IModelProxy proxy) {
			delta.accept(fVisitor);
		}
		
	}
	
	/**
	 * Context provider
	 */
	private ContextProvider fProvider;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.DEBUG_VIEW;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		setAction("Properties", new PropertyDialogAction(getSite(), getSite().getSelectionProvider())); //$NON-NLS-1$
		fEditConfigAction = new EditLaunchConfigurationAction();
		fAddToFavoritesAction = new AddToFavoritesAction();
		fEditSourceAction = new EditSourceLookupPathAction(this);
		fLookupAction = new LookupSourceAction(this);
		setAction(FIND_ACTION, new VirtualFindAction((InternalTreeModelViewer) getViewer())); 
        
        addCapabilityAction(new TerminateCommandAction(), TERMINATE);
        addCapabilityAction(new DisconnectCommandAction(), DISCONNECT);
        addCapabilityAction(new SuspendCommandAction(), SUSPEND);
        addCapabilityAction(new ResumeCommandAction(), RESUME);
        addCapabilityAction(new StepReturnCommandAction(), STEP_RETURN);
        addCapabilityAction(new StepOverCommandAction(), STEP_OVER);
        addCapabilityAction(new StepIntoCommandAction(), STEP_INTO);
        addCapabilityAction(new DropToFrameCommandAction(), DROP_TO_FRAME);
        addCapabilityAction(new TerminateAndRemoveAction(), TERMINATE_AND_REMOVE);
        addCapabilityAction(new TerminateAndRelaunchAction(), TERMINATE_AND_RELAUNCH);
        addCapabilityAction(new TerminateAllAction(), TERMINATE_ALL);
        addCapabilityAction(new ToggleStepFiltersAction(), TOGGLE_STEP_FILTERS);
	}
	

	/**
	 * Initializes the action and associates it with the given id.
	 * 
	 * @param capability
	 * @param actionID
	 */
	private void addCapabilityAction(DebugCommandAction capability, String actionID) {
		capability.init(this);
		setAction(actionID, capability);
	}
	
	/**
	 * Disposes the given action.
	 * 
	 * @param actionID
	 */
	private void disposeCommandAction(String actionID) {
		DebugCommandAction action = (DebugCommandAction) getAction(actionID);
		action.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		fPresentation = new DelegatingModelPresentation();
		TreeModelViewer viewer = new TreeModelViewer(parent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL,
				new DebugModelPresentationContext(IDebugUIConstants.ID_DEBUG_VIEW, fPresentation));
        
        viewer.addSelectionChangedListener(this);
        viewer.getControl().addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent event) {
        		if (event.character == SWT.DEL && event.stateMask == 0) {
        			handleDeleteKeyPressed();
        		}
        	}
        });
        // add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(viewer);
		viewer.setInput(DebugPlugin.getDefault().getLaunchManager());
		//setEventHandler(new LaunchViewEventHandler(this));
		fProvider = new ContextProvider(viewer);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextProvider(fProvider);
		return viewer;
	}
		
	private void handleDeleteKeyPressed() {
		IStructuredSelection selection= (IStructuredSelection) getViewer().getSelection();
		Iterator iter= selection.iterator();
		Object item;
		boolean itemsToTerminate= false;
		ITerminate terminable;
		while (iter.hasNext()) {
			item= iter.next();
			if (item instanceof ITerminate) {
				terminable= (ITerminate) item;
				if (terminable.canTerminate() && !terminable.isTerminated()) {
					itemsToTerminate= true;
					break;
				}
			}
		}
		if (itemsToTerminate) {
			// Prompt the user to proceed with termination
			if (!MessageDialog.openQuestion(getSite().getShell(), DebugUIViewsMessages.LaunchView_Terminate_and_Remove_1, DebugUIViewsMessages.LaunchView_Terminate_and_remove_selected__2)) {  
				return;
			}
		}
		MultiStatus status= new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, DebugUIViewsMessages.LaunchView_Exceptions_occurred_attempting_to_terminate_and_remove_3, null); 
		iter= selection.iterator(); 
		while (iter.hasNext()) {
			try {
				terminateAndRemove(iter.next());
			} catch (DebugException exception) {
				status.merge(exception.getStatus());				
			}
		}
		if (!status.isOK()) {
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				DebugUIPlugin.errorDialog(window.getShell(), DebugUIViewsMessages.LaunchView_Terminate_and_Remove_4, DebugUIViewsMessages.LaunchView_Terminate_and_remove_failed_5, status);  
			} else {
				DebugUIPlugin.log(status);
			}
		}
	}
	
	/**
	 * Terminates and removes the given element from the launch manager
	 */
	public static void terminateAndRemove(Object element) throws DebugException {
		ILaunch launch= null;
		ITerminate terminable = null;
		if (element instanceof ILaunch) {
			launch= (ILaunch) element;
		} else if (element instanceof IDebugElement) {
			launch= ((IDebugElement) element).getLaunch();
		} else if (element instanceof IProcess) {
			launch= ((IProcess) element).getLaunch();
		}		
		terminable = launch;
		if (terminable == null) {
			if (element instanceof ITerminate) {
				terminable = (ITerminate) element;
			}
		}
		if (terminable == null) {
			return;
		}
		if (!(terminable.canTerminate() || terminable.isTerminated())) {
			// Don't try to terminate or remove attached launches
			return;
		}
		try {
			if (!terminable.isTerminated()) {
				terminable.terminate();
			}
		} finally {
			if (launch != null) {
				ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
				lManager.removeLaunch(launch);		
			}
		}
	}

	private void commonInit(IViewSite site) {
		site.getPage().addPartListener((IPartListener2) this);
		site.getWorkbenchWindow().addPageListener(this);
		site.getWorkbenchWindow().addPerspectiveListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		commonInit(site);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		commonInit(site);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(IDebugUIConstants.THREAD_GROUP));
		tbm.add(new Separator(IDebugUIConstants.STEP_GROUP));
		tbm.add(new GroupMarker(IDebugUIConstants.STEP_INTO_GROUP));
		tbm.add(new GroupMarker(IDebugUIConstants.STEP_OVER_GROUP));
		tbm.add(new GroupMarker(IDebugUIConstants.STEP_RETURN_GROUP));
		tbm.add(new GroupMarker(IDebugUIConstants.EMPTY_STEP_GROUP));
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
        
        tbm.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(RESUME));
        tbm.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(SUSPEND));
        tbm.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(TERMINATE));
        tbm.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(DISCONNECT));
        
        tbm.appendToGroup(IDebugUIConstants.STEP_INTO_GROUP, getAction(STEP_INTO));
        tbm.appendToGroup(IDebugUIConstants.STEP_OVER_GROUP, getAction(STEP_OVER));
        tbm.appendToGroup(IDebugUIConstants.STEP_RETURN_GROUP, getAction(STEP_RETURN));
        
        tbm.appendToGroup(IDebugUIConstants.EMPTY_STEP_GROUP, getAction(DROP_TO_FRAME));
        
        tbm.appendToGroup(IDebugUIConstants.RENDER_GROUP, getAction(TOGGLE_STEP_FILTERS));
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextProvider(fProvider);
        disposeActions();
		fProvider.dispose();
	    Viewer viewer = getViewer();
		if (viewer != null) {
			viewer.removeSelectionChangedListener(this);
			if (viewer instanceof AsynchronousTreeViewer) {
				AsynchronousTreeViewer asyncTreeViewer = (AsynchronousTreeViewer) viewer;
				asyncTreeViewer.dispose();
			}
		}
		IWorkbenchPage page = getSite().getPage();
		page.removePartListener((IPartListener2) this);
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		window.removePerspectiveListener(this);
		window.removePageListener(this);
		
		super.dispose();
	}
		
	private void disposeActions() {
        PropertyDialogAction properties = (PropertyDialogAction) getAction("Properties"); //$NON-NLS-1$
        properties.dispose();
        
        disposeCommandAction(TERMINATE);
        disposeCommandAction(DISCONNECT);
        disposeCommandAction(SUSPEND);
        disposeCommandAction(RESUME);
        disposeCommandAction(STEP_RETURN);
        disposeCommandAction(STEP_OVER);
        disposeCommandAction(STEP_INTO);
        disposeCommandAction(DROP_TO_FRAME);
        disposeCommandAction(TERMINATE_AND_REMOVE);
        disposeCommandAction(TERMINATE_AND_RELAUNCH);
        disposeCommandAction(TERMINATE_ALL);
    }

    /**
	 * The selection has changed in the viewer. Show the
	 * associated source code if it is a stack frame.
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		fProvider.activate(event.getSelection());
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		ISelection selection= event.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection ss= (IStructuredSelection)selection;
		Object o= ss.getFirstElement();
		if (o == null || o instanceof IStackFrame) {
			return;
		} 
		StructuredViewer viewer = (StructuredViewer) getViewer();
		viewer.refresh(o);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		setActive(page.findView(getSite().getId()) != null);
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		setActive(page.findView(getSite().getId()) != null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (getSite().getPage().equals(page)) {
			setActive(true);
			updateObjects();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getPresentation(java.lang.String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		return ((DelegatingModelPresentation)fPresentation).getPresentation(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		
		menu.add(new Separator(IDebugUIConstants.EMPTY_EDIT_GROUP));
		menu.add(new Separator(IDebugUIConstants.EDIT_GROUP));
		menu.add(getAction(FIND_ACTION));
		menu.add(new Separator(IDebugUIConstants.EMPTY_STEP_GROUP));
		menu.add(new Separator(IDebugUIConstants.STEP_GROUP));
		menu.add(new GroupMarker(IDebugUIConstants.STEP_INTO_GROUP));
		menu.add(new GroupMarker(IDebugUIConstants.STEP_OVER_GROUP));
		menu.add(new GroupMarker(IDebugUIConstants.STEP_RETURN_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_LAUNCH_GROUP));
		menu.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		IStructuredSelection selection = (IStructuredSelection) getSite().getSelectionProvider().getSelection();
		updateAndAdd(menu, fEditConfigAction, selection);
		updateAndAdd(menu, fAddToFavoritesAction, selection);
		updateAndAdd(menu, fEditSourceAction, selection);
		updateAndAdd(menu, fLookupAction, selection);
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.PROPERTY_GROUP));
		PropertyDialogAction action = (PropertyDialogAction)getAction("Properties"); //$NON-NLS-1$
		action.setEnabled(action.isApplicableForSelection());
		menu.add(action);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        
        menu.appendToGroup(IDebugUIConstants.LAUNCH_GROUP, getAction(TERMINATE_AND_REMOVE));
        menu.appendToGroup(IDebugUIConstants.LAUNCH_GROUP, getAction(TERMINATE_ALL));
		
        menu.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(RESUME));
        menu.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(SUSPEND));
        menu.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(TERMINATE));
        menu.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(TERMINATE_AND_RELAUNCH));
        menu.appendToGroup(IDebugUIConstants.THREAD_GROUP, getAction(DISCONNECT));
        
        menu.appendToGroup(IDebugUIConstants.STEP_INTO_GROUP, getAction(STEP_INTO));
        menu.appendToGroup(IDebugUIConstants.STEP_OVER_GROUP, getAction(STEP_OVER));
        menu.appendToGroup(IDebugUIConstants.STEP_RETURN_GROUP, getAction(STEP_RETURN));
        
        menu.appendToGroup(IDebugUIConstants.EMPTY_STEP_GROUP, getAction(DROP_TO_FRAME));
        
        menu.appendToGroup(IDebugUIConstants.RENDER_GROUP, getAction(TOGGLE_STEP_FILTERS));
    }
	
	/**
	 * Updates the enabled state of the given action based on the selection
	 * and adds to the menu if enabled.
	 * 
	 * @param menu menu to add the action to
	 * @param action action to add if enabled
	 * @param selection selection to update enabled state for
	 */
	private void updateAndAdd(IMenuManager menu, SelectionListenerAction action, IStructuredSelection selection) {
		action.selectionChanged(selection);
		if (action.isEnabled()) {
			menu.add(action);
		}		
	}
		
	/**
	 * Sets whether this view is in the active page of a
	 * perspective. Since a page can have more than one
	 * perspective, this view only show's source when in
	 * the active perspective/page.
	 * 
	 * @param active whether this view is in the active page of a
	 * perspective
	 */
	protected void setActive(boolean active) {
		fIsActive = active;
	} 

	/**
	 * Returns whether this view is in the active page of
	 * the active perspective and has been fully created.
	 * 
	 * @return whether this view is in the active page of
	 * the active perspective and has been fully created.
	 */
	protected boolean isActive() {
		return fIsActive && getViewer() != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInTarget#show(org.eclipse.ui.part.ShowInContext)
	 */
	public boolean show(ShowInContext context) {
		ISelection selection = context.getSelection();
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() == 1) {
					Object obj = ss.getFirstElement();
					if (obj instanceof IDebugTarget || obj instanceof IProcess) {
						getViewer().setSelection(selection, true);
						return true;
					}
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInSource#getShowInContext()
	 */
	public ShowInContext getShowInContext() {
		if (isActive()) { 
			IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
			if (selection.size() == 1) { 
				Object object = selection.getFirstElement();
				if (object instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) object;
					IShowInSource show = (IShowInSource) adaptable.getAdapter(IShowInSource.class);
					if (show != null) {
						return show.getShowInContext();
					}
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInTargetList#getShowInTargetIds()
	 */
	public String[] getShowInTargetIds() {
		if (isActive()) { 
			IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
			if (selection.size() == 1) { 
				Object object = selection.getFirstElement();
				if (object instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) object;
					IShowInTargetList show = (IShowInTargetList) adaptable.getAdapter(IShowInTargetList.class);
					if (show != null) {
						return show.getShowInTargetIds();
					}
				}
			}
		}
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part == this) {
			setActive(true);
// TODO: Workaround for Bug #63332. Reexamine after M9.
//			updateContextListener();
			// When the launch view becomes visible, turn on the
			// debug action set. Note that the workbench will handle the
			// case where the user really doesn't want the action set
			// enabled - showActionSet(String) will do nothing for an
			// action set that's been manually disabled.
			getSite().getPage().showActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		getViewer().refresh();
	}
	
	

}
