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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.InstructionPointerManager;
import org.eclipse.debug.internal.ui.actions.AddToFavoritesAction;
import org.eclipse.debug.internal.ui.actions.EditLaunchConfigurationAction;
import org.eclipse.debug.internal.ui.sourcelookup.EditSourceLookupPathAction;
import org.eclipse.debug.internal.ui.sourcelookup.LookupSourceAction;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupResult;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.DebugViewDecoratingLabelProvider;
import org.eclipse.debug.internal.ui.views.DebugViewInterimLabelProvider;
import org.eclipse.debug.internal.ui.views.DebugViewLabelDecorator;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
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
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;

public class LaunchView extends AbstractDebugEventHandlerView implements ISelectionChangedListener, IPerspectiveListener2, IPageListener, IPropertyChangeListener, IResourceChangeListener, IShowInTarget, IShowInSource, IShowInTargetList, IPartListener2 {
	
	public static final String ID_CONTEXT_ACTIVITY_BINDINGS = "contextActivityBindings"; //$NON-NLS-1$
			
	/**s
	 * Cache of the stack frame that source was displayed
	 * for.
	 */
	private IStackFrame fStackFrame = null;
	
	/**
	 * Result of last source lookup
	 */
	private ISourceLookupResult fResult = null;
		
	/**
	 * Whether this view is in the active page of a perspective.
	 */
	private boolean fIsActive = true; 	
	
	/**
	 * Resource delta visitor
	 */
	private IResourceDeltaVisitor fVisitor = null;
	
	/**
	 * Editor presentation or <code>null</code> if none
	 */
	private IDebugEditorPresentation fEditorPresentation = null;
	
	private EditLaunchConfigurationAction fEditConfigAction = null;
	private AddToFavoritesAction fAddToFavoritesAction = null;
	private EditSourceLookupPathAction fEditSourceAction = null;
	private LookupSourceAction fLookupAction = null;
	
	/**
	 * Progress service or <code>null</code>
	 */
	private IWorkbenchSiteProgressService fProgressService = null;

	/**
	 * Context manager which automatically opens and closes views
	 * based on debug contexts.
	 */
	private LaunchViewContextListener fContextListener;
	
	/**
	 * A job to perform source lookup on the currently selected stack frame.
	 */
	class SourceLookupJob extends Job {

		/**
		 * Constructs a new source lookup job.
		 */
		public SourceLookupJob() {
			super(DebugUIViewsMessages.LaunchView_0); 
			setPriority(Job.INTERACTIVE);
			setSystem(true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				IStackFrame frame = getStackFrame();
				ISourceLookupResult result = null;
				if (frame != null) {
					result = DebugUITools.lookupSource(frame, null);
				}
				setSourceLookupResult(result);
				scheduleSourceDisplay();
			}
			return Status.OK_STATUS;
		}
		
	}
	
	/**
	 * Source lookup job.
	 */
	private Job fSourceLookupJob = null;
	
	class SourceDisplayJob extends UIJob {

		/**
		 * Constructs a new source display job
		 */
		public SourceDisplayJob() {
			super(DebugUIViewsMessages.LaunchView_1); 
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				ISourceLookupResult result = getSourceLookupResult();
				if (result != null) {
					DebugUITools.displaySource(result, getSite().getPage());
				}
			}
			return Status.OK_STATUS;
		}
		
	}
    
	/**
	 * Label provider for the launch view which renders pending launches
	 * with italic font.
	 */
    private class LaunchViewLabelProvider extends DebugViewDecoratingLabelProvider {
    	
    	// The cached italic font used for pending launches
    	private Font fItalicFont= null;
    	
        public LaunchViewLabelProvider(StructuredViewer viewer, IDebugModelPresentation presentation) {
            super(viewer, new DebugViewInterimLabelProvider(presentation), new DebugViewLabelDecorator(presentation));
        }
        
        public Font getFont(Object element) {
            if (element instanceof DebugUIPlugin.PendingLaunch) {
            	if (fItalicFont == null) {
	                Control control = getViewer().getControl();
	                Font originalFont = control.getFont();
	                FontData fontData[] = originalFont.getFontData();
	                // Add the italic attribute
	                for (int i = 0; i < fontData.length; i++) {
	                    fontData[i].setStyle(fontData[i].getStyle() | SWT.ITALIC);
	                }
	                fItalicFont= new Font(control.getDisplay(), fontData);
            	}
            	return fItalicFont;
            }
            return super.getFont(element);
        }

		public void dispose() {
			if (fItalicFont != null) {
				fItalicFont.dispose();
			}
			super.dispose();
		}
    }
	
	/**
	 * Job used for source display.
	 */
	private Job fSourceDisplayJob = null;
	
	/**
	 * Creates a launch view and an instruction pointer marker for the view
	 */
	public LaunchView() {
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
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
		LaunchViewer lv = new LaunchViewer(parent);
		lv.addPostSelectionChangedListener(this);
		lv.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					handleDeleteKeyPressed();
				}
			}
		});
		lv.setContentProvider(new DebugViewContentProvider(lv, getSite()));
		final DelegatingModelPresentation presentation = new DelegatingModelPresentation();
		lv.setLabelProvider(new LaunchViewLabelProvider(lv, presentation));
		fEditorPresentation = presentation;
		// add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(lv);
		lv.setInput(DebugPlugin.getDefault().getLaunchManager());
		setEventHandler(new LaunchViewEventHandler(this));
		return lv;
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
			if (!MessageDialog.openQuestion(getSite().getShell(), DebugUIViewsMessages.LaunchView_Terminate_and_Remove_1, DebugUIViewsMessages.LaunchView_Terminate_and_remove_selected__2)) { // 
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
				DebugUIPlugin.errorDialog(window.getShell(), DebugUIViewsMessages.LaunchView_Terminate_and_Remove_4, DebugUIViewsMessages.LaunchView_Terminate_and_remove_failed_5, status); // 
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
		TreeViewer tv = (TreeViewer)getViewer();
		tv.expandToLevel(2);
		final Object[] elements = tv.getExpandedElements();
		// traverse debug model in non UI thread
		Job initJob = new Job(DebugUIViewsMessages.LaunchView_2) { 
			/* (non-Javadoc)
			 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				for (int i = 0; i < elements.length; i++) {
					if (elements[i] instanceof ILaunch) {
						final IStackFrame frame = findFrame((ILaunch)elements[i]);
						if (frame != null) {
							Runnable runnable = new Runnable() {
								public void run() {
									autoExpand(frame, true);
								}
							};
							asyncExec(runnable);
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		initJob.schedule();
	}
	
	/**
	 * Returns the first stack frame in the first suspended
	 * thread of the given launch, or <code>null</code> if
	 * none. Prioritizes selection based on which thread is
	 * suspended at a breakpoint.
	 * 
	 * @param launch a launch in this view
	 * @return stack frame or <code>null</code>
	 */
	private IStackFrame findFrame(ILaunch launch) {
		IDebugTarget target = launch.getDebugTarget();
		if (target != null) {
			try {
				IThread[] threads = target.getThreads();
				IThread suspended = null;
				for (int i = 0; i < threads.length; i++) {
					if (threads[i].isSuspended()) {
						if (threads[i].getBreakpoints().length > 0) {
							return threads[i].getTopStackFrame();
						} else if (suspended == null) {
							suspended = threads[i];
						}
					}
				}
				if (suspended != null) {
					return suspended.getTopStackFrame();
				}
			} catch (DebugException e) {
			}
		}
		return null;
	}

	private void commonInit(IViewSite site) {
		site.getPage().addPartListener((IPartListener2) this);
		site.getWorkbenchWindow().addPageListener(this);
		site.getWorkbenchWindow().addPerspectiveListener(this);
		fProgressService = (IWorkbenchSiteProgressService) site.getAdapter(IWorkbenchSiteProgressService.class);
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
	    RemoteTreeViewer viewer = (RemoteTreeViewer) getViewer();
		if (viewer != null) {
			viewer.removeSelectionChangedListener(this);
			viewer.cancelJobs();
		}
		if (fContextListener != null) {
			fContextListener.dispose();
		}
		IWorkbenchPage page = getSite().getPage();
		page.removePartListener((IPartListener2) this);
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		window.removePerspectiveListener(this);
		window.removePageListener(this);
		
		cleanup();
		
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	/**
	 * Disposes of cached information
	 */
	protected void cleanup() {
		setSourceLookupResult(null);
		setStackFrame(null);
	}
	
	private void setSourceLookupResult(ISourceLookupResult result) {
	    fResult = result;
	}
	
	private ISourceLookupResult getSourceLookupResult() {
	    return fResult;
	}
	
	/**
	 * The selection has changed in the viewer. Show the
	 * associated source code if it is a stack frame.
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		clearStatusLine();
		updateObjects();
		showEditorForCurrentSelection();
		if (isActive()) {
		    fContextListener.updateForSelection(((IStructuredSelection) getViewer().getSelection()).getFirstElement());
		}
	}

	/**
	 * Lookup source element for current stack frame again.
	 */
	public void redoSourceLookup() {
		setStackFrame(null);
		selectionChanged(null);
	}

	/**
	 * Notifies this view to clean up for the given launches (they've been terminated,
	 * removed, etc.). Remove all context submissions associated with these launches.
	 * Clear the cache of the last stack frame that source was displayed for
	 * if that launch is terminated.
	 * 
	 * @param launches the terminated launches
	 */
	protected void cleanupLaunches(ILaunch[] launches) {
		fContextListener.launchesTerminated(launches);
		IStackFrame frame = getStackFrame();
		if (frame != null) {
			ILaunch frameLaunch= frame.getLaunch();
			for (int i = 0; i < launches.length; i++) {
				ILaunch launch = launches[i];
				if (launch.equals(frameLaunch)) {
					setStackFrame(null);
					setSourceLookupResult(null);
				}
			}
		}
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
		TreeViewer tViewer= (TreeViewer)getViewer();
		boolean expanded= tViewer.getExpandedState(o);
		tViewer.setExpandedState(o, !expanded);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		setActive(page.findView(getSite().getId()) != null);
		updateObjects();
		showEditorForCurrentSelection();
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
			showEditorForCurrentSelection();
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
	
	/**
	 * Opens an editor for the current selection if it is a stack frame.
	 * Otherwise, nothing will happen.
	 */
	protected void showEditorForCurrentSelection() {
		// ensure this view is visible in the active page
		if (!isActive()) {
			return;
		}		
		ISelection selection= getViewer().getSelection();
		Object obj= null;
		if (selection instanceof IStructuredSelection) {
			obj= ((IStructuredSelection) selection).getFirstElement();
		}
		if (!(obj instanceof IStackFrame)) {
			return;
		}
		openEditorForStackFrame((IStackFrame) obj);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getPresentation(java.lang.String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		return ((DelegatingModelPresentation)fEditorPresentation).getPresentation(id);
	}
	
	/**
	 * Get the active window and open/bring to the front an editor on the stack
	 * frame. Selection is based on the line number OR the char start and end.
	 */
	protected void openEditorForStackFrame(IStackFrame stackFrame) {
		if (!stackFrame.isSuspended()) {
			return;
		}
		if (!stackFrame.equals(getStackFrame()) || (getEditorInput() == null || getEditorId() == null)) {
			setStackFrame(stackFrame);
			scheduleSourceLookup();
		} else {
			setStackFrame(stackFrame);
			SourceLookupResult result = (SourceLookupResult) fResult;
			if (result != null) {
				result.updateArtifact(stackFrame);
			}
			scheduleSourceDisplay();
		}
	}
	
	/**
	 * Schedules a source lookup job.
	 */
	private void scheduleSourceLookup() {
		if (fSourceLookupJob == null) {
			fSourceLookupJob = new SourceLookupJob();
		}
		setSourceLookupResult(null);
		schedule(fSourceLookupJob);
	}
	
	/**
	 * Schedules a source display job.
	 */
	private void scheduleSourceDisplay() {
		if (fSourceDisplayJob == null) {
			fSourceDisplayJob = new SourceDisplayJob();
		}
		schedule(fSourceDisplayJob);
	}	
	
	/**
	 * Schedules a job with this part's progress service, if available.
	 * 
	 * @param job job to schedule
	 */
	private void schedule(Job job) {
		if (fProgressService == null) {
			job.schedule();
		} else {
			fProgressService.schedule(job);
		}
	}

	private IEditorInput getEditorInput() {
		if (fResult != null) {
			return fResult.getEditorInput();
		}
		return null;
	}
	
	private String getEditorId() {
		if (fResult != null) {
			return fResult.getEditorId();
		}
		return null;
	}
	
	/**
	 * Deselects any source decorations associated with the given thread or
	 * debug target.
	 * 
	 * @param source thread or debug target
	 */
	public void clearSourceSelection(Object source) {		
		if (source instanceof IThread) {
			IThread thread = (IThread)source;
			DecorationManager.removeDecorations(thread);
			InstructionPointerManager.getDefault().removeAnnotations(thread);
		} else if (source instanceof IDebugTarget) {
			IDebugTarget target = (IDebugTarget)source;
			DecorationManager.removeDecorations(target);
			InstructionPointerManager.getDefault().removeAnnotations(target);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		
		menu.add(new Separator(IDebugUIConstants.EMPTY_EDIT_GROUP));
		menu.add(new Separator(IDebugUIConstants.EDIT_GROUP));
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
	 * Auto-expand and select the given element - must be called in UI thread.
	 * This is used to implement auto-expansion-and-select on a SUSPEND event.
	 * 
	 * @param element the element to expand, cannot be <code>null</code>
	 * @param selectNeeded whether the element should be selected
	 */
	public void autoExpand(Object element, boolean selectNeeded) {
		LaunchViewer launchViewer = (LaunchViewer)getViewer();
        launchViewer.deferExpansion(element);
		if (selectNeeded) {
			IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
			// if a frame is selected in a different thread, do not update selection
			Iterator iterator = selection.iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof IStackFrame) {
					if (element instanceof IStackFrame) {
						IThread currThread = ((IStackFrame)object).getThread();
						if (!currThread.equals(((IStackFrame)element).getThread())) {
							// a frame in a different thread is selected, don't change
							return;
						}
					} else {
						// a frame is selected and the new selection is not a frame
						// do not change the selection
						return;
					}
				}
			}
			launchViewer.deferSelection(new StructuredSelection(element));
		}
	}
	
	/**
	 * Returns the last stack frame that source was retrieved
	 * for. Used to avoid source lookups for the same stack
	 * frame when stepping.
	 * 
	 * @return stack frame, or <code>null</code>
	 */
	protected IStackFrame getStackFrame() {
		return fStackFrame;
	}	
	
	/**
	 * Sets the last stack frame that source was retrieved
	 * for. Used to avoid source lookups for the same stack
	 * frame when stepping. Setting the stack frame to <code>null</code>
	 * effectively forces a source lookup.
	 * 
	 * @param frame The stack frame or <code>null</code>
	 */
	protected void setStackFrame(IStackFrame frame) {
		fStackFrame= frame;
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

	/**
	 * Visitor for handling resource deltas. When a project is closed, we must clear
	 * the cache of editor input/stack frame, etc., as the elements can become invalid.
	 */
	class LaunchViewVisitor implements IResourceDeltaVisitor {
		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null) {
				return false;
			}
			IResource resource = delta.getResource();
			if (0 != (delta.getFlags() & IResourceDelta.OPEN)) {
				if (resource instanceof IProject) {
					IProject project = (IProject)resource;
					if (!project.isOpen()) {
						// clear
					    cleanup();
					}
				}
				return false;
			}
			return resource instanceof IWorkspaceRoot;
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				delta.accept(getVisitor());
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}		
	}

	/**
	 * Returns the resource delta visitor for this view,
	 * creating if required.
	 * 
	 * @return resource delta visitor
	 */
	protected IResourceDeltaVisitor getVisitor() {
		if (fVisitor == null) {
			fVisitor = new LaunchViewVisitor();
		}
		return fVisitor;
	}
	
	/**
	 * When this view becomes visible, selects the last stack frame whose
	 * location was revealed.
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		IStructuredSelection selection= (IStructuredSelection) getViewer().getSelection(); 
		if (selection.isEmpty() || !selection.getFirstElement().equals(getStackFrame())) {
			initializeSelection();
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
		if (isActive()) { 
			IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
			if (!selection.isEmpty()) { 
				Object sourceElement = null;
				if (fResult != null) {
					sourceElement = fResult.getSourceElement();
				}
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
}
