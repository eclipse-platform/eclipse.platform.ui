/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.InstructionPointerManager;
import org.eclipse.debug.internal.ui.actions.AddToFavoritesAction;
import org.eclipse.debug.internal.ui.actions.EditLaunchConfigurationAction;
import org.eclipse.debug.internal.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.debug.internal.ui.sourcelookup.EditSourceLookupPathAction;
import org.eclipse.debug.internal.ui.sourcelookup.LookupSourceAction;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.DebugViewDecoratingLabelProvider;
import org.eclipse.debug.internal.ui.views.DebugViewInterimLabelProvider;
import org.eclipse.debug.internal.ui.views.DebugViewLabelDecorator;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IReusableEditor;
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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class LaunchView extends AbstractDebugEventHandlerView implements ISelectionChangedListener, IPerspectiveListener2, IPageListener, IPropertyChangeListener, IResourceChangeListener, IShowInTarget, IShowInSource, IShowInTargetList, IPartListener2 {
	
	public static final String ID_CONTEXT_ACTIVITY_BINDINGS = "contextActivityBindings"; //$NON-NLS-1$

	private boolean fShowingEditor = false;
			
	/**s
	 * Cache of the stack frame that source was displayed
	 * for.
	 */
	private IStackFrame fStackFrame = null;
	
	/**
	 * Cache of the editor input used to display source
	 */
	private IEditorInput fEditorInput = null;
	
	/**
	 * Cache of the editor id used to display source
	 */
	private String fEditorId = null;
	
	/**
	 * Whether this view is in the active page of a perspective.
	 */
	private boolean fIsActive = true; 	
	
	/**
	 * Editor to reuse
	 */
	private IEditorPart fEditor = null;
	
	/**
	 * The source element corresponding to the selected stack frame
	 */
	private Object fSourceElement = null;
	
	/**
	 * The restored editor index of the editor to re-use
	 */
	private int fEditorIndex = -1;
	
	/**
	 * Whether to re-use editors
	 */
	private boolean fReuseEditor = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_REUSE_EDITOR);
	
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
	 * Context manager which automatically opens and closes views
	 * based on debug contexts.
	 */
	private LaunchViewContextListener fContextListener;
	
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
		setAction("Properties", new PropertyDialogAction(getSite().getWorkbenchWindow().getShell(), getSite().getSelectionProvider())); //$NON-NLS-1$
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
		lv.setContentProvider(new DeferredContentProvider(lv, getSite()));
		final DelegatingModelPresentation presentation = new DelegatingModelPresentation();
		DebugViewDecoratingLabelProvider labelProvider= new DebugViewDecoratingLabelProvider(lv, new DebugViewInterimLabelProvider(presentation), new DebugViewLabelDecorator(presentation));
		lv.setLabelProvider(labelProvider);
		fEditorPresentation = presentation;
		// add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(lv);
		lv.setInput(DebugPlugin.getDefault().getLaunchManager());
		setEventHandler(new LaunchViewEventHandler(this));
		
		// determine if active
		setActive(getSite().getPage().findView(getSite().getId()) != null);
		
		return lv;
	}
	
	/**
	 * Returns the label decorator used by this view.
	 * 
	 * @return this view's label decorator
	 */
	public DebugViewLabelDecorator getLabelDecorator() {
		return (DebugViewLabelDecorator) ((DebugViewDecoratingLabelProvider) ((LaunchViewer) getViewer()).getLabelProvider()).getLabelDecorator();
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
			if (!MessageDialog.openQuestion(getSite().getShell(), DebugUIViewsMessages.getString("LaunchView.Terminate_and_Remove_1"), DebugUIViewsMessages.getString("LaunchView.Terminate_and_remove_selected__2"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		MultiStatus status= new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, DebugUIViewsMessages.getString("LaunchView.Exceptions_occurred_attempting_to_terminate_and_remove_3"), null); //$NON-NLS-1$
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
				DebugUIPlugin.errorDialog(window.getShell(), DebugUIViewsMessages.getString("LaunchView.Terminate_and_Remove_4"), DebugUIViewsMessages.getString("LaunchView.Terminate_and_remove_failed_5"), status); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				DebugUIPlugin.log(status);
			}
		}
	}
	
	/**
	 * Terminates and removes the given element from the launch manager
	 */
	public static void terminateAndRemove(Object element) throws DebugException {
		if (!(element instanceof ITerminate)) {
			return;
		}
		ITerminate terminable= (ITerminate) element;
		if (!(terminable.canTerminate() || terminable.isTerminated())) {
			// Don't try to terminate or remove attached launches
			return;
		}
		try {
			if (!terminable.isTerminated()) {
				terminable.terminate();
			}
		} finally {
			remove(element);
		}
	}
	
	/**
	 * Removes the given element from the launch manager. Has no effect if the
	 * given element is not of type ILaunch, IDebugElement, or IProcess
	 */
	private static void remove(Object element) {
		ILaunch launch= null;
		if (element instanceof ILaunch) {
			launch= (ILaunch) element;
		} else if (element instanceof IDebugElement) {
			launch= ((IDebugElement) element).getLaunch();
		} else if (element instanceof IProcess) {
			launch= ((IProcess) element).getLaunch();
		} else {
			return;
		}
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		lManager.removeLaunch(launch);
	}
	
	/**
	 * Select the first stack frame in a suspended thread,
	 * if any.
	 */
	protected void initializeSelection() {
		if (!isAvailable()) {
			return;
		}
		TreeViewer tv = (TreeViewer)getViewer();
		tv.expandToLevel(2);
		Object[] elements = tv.getExpandedElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof ILaunch) {
				IStackFrame frame = findFrame((ILaunch)elements[i]);
				if (frame != null) {
					autoExpand(frame, true);
				}
			}
		}
	}
	
	/**
	 * Returns the first stack frame in the first suspended
	 * thread of the given launch, or <code>null</code> if
	 * none.
	 * 
	 * @param launch a launch in this view
	 * @return stack frame or <code>null</code>
	 */
	protected IStackFrame findFrame(ILaunch launch) {
		IDebugTarget target = launch.getDebugTarget();
		if (target != null) {
			try {
				IThread[] threads = target.getThreads();
				for (int i = 0; i < threads.length; i++) {
					if (threads[i].isSuspended()) {
						return threads[i].getTopStackFrame();
					}
				}
			} catch (DebugException e) {
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getPage().addPartListener((IPartListener2) this);
		site.getWorkbenchWindow().addPageListener(this);
		site.getWorkbenchWindow().addPerspectiveListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		site.getPage().addPartListener((IPartListener2) this);
		site.getWorkbenchWindow().addPageListener(this);
		site.getWorkbenchWindow().addPerspectiveListener(this);
		if (memento == null) {
			return;
		}
		if (fReuseEditor) {
			String index = memento.getString(IDebugUIConstants.PREF_REUSE_EDITOR);
			if (index != null) {
				try {
					fEditorIndex = Integer.parseInt(index);
				} catch (NumberFormatException e) {
					DebugUIPlugin.log(e);
				}
			}
		}
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
	    LaunchViewer viewer = (LaunchViewer) getViewer();
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
		setEditorId(null);
		setEditorInput(null);
		setStackFrame(null);
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
	 * removed, etc.). Wemove all context submissions associated with these launches.
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
			fContextListener.perspectiveChanged(page, changeId);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
		if (fContextListener != null) {
			fContextListener.perspectiveChanged(page, partRef, changeId);
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
	
	/**
	 * Translate to an editor input using the source presentation
	 * provided by the source locator, or the default debug model
     * presentation.
     */
	private void lookupEditorInput(IStackFrame stackFrame) {
		setEditorId(null);
		setEditorInput(null);
		setSourceElement(null);
		Object sourceElement= null;
		ILaunch launch= stackFrame.getLaunch();
		if (launch == null) {
			return;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null) {
			return;
		}
		sourceElement= locator.getSourceElement(stackFrame);
		if (sourceElement == null) {
			if (locator instanceof AbstractSourceLookupDirector)
				commonSourceNotFound(stackFrame);
			else
				sourceNotFound(stackFrame);
			return;
		}
		ISourcePresentation presentation= null;
		if (locator instanceof ISourcePresentation) {
			presentation= (ISourcePresentation) locator;
		} else {
			presentation= getPresentation(stackFrame.getModelIdentifier());
		}
		IEditorInput editorInput= null;
		String editorId= null;
		if (presentation != null) {
			editorInput= presentation.getEditorInput(sourceElement);
		}
		if (editorInput != null) {
			editorId= presentation.getEditorId(editorInput, sourceElement);
		}
		setEditorInput(editorInput);
		setEditorId(editorId);
		setSourceElement(sourceElement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugView#getPresentation(java.lang.String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		return ((DelegatingModelPresentation)fEditorPresentation).getPresentation(id);
	}
	
	/**
	 * Sets editor id and input for the "source not found" editor.
	 */
	private void sourceNotFound(IStackFrame frame) {
		setEditorInput(new SourceNotFoundEditorInput(frame));
		setEditorId(IInternalDebugUIConstants.ID_SOURCE_NOT_FOUND_EDITOR);
	}

	/**
	 * Sets editor id and input for the "common source not found" editor.
	 */
	private void commonSourceNotFound(IStackFrame frame) {
		setEditorInput(new CommonSourceNotFoundEditorInput(frame));
		setEditorId(IInternalDebugUIConstants.ID_COMMON_SOURCE_NOT_FOUND_EDITOR);
	}

	/**
	 * Get the active window and open/bring to the front an editor on the stack
	 * frame. Selection is based on the line number OR the char start and end.
	 */
	protected void openEditorForStackFrame(IStackFrame stackFrame) {
		if (fShowingEditor) {
			return;
		}
		try {
			fShowingEditor = true;

			if (!stackFrame.isSuspended()) {
				return;
			}

			if (stackFrame.equals(getStackFrame())) {
				if (getEditorInput() == null || getEditorId() == null) {
					lookupEditorInput(stackFrame);
				}
			} else {
				setStackFrame(stackFrame);
				lookupEditorInput(stackFrame);
			}
			if (getEditorInput() == null || getEditorId() == null) {
				return;
			}
			IEditorPart editor= openEditor();
			if (editor == null) {
				return;
			}
			// position and annotate editor for stack frame
			if (fEditorPresentation.addAnnotations(editor, stackFrame)) {
				Decoration decoration = new StandardDecoration(fEditorPresentation, editor, stackFrame.getThread());
				DecorationManager.addDecoration(decoration);				
			} else {
				// perform standard positioning and annotations
				ITextEditor textEditor = null;
				if (editor instanceof ITextEditor) {					
					textEditor = (ITextEditor)editor;
				} else {
					textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
				}
				if (textEditor != null) {
					positionEditor(textEditor, stackFrame);
					InstructionPointerManager.getDefault().addAnnotation(textEditor, stackFrame);
				}
			}
		} finally {
			fShowingEditor= false;
		}
	}
	
	/**
	 * Positions the text editor for the given stack frame
	 */
	private void positionEditor(ITextEditor editor, IStackFrame frame) {
		try {
			int charStart = frame.getCharStart();
			if (charStart > 0) {
				editor.selectAndReveal(charStart, 0);
				return;
			}
			int lineNumber = frame.getLineNumber();
			lineNumber--; // Document line numbers are 0-based. Debug line numbers are 1-based.
			IRegion region= getLineInformation(editor, lineNumber);
			if (region != null) {
				editor.selectAndReveal(region.getOffset(), 0);
			}
		} catch (DebugException e) {
		}
	}
	
	/**
	 * Returns the line information for the given line in the given editor
	 */
	private IRegion getLineInformation(ITextEditor editor, int lineNumber) {
		IDocumentProvider provider= editor.getDocumentProvider();
		IEditorInput input= editor.getEditorInput();
		try {
			provider.connect(input);
		} catch (CoreException e) {
			return null;
		}
		try {
			IDocument document= provider.getDocument(input);
			if (document != null)
				return document.getLineInformation(lineNumber);
		} catch (BadLocationException e) {
		} finally {
			provider.disconnect(input);
		}
		return null;
	}


	/**
	 * Opens the editor used to display the source for an element selected in
	 * this view and returns the editor that was opened or <code>null</code> if
	 * no editor could be opened.
	 */
	private IEditorPart openEditor() {
		IWorkbenchWindow window= getSite().getWorkbenchWindow();
		if (window == null) {
			return null;
		}
		IWorkbenchPage page= window.getActivePage();
		if (page == null) {
			return null;
		}

		if (fEditorIndex >= 0) {
			// first restoration of editor re-use
			IEditorReference[] refs = page.getEditorReferences();
			if (fEditorIndex < refs.length) {
				fEditor = refs[fEditorIndex].getEditor(false);
			}
			fEditorIndex = -1;
		}

		IEditorPart editor = null;
		IEditorInput input= getEditorInput();
		String id= getEditorId();
		if (input == null || id == null) {
			return null;
		}
		if (fReuseEditor) {
			editor = page.getActiveEditor();
			if (editor != null) {
				// The active editor is the one we want to reuse
				if (!editor.getEditorInput().equals(input)) {
					editor = null;
				}
			}
			if (editor == null) {
				// Try to find the editor we want to reuse and activate it
				IEditorReference[] refs = page.getEditorReferences();
				for (int i = 0; i < refs.length; i++) {
					IEditorPart refEditor= refs[i].getEditor(false);
					if (refEditor != null && input.equals(refEditor.getEditorInput())) {
						editor = refEditor;
						page.bringToTop(editor);
						break;
					}
				}
			}
			if (editor == null) {
				if (fEditor == null || fEditor.isDirty() || page.isEditorPinned(fEditor)) {
					editor = openEditor(page, input, id);
					fEditor = editor;
				} else if (fEditor instanceof IReusableEditor && fEditor.getSite().getId().equals(id)) {
					((IReusableEditor)fEditor).setInput(input);
					editor = fEditor;
					page.bringToTop(editor);
				} else {
					editor = openEditor(page, input, id);
					page.closeEditor(fEditor, false);
					fEditor = editor;
				}
			}
		} else {
			// Open a new editor
			editor = openEditor(page, input, id);
		}
		return editor;
	}
	
	/**
	 * Opens an editor in the workbench and returns the editor that was opened
	 * or <code>null</code> if an error occurred while attempting to open the
	 * editor.
	 */
	private IEditorPart openEditor(final IWorkbenchPage page, final IEditorInput input, final String id) {
		final IEditorPart[] editor = new IEditorPart[] {null};
		Runnable r = new Runnable() {
			public void run() {
				try {
					editor[0] = page.openEditor(input, id, false);
				} catch (PartInitException e) {
					DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), 
						DebugUIViewsMessages.getString("LaunchView.Error_1"),  //$NON-NLS-1$
						DebugUIViewsMessages.getString("LaunchView.Exception_occurred_opening_editor_for_debugger._2"),  //$NON-NLS-1$
						e);
				}					
			}
		}; 
		BusyIndicator.showWhile(DebugUIPlugin.getStandardDisplay(), r);
		return editor[0];
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
	 */
	public void autoExpand(Object element, boolean selectNeeded) {
	    boolean refresh = false;
		if (element instanceof IThread) {
			refresh = threadRefreshNeeded((IThread)element);
		}
		if (refresh) {
			//ensures that the child item exists in the viewer widget
			//set selection only works if the child exists
			getStructuredViewer().refresh(element);
		}
		LaunchViewer launchViewer = (LaunchViewer)getViewer();
        launchViewer.deferExpansion(element);
		if (selectNeeded) {
			launchViewer.setDeferredSelection(element);
		}
	}
	
	/**
	 * Returns whether the given thread needs to
	 * be refreshed in the tree.
	 * 
	 * The tree needs to be refreshed if the
	 * underlying model objects (IStackFrame) under the given thread
	 * differ from those currently displayed in the tree.
	 */
	protected boolean threadRefreshNeeded(IThread thread) {
		LaunchViewer viewer= (LaunchViewer)getStructuredViewer();
		ILaunch launch= thread.getLaunch();
		TreeItem[] launches= viewer.getTree().getItems();
		for (int i = 0; i < launches.length; i++) {
			if (launches[i].getData() == launch) {
				IDebugTarget target= thread.getDebugTarget();
				TreeItem[] targets= launches[i].getItems();
				for (int j = 0; j < targets.length; j++) {
					if (targets[j].getData() == target) {
						TreeItem[] threads= targets[j].getItems();
						for (int k = 0; k < threads.length; k++) {
							if (threads[k].getData() == thread) {
								IStackFrame[] frames= null;
								try {
									frames = thread.getStackFrames();
								} catch (DebugException exception) {
									return true;
								}
								TreeItem[] treeFrames= threads[k].getItems();
								if (frames.length != treeFrames.length) {
									return true;
								}
								for (int l= 0, numFrames= treeFrames.length; l < numFrames; l++) {
									if (treeFrames[l].getData() != frames[l]) {
										return true;
									}
								}
								break;
							}
						}
						break;
					}
				}
				break;
			}
		}
		return false;
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
	 * Sets the editor input that was resolved for the
	 * source display.
	 * 
	 * @param editorInput editor input
	 */
	private void setEditorInput(IEditorInput editorInput) {
		fEditorInput = editorInput;
	}
	
	/**
	 * Returns the editor input that was resolved for the
	 * source display.
	 * 
	 * @return editor input
	 */
	protected IEditorInput getEditorInput() {
		return fEditorInput;
	}	
	
	/**
	 * Sets the id of the editor opened when displaying
	 * source.
	 * 
	 * @param editorId editor id
	 */
	private void setEditorId(String editorId) {
		fEditorId = editorId;
	}
	
	/**
	 * Returns the id of the editor opened when displaying
	 * source.
	 * 
	 * @return editor id
	 */
	protected String getEditorId() {
		return fEditorId;
	}	
	
	/**
	 * Sets the current source element, possibly <code>null</code>
	 * 
	 * @param sourceElement
	 */
	private void setSourceElement(Object sourceElement) {
		fSourceElement = sourceElement;
	}
	
	/**
	 * Returns the current source element, possibly <code>null</code>
	 * 
	 * @return Object
	 */
	protected Object getSourceElement() {
		return fSourceElement;
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
		if (property.equals(IDebugUIConstants.PREF_REUSE_EDITOR)) {
			fReuseEditor = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_REUSE_EDITOR);
		} else if (property.equals(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES)) {
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
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		if (fReuseEditor && fEditor != null) {
			IWorkbenchWindow dwindow= getSite().getWorkbenchWindow();
			if (dwindow == null) {
				return;
			}	
			IWorkbenchPage page= dwindow.getActivePage();
			if (page == null) {
				return;
			}
			IEditorReference[] refs = page.getEditorReferences();
			int index = -1;
			for (int i = 0; i < refs.length; i++) {
				if (fEditor.equals(refs[i].getEditor(false))) {
					index = i;
					break;
				}
			}
			if (index >= 0) {	
				memento.putString(IDebugUIConstants.PREF_REUSE_EDITOR, Integer.toString(index));
			}
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
				Object sourceElement = getSourceElement();
				if (sourceElement instanceof IAdaptable) {
					if (((IAdaptable)sourceElement).getAdapter(IResource.class) != null) {
						return new ShowInContext(null, new StructuredSelection(getSourceElement()));
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
		IWorkbenchPart part = partRef.getPart(false);
		if (part != null && part.equals(fEditor)) {
			fEditor = null;
		}
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