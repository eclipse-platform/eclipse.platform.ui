/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.AddToFavoritesAction;
import org.eclipse.debug.internal.ui.actions.EditLaunchConfigurationAction;
import org.eclipse.debug.internal.ui.actions.FindElementAction;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.debug.internal.ui.contexts.IDebugContextListener;
import org.eclipse.debug.internal.ui.contexts.IDebugContextProvider;
import org.eclipse.debug.internal.ui.sourcelookup.EditSourceLookupPathAction;
import org.eclipse.debug.internal.ui.sourcelookup.LookupSourceAction;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.PresentationContext;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
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

public class LaunchView extends AbstractDebugView implements ISelectionChangedListener, IPerspectiveListener2, IPageListener, IPropertyChangeListener, IShowInTarget, IShowInSource, IShowInTargetList, IPartListener2 {
	
	public static final String ID_CONTEXT_ACTIVITY_BINDINGS = "contextActivityBindings"; //$NON-NLS-1$
			
	/**
	 * Whether this view is in the active page of a perspective.
	 */
	private boolean fIsActive = true; 	
		
	/**
	 * Editor presentation or <code>null</code> if none
	 */
	private IDebugEditorPresentation fEditorPresentation = null;
	
	private EditLaunchConfigurationAction fEditConfigAction = null;
	private AddToFavoritesAction fAddToFavoritesAction = null;
	private EditSourceLookupPathAction fEditSourceAction = null;
	private LookupSourceAction fLookupAction = null;

	class ContextProvider implements IDebugContextProvider {
		/**
		 * Context listeners
		 */
		private ListenerList fListeners = new ListenerList();
		
		private ISelection fContext = null;
		
		protected void dispose() { 
			fContext = null;
			fListeners.clear();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.contexts.IDebugContextProvider#getPart()
		 */
		public IWorkbenchPart getPart() {
			return LaunchView.this;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.contexts.IDebugContextProvider#addDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener)
		 */
		public void addDebugContextListener(IDebugContextListener listener) {
			fListeners.add(listener);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.contexts.IDebugContextProvider#removeDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener)
		 */
		public void removeDebugContextListener(IDebugContextListener listener) {
			fListeners.remove(listener);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.contexts.IDebugContextProvider#getActiveContext()
		 */
		public synchronized ISelection getActiveContext() {
			return fContext;
		}	
		
		protected synchronized void activate(ISelection selection) {
			fContext = selection;
			Object[] listeners = fListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final IDebugContextListener listener = (IDebugContextListener) listeners[i];
				Platform.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.contextActivated(fContext, ContextProvider.this.getPart());
					}
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
				});
				
			}
		}
		
		protected synchronized void possibleContextChange(Object element) {
			if (fContext instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) fContext;
				if (ss.size() == 1 && ss.getFirstElement().equals(element)) {
					Object[] listeners = fListeners.getListeners();
					for (int i = 0; i < listeners.length; i++) {
						final IDebugContextListener listener = (IDebugContextListener) listeners[i];
						Platform.run(new ISafeRunnable() {
							public void run() throws Exception {
								listener.contextChanged(fContext, ContextProvider.this.getPart());
							}
							public void handleException(Throwable exception) {
								DebugUIPlugin.log(exception);
							}
						});
						
					}					
				}
			}
		}
		
	}
	
	/**
	 * Context provider
	 */
	private ContextProvider fProvider = new ContextProvider();
	
	/**
	 * Context manager which automatically opens and closes views
	 * based on debug contexts.
	 */
	private LaunchViewContextListener fContextListener;
	
	/**
	 * Creates a launch view and an instruction pointer marker for the view
	 */
	public LaunchView() {
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}
	
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
		setAction(FIND_ACTION, new FindElementAction((AsynchronousTreeViewer) getViewer()));
				
		// submit an async exec to update the selection once the
		// view has been created - i.e. auto-expand and select the
		// suspended thread on creation. (Done here, because the
		// viewer needs to be set).
		Runnable r = new Runnable() {
			public void run() {
				initializeSelection();
			}
		};
		asyncExec(r);		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		AsynchronousTreeViewer viewer = new LaunchViewer(parent, this);
		viewer.setContext(new PresentationContext(this));
        viewer.setInput(DebugPlugin.getDefault().getLaunchManager());
        
        viewer.addSelectionChangedListener(this);
        viewer.getControl().addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent event) {
        		if (event.character == SWT.DEL && event.stateMask == 0) {
        			handleDeleteKeyPressed();
        		}
        	}
        });
        final DelegatingModelPresentation presentation = new DelegatingModelPresentation();
        fEditorPresentation = presentation;
        // add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(viewer);
		viewer.setInput(DebugPlugin.getDefault().getLaunchManager());
		//setEventHandler(new LaunchViewEventHandler(this));
		DebugContextManager.getDefault().addDebugContextProvider(fProvider);
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
	
	/**
	 * Select the first stack frame in a suspended thread,
	 * if any.
	 */
	private void initializeSelection() {
		if (!isAvailable()) {
			return;
		}

		// traverse debug model in non UI thread
		Job initJob = new Job(DebugUIViewsMessages.LaunchView_2) { 
			/* (non-Javadoc)
			 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				ILaunchManager launchManager = (ILaunchManager) getViewer().getInput();
				ILaunch[] launches = launchManager.getLaunches();
				// forces the delegates to update enablement
				// TODO: would it be better to contribute toolbar/context actions in code?
				if (launches.length == 0) {
					Runnable runnable = new Runnable() {
						public void run() {
							getViewer().setSelection(new StructuredSelection());
						}
					};
					asyncExec(runnable);
				}
				return Status.OK_STATUS;
			}
		};
		initJob.schedule();
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
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		DebugContextManager.getDefault().removeDebugContextProvider(fProvider);
		fProvider.dispose();
	    Viewer viewer = getViewer();
		if (viewer != null) {
			viewer.removeSelectionChangedListener(this);
			if (viewer instanceof AsynchronousTreeViewer) {
				AsynchronousTreeViewer asyncTreeViewer = (AsynchronousTreeViewer) viewer;
				asyncTreeViewer.dispose();
			}
		}
		if (fContextListener != null) {
			fContextListener.dispose();
		}
		IWorkbenchPage page = getSite().getPage();
		page.removePartListener((IPartListener2) this);
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		window.removePerspectiveListener(this);
		window.removePageListener(this);
		
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
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
		
//		 TODO: replace view conetxt stuff based on debug context
		if (isActive()) {
			Object element = ((IStructuredSelection)event.getSelection()).getFirstElement();
		    fContextListener.updateForSelection(element);
		}
		
	}
	
	protected void possibleContextChange(Object element) {
		fProvider.possibleContextChange(element);
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
		fContextListener.clearLastEnabledContexts();
		if (isActive()) {
			fContextListener.updateForSelection(((IStructuredSelection) getViewer().getSelection()).getFirstElement());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		setActive(page.findView(getSite().getId()) != null);
		if (fContextListener != null) {
			fContextListener.perspectiveChanged(changeId);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
		if (fContextListener != null) {
			fContextListener.perspectiveChanged(partRef, changeId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (getSite().getPage().equals(page)) {
			setActive(true);
			updateObjects();
			if (fContextListener != null) {
				fContextListener.loadTrackViews();
			}
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
		return ((DelegatingModelPresentation)fEditorPresentation).getPresentation(id);
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
	}	
	
	/**
	 * Updates the enablement of the given action based on the selection
	 * and addes to the menu iff enabled.
	 * 
	 * @param menu menu to add the action to
	 * @param action action to add if enabled
	 * @param selection selection to update enablement
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
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES)) {
			fContextListener.reloadAutoManagePerspectives(((IStructuredSelection) getViewer().getSelection()).getFirstElement());
		} else if (property.equals(LaunchViewContextListener.PREF_OPENED_VIEWS) && fContextListener != null) {
			fContextListener.loadOpenedViews();
		} else if (property.equals(LaunchViewContextListener.PREF_VIEWS_TO_NOT_OPEN) && fContextListener != null) {
			fContextListener.reloadViewsToNotOpen(((IStructuredSelection) getViewer().getSelection()).getFirstElement());
		} else if (property.equals(IInternalDebugUIConstants.PREF_TRACK_VIEWS) && fContextListener != null) {
			fContextListener.loadTrackViews();
		}
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
		// TODO: fix this
		if (isActive()) { 
			IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
			if (!selection.isEmpty()) { 
				Object sourceElement = null;
//				if (fResult != null) {
//					sourceElement = fResult.getSourceElement();
//				}
				if (sourceElement instanceof IAdaptable) {
					if (((IAdaptable)sourceElement).getAdapter(IResource.class) != null) {
						return new ShowInContext(null, new StructuredSelection(sourceElement));
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
		return new String[] { IPageLayout.ID_RES_NAV };
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
		IWorkbenchPart part = partRef.getPart(false);
		if (part == this) {
            fContextListener= new LaunchViewContextListener(LaunchView.this);
		}
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
