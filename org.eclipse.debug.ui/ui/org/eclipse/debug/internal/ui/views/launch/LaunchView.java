/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
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
import org.eclipse.debug.ui.DebugUITools;
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
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class LaunchView extends AbstractDebugEventHandlerView implements ISelectionChangedListener, IPerspectiveListener, IPageListener, IPropertyChangeListener, IResourceChangeListener, IShowInTarget, IShowInSource, IShowInTargetList {
	

	/**
	 * A marker for the source selection and icon for an
	 * instruction pointer.  This marker is transient.
	 */
	private IMarker fInstructionPointer;
	private boolean fShowingEditor = false;
	
	// marker attributes
	private final static String[] fgStartEnd = 
		new String[] {IMarker.CHAR_START, IMarker.CHAR_END};
		
	private final static String[] fgLineStartEnd = 
		new String[] {IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END};	
		
	/**
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
	 * Whether or not this view automatically opens and closes views
	 * based on debug contexts.
	 */
	private boolean fAutoManage= true;
	/**
	 * Memento key used to store the views setting for automatically
	 * managing views.
	 */
	private static String ATTR_AUTO_MANAGE_VIEWS= "autoManageViews"; //$NON-NLS-1$
	/**
	 * Context manager which automatically opens and closes views
	 * based on debug contexts.
	 */
	private LaunchViewContextListener contextListener;
	
	/**
	 * Map of ILaunch objects to the List of EnabledSubmissions that were
	 * submitted for them.
	 * Key: ILaunch
	 * Value: List <EnabledSubmission>
	 */
	private Map fContextSubmissions= new HashMap();
	
	/**
	 * Creates a launch view and an instruction pointer marker for the view
	 */
	public LaunchView() {
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		contextListener= new LaunchViewContextListener(this);
	}
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.DEBUG_VIEW;
	}
	
	/**
	 * @see AbstractDebugView#createActions()
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

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		LaunchViewer lv = new LaunchViewer(parent);
		lv.addSelectionChangedListener(this);
		lv.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					handleDeleteKeyPressed();
				}
			}
		});
		lv.setContentProvider(createContentProvider());
		final DelegatingModelPresentation presentation = new DelegatingModelPresentation();
		DebugViewDecoratingLabelProvider labelProvider= new DebugViewDecoratingLabelProvider(new DebugViewInterimLabelProvider(presentation), new DebugViewLabelDecorator(presentation));
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
					autoExpand(frame, false, true);
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
				DebugUIPlugin.log(e);
			}
		}
		return null;
	}
	
	/**
	 * @see IViewPart#init(IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getPage().addPartListener(this);
		site.getWorkbenchWindow().addPageListener(this);
		site.getWorkbenchWindow().addPerspectiveListener(this);
	}

	/**
	 * @see IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		site.getPage().addPartListener(this);
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
		String autoManage = memento.getString(ATTR_AUTO_MANAGE_VIEWS);
		if (autoManage != null) {
			fAutoManage= Boolean.valueOf(autoManage).booleanValue();
		}
		contextListener.init(memento);
	}
		
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
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

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (getViewer() != null) {
			getViewer().removeSelectionChangedListener(this);
		}
		
		getSite().getPage().removePartListener(this);
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
		getSite().getWorkbenchWindow().removePageListener(this);
		
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
	 * Creates and returns the content provider to use for
	 * the viewer of this view.
	 */
	protected IStructuredContentProvider createContentProvider() {
		return new LaunchViewContentProvider();
	}
	
	/**
	 * The selection has changed in the viewer. Show the
	 * associated source code if it is a stack frame.
	 * 
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		clearStatusLine();
		updateObjects();
		showEditorForCurrentSelection();
		showViewsForCurrentSelection();
	}
	

	

	


	/**
	 * Lookup source element for current stack frame again.
	 */
	public void redoSourceLookup() {
		setStackFrame(null);
		selectionChanged(null);
	}
	
	/**
	 * Returns whether this view automatically opens and closes
	 * views based on contexts
	 * @return whether or not this view automatically manages
	 * views based on contexts
	 */
	public boolean isAutoManageViews() {
		return fAutoManage;
	}
	
	/**
	 * Sets whether or not this view will automatically
	 * open and close views based on contexts.
	 * @param autoManage whether or not this view should
	 * automatically manage views based on contexts
	 */
	public void setAutoManageViews(boolean autoManage) {
		fAutoManage= autoManage;
		showViewsForCurrentSelection();
	}
	
	/**
	 * Determines the debug context associated with the selected
	 * stack frame's debug model (if any) and activates that
	 * context. This triggers this view's context listener
	 * to automatically open/close/activate views as appropriate.
	 */
	private void showViewsForCurrentSelection() {
		// ensure this view is visible in the active page
		if (!isActive() || !fAutoManage) {
			return;
		}		
		Object selection = ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
		ILaunch launch= getLaunch(selection);
		if (launch == null) {
			return;
		}
		List contextIds = getContextsForSelection(selection);
		if (!contextIds.isEmpty()) {
			enableContexts(contextIds, launch);
		}
	}
			
	/**
	 * Returns the contexts (String identifiers) associated with the
	 * given selection.
	 * 
	 * @param selection the selection
	 * @return the contexts associated with the given selection
	 */
	protected List getContextsForSelection(Object selection) {
		List contextIds= new ArrayList();
		if (selection instanceof IAdaptable) {
			IDebugModelProvider context= (IDebugModelProvider) Platform.getAdapterManager().getAdapter(selection, IDebugModelProvider.class);
			if (context != null) {
				String[] modelIds= context.getModelIdentifiers();
				if (modelIds != null) {
					for (int i = 0; i < modelIds.length; i++) {
						List ids= contextListener.getDebugModelContexts(modelIds[i]);
						if (ids != null) {
							contextIds.addAll(ids);
						}
					}
				}
			}
		}
		if (contextIds.isEmpty() && selection instanceof IStackFrame) {
			List ids = contextListener.getDebugModelContexts(((IStackFrame) selection).getModelIdentifier());
			if (ids != null) {
				contextIds.addAll(ids);
			}
		}
		return contextIds;
	}
	
	/**
	 * Activate the given contexts for the given launch. Context
	 * IDs which are not currently enabled in the workbench will be
	 * submitted to the workbench. The launch view context listener
	 * will be told about contexts that are already enabled so that
	 * their views can be promoted.
	 * 
	 * @param contextIds the contexts to enable
	 * @param launch the launch for which the contexts are being enabled
	 */
	protected void enableContexts(List contextIds, ILaunch launch) {
		Set enabledContexts = PlatformUI.getWorkbench().getContextSupport().getContextManager().getEnabledContextIds();
		Set contextsAlreadyEnabled= new HashSet();
		Iterator iter= contextIds.iterator();
		while (iter.hasNext()) {
			String contextId= (String) iter.next();
			if (enabledContexts.contains(contextId)) {
				// If a context is already enabled, submitting it won't
				// generate a callback from the workbench. So we inform
				// our context listener ourselves.
				contextsAlreadyEnabled.add(contextId);
			}
		}
		submitContexts(contextIds, launch);
		contextListener.contextActivated((String[]) contextsAlreadyEnabled.toArray(new String[0]));
	}

	/**
	 * Submits the given context IDs to the workbench context support
	 * on behalf of the given launch. When the launch terminates,
	 * the context submissions will be automatically removed.
	 *  
	 * @param contextIds the contexts to submit
	 * @param launch the launch for which the contexts are being submitted
	 */
	protected void submitContexts(List contextIds, ILaunch launch) {
		List submissions = (List) fContextSubmissions.get(launch);
		if (submissions == null) {
			submissions= new ArrayList();
			fContextSubmissions.put(launch, submissions);
		}
		List newSubmissions= new ArrayList();
		Iterator iter= contextIds.iterator();
		while (iter.hasNext()) {
			newSubmissions.add(new EnabledSubmission((Shell) null, null, (String) iter.next()));
		}
		IWorkbenchContextSupport contextSupport = PlatformUI.getWorkbench().getContextSupport();
		if (!newSubmissions.isEmpty()) {
			contextSupport.addEnabledSubmissions(newSubmissions);
			// After adding the new submissions, remove any old submissions
			// that exist for the same context IDs. This prevents us from
			// building up a ton of redundant submissions.
			List submissionsToRemove= new ArrayList();
			ListIterator oldSubmissions= submissions.listIterator();
			while (oldSubmissions.hasNext()) {
				EnabledSubmission oldSubmission= (EnabledSubmission) oldSubmissions.next();
				String contextId = oldSubmission.getContextId();
				if (contextIds.contains(contextId)) {
					oldSubmissions.remove();
					submissionsToRemove.add(oldSubmission);
				}
			}
			contextSupport.removeEnabledSubmissions(submissionsToRemove);
			submissions.addAll(newSubmissions);
		}
	}
	
	/**
	 * Notifies this view that the given launches have terminated. When a launch
	 * terminates, remove all context submissions associated with it.
	 * 
	 * @param launches the terminated launches
	 */
	protected void launchesTerminated(ILaunch[] launches) {
		List allSubmissions= new ArrayList();
		for (int i = 0; i < launches.length; i++) {
			List submissions= (List) fContextSubmissions.remove(launches[i]);
			if (submissions != null) {
				allSubmissions.addAll(submissions);
			}
		}
		PlatformUI.getWorkbench().getContextSupport().removeEnabledSubmissions(allSubmissions);
	}
	
	/**
	 * Removes all context submissions made by this view.
	 */
	protected void removeAllContextSubmissions() {
		IWorkbenchContextSupport contextSupport= PlatformUI.getWorkbench().getContextSupport();
		List submissions= new ArrayList();
		Iterator iterator = fContextSubmissions.values().iterator();
		while (iterator.hasNext()) {
			submissions.addAll((List) iterator.next());
		}
		contextSupport.removeEnabledSubmissions(submissions);
		fContextSubmissions.clear();
	}

	/**
	 * Returns the ILaunch associated with the given selection or
	 * <code>null</code> if none can be determined.
	 * 
	 * @param selection the selection or <code>null</code>
	 * @return the ILaunch associated with the given selection or <code>null</code>
	 */
	protected static ILaunch getLaunch(Object selection) {
		ILaunch launch= null;
		if (selection instanceof ILaunch) {
			launch= (ILaunch) selection;
		} else if (selection instanceof IDebugElement) {
			launch= ((IDebugElement) selection).getLaunch();
		} else if (selection instanceof IProcess) {
			launch= ((IProcess) selection).getLaunch();
		}
		return launch;
	}

	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
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
		
	/**
	 * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		setActive(page.findView(getSite().getId()) != null);
		updateObjects();
		showEditorForCurrentSelection();
	}

	/**
	 * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		setActive(page.findView(getSite().getId()) != null);
	}

	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (getSite().getPage().equals(page)) {
			setActive(true);
			updateObjects();
			showEditorForCurrentSelection();
		}
	}
	
	/**
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(fEditor)) {
			fEditor = null;
		}
	}
	

	/**
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
		if (part == this) {
			showViewsForCurrentSelection();
		}
	}
	
	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
	}
		
	/**
	 * Returns the configured instruction pointer or <code>null</code> if an
	 * exception occurs creating the marker. Selection is based on the line
	 * number OR char start and char end.
	 */
	protected IMarker getInstructionPointer(final int lineNumber, final int charStart, final int charEnd) {
		
		if (fInstructionPointer == null) {
			try {
				fInstructionPointer = ResourcesPlugin.getWorkspace().getRoot().createMarker(IInternalDebugUIConstants.INSTRUCTION_POINTER);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
				return null;
			}
		}
		
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (lineNumber == -1) {
					fInstructionPointer.setAttributes(fgStartEnd, 
						new Object[] {new Integer(charStart), new Integer(charEnd)});
				} else {
					fInstructionPointer.setAttributes(fgLineStartEnd, 
						new Object[] {new Integer(lineNumber), new Integer(charStart), new Integer(charEnd)});
				}
			}
		};
		
		try {
			workspace.run(runnable, null, 0, null);
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);
		}
		
		return fInstructionPointer;
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
		try {
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
					commonSourceNotFound(stackFrame, stackFrame);
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
		} catch (Throwable e) {
			DebugUIPlugin.log(e);
		}
	}
	
	/**
	 * @see IDebugView#getPresentation(String)
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
	private void commonSourceNotFound(IStackFrame frame, Object objectRequestingSource) {
		setEditorInput(new CommonSourceNotFoundEditorInput(frame, objectRequestingSource));
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
	
			int lineNumber= 0;
			int charStart = -1;
			int charEnd = -1;
			try {
				lineNumber= stackFrame.getLineNumber();
				charStart= stackFrame.getCharStart();
				charEnd= stackFrame.getCharEnd();
			} catch (DebugException de) {
				DebugUIPlugin.log(de);
			}
			if (!selectAndReveal(editor, stackFrame)) {
				// perform the select and reveal ourselves
				if (lineNumber >= 0 || charStart >= 0) {
					if (editor instanceof ITextEditor) {
						selectAndReveal((ITextEditor)editor, lineNumber, charStart, charEnd, stackFrame.getThread());
					} else {
						IMarker marker= getInstructionPointer(lineNumber, charStart, charEnd);
						if (marker != null) {
							IDE.gotoMarker(editor, marker);
						}
					}
				}
			}
			// add instruction pointer annotation (for text editors only)
			if (editor instanceof ITextEditor) {
				DebugUITools.addInstructionPointer((ITextEditor)editor, stackFrame);
			}
			// decorate the editor
			decorateEditor(editor, stackFrame);		
		} finally {
			fShowingEditor= false;
		}
	}
	
	/**
	 * Delegate to the model presentation to decorate the opened editor.
	 * 
	 * @param editor editor to decorate
	 * @param stackFrame stack frame to decorate for
	 */
	private void decorateEditor(IEditorPart editor, IStackFrame stackFrame) {
		if (fEditorPresentation != null) {
			fEditorPresentation.decorateEditor(editor, stackFrame);
			Decoration decoration = new StandardDecoration(fEditorPresentation, editor, stackFrame.getThread());
			DecorationManager.addDecoration(decoration);
		}
	}

	/**
	 * Delegates to the model presentation to perform the select and reveal,
	 * and returns whether the select and reveal was completed.
	 * 
	 * @param editor the editor to position 
	 * @param stackFrame the stack frame to position for
	 * @return whether the select and reveal is complete
	 */
	private boolean selectAndReveal(IEditorPart editor, IStackFrame stackFrame) {
		if (fEditorPresentation != null) {
			return fEditorPresentation.selectAndReveal(editor, stackFrame);
		}
		return false;
	}

	/**
	 * Highlights the given line or character range in the given editor
	 */
	private void selectAndReveal(ITextEditor editor, int lineNumber, int charStart, int charEnd, IThread thread) {
		lineNumber--; // Document line numbers are 0-based. Debug line numbers are 1-based.
		if (charStart > 0 && charEnd > charStart) {
			editor.selectAndReveal(charStart, 0);
			return;
		}
		int offset= -1;
		IRegion region= getLineInformation(editor, lineNumber);
		if (region == null) {
			// use "goto marker" if line info not available
			// increment line number for marker approach (1 based)
			lineNumber++;
			IMarker marker= getInstructionPointer(lineNumber, charStart, charEnd);
			if (marker != null) {
				IDE.gotoMarker(editor, marker);
				// add decoration
				Decoration decoration = new MarkerTextSelection(editor, lineNumber, thread);
				DecorationManager.addDecoration(decoration);
			}			
			return;
		}
		if (charStart > 0) {
			offset= charStart;
		} else { 
			offset= region.getOffset();
		}
		editor.selectAndReveal(offset, 0);
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
					IEditorPart refEditor= refs[i].getEditor(true);
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
					page.closeEditor(fEditor, false);
					editor = openEditor(page, input, id);
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
	
	/**
	 * @see AbstractDebugView#fillContextMenu(IMenuManager)
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
	public void autoExpand(Object element, boolean refreshNeeded, boolean selectNeeded) {
		Object selectee = element;
		Object[] children= null;
		if (element instanceof IThread) {
			if (!refreshNeeded) {
				refreshNeeded= threadRefreshNeeded((IThread)element);
			}
			// try the top stack frame
			try {
				selectee = ((IThread)element).getTopStackFrame();
			} catch (DebugException de) {
			}
			if (selectee == null) {
				selectee = element;
			}
		} else if (element instanceof ILaunch) {
			IDebugTarget dt = ((ILaunch)element).getDebugTarget();
			if (dt != null) {
				selectee= dt;
				try {
					children= dt.getThreads();
				} catch (DebugException de) {
					DebugUIPlugin.log(de);
				}
			} else {
				IProcess[] processes= ((ILaunch)element).getProcesses();
				if (processes.length != 0) {
					selectee= processes[0];
				}		
			}
		}
		if (refreshNeeded) {
			//ensures that the child item exists in the viewer widget
			//set selection only works if the child exists
			getStructuredViewer().refresh(element);
		}
		if (selectNeeded) {
			getViewer().setSelection(new StructuredSelection(selectee), true);
		}
		if (children != null && children.length > 0) {
			//reveal the thread children of a debug target
			getStructuredViewer().reveal(children[0]);
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
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IDebugUIConstants.PREF_REUSE_EDITOR)) {
			fReuseEditor = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_REUSE_EDITOR);
		}
	}

	/**
	 * @see IViewPart#saveState(IMemento)
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
		memento.putString(ATTR_AUTO_MANAGE_VIEWS, Boolean.toString(fAutoManage));
		contextListener.saveState(memento);
	}

	/**
	 * Visitor for handling resource deltas. When a project is closed, we must clear
	 * the cache of editor input/stack frame, etc., as the elements can become invalid.
	 */
	class LaunchViewVisitor implements IResourceDeltaVisitor {
		/**
		 * @see IResourceDeltaVisitor#visit(IResourceDelta)
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
	
	/**
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
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
	
	/**
	 * @see IShowInTarget#show(org.eclipse.ui.part.ShowInContext)
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

	/**
	 * @see IShowInSource#getShowInContext()
	 */
	public ShowInContext getShowInContext() {
		if (isActive()) { 
			IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
			if (selection != null && !selection.isEmpty()) { 
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
	
	/**
	 * @see org.eclipse.ui.part.IShowInTargetList#getShowInTargetIds()
	 */
	public String[] getShowInTargetIds() {
		return new String[] { IPageLayout.ID_RES_NAV };
	}

}
