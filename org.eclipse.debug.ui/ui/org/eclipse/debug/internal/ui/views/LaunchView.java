package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.TerminateAllAction;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.texteditor.ITextEditor;

public class LaunchView extends AbstractDebugEventHandlerView implements ISelectionChangedListener, IPerspectiveListener, IPageListener {
	
	/**
	 * A marker for the source selection and icon for an
	 * instruction pointer.  This marker is transient.
	 */
	private IMarker fInstructionPointer;
	private boolean fShowingMarker = false;
	
	// marker attributes
	private final static String[] fgStartEnd = 
		new String[] {IMarker.CHAR_START, IMarker.CHAR_END};
		
	private final static String[] fgLineStartEnd = 
		new String[] {IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END};	
		
	/**
	 * Cache of the last stack frame that source was displayed
	 * for.
	 */
	private IStackFrame fLastFrame = null;
	
	/**
	 * Cache of the last editor input used to display source
	 */
	private IEditorInput fLastInput = null;
	
	/**
	 * Cache of the last editor id used to display source
	 */
	private String fLastEditorId = null;
	
	/**
	 * Wether this view is in the active page of a perspective.
	 */
	private boolean fIsActive = true; 	
	
	/**
	 * Creates a launch view and an instruction pointer marker for the view
	 */
	public LaunchView() {
		try {
			fInstructionPointer = ResourcesPlugin.getWorkspace().getRoot().createMarker(IInternalDebugUIConstants.INSTRUCTION_POINTER);
		} catch (CoreException e) {
			DebugUIPlugin.logError(e);
		}
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
		setAction("TerminateAll", new TerminateAllAction()); //$NON-NLS-1$
		setAction("Properties", new PropertyDialogAction(getSite().getWorkbenchWindow().getShell(), getSite().getSelectionProvider())); //$NON-NLS-1$
				
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
		lv.setContentProvider(createContentProvider());
		lv.setLabelProvider(new DelegatingModelPresentation());
		lv.setUseHashlookup(true);
		// add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(lv);
		lv.setInput(DebugPlugin.getDefault().getLaunchManager());
		setEventHandler(new LaunchViewEventHandler(this));
		
		// determine if active
		setActive(getSite().getPage().findView(getSite().getId()) != null);
		
		return lv;
	}
	
	/**
	 * Select the first stack frame in a suspended thread,
	 * if any.
	 */
	protected void initializeSelection() {
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
				DebugUIPlugin.logError(e);
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
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(IDebugUIConstants.THREAD_GROUP));
		tbm.add(new Separator(IDebugUIConstants.STEP_GROUP));
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
		setLastEditorId(null);
		setLastEditorInput(null);
		setLastStackFrame(null);
		super.dispose();
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
		updateActions();
		showMarkerForCurrentSelection();
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
		if (o instanceof IStackFrame) {
			return;
		} 
		TreeViewer tViewer= (TreeViewer)getViewer();
		boolean expanded= tViewer.getExpandedState(o);
		tViewer.setExpandedState(o, !expanded);
	}

	/**
	 * When the perspective is changed back to a page containing
	 * a debug view, we must update the actions and source.
	 * 
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
		if (part == this) {
			updateActions();
			showMarkerForCurrentSelection();
		}		
	}	
		
	/**
	 * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		setActive(page.findView(getSite().getId()) != null);
		updateActions();
		showMarkerForCurrentSelection();
	}

	/**
	 * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	public void perspectiveChanged(
		IWorkbenchPage page,
		IPerspectiveDescriptor perspective,
		String changeId) {
			setActive(page.findView(getSite().getId()) != null);
	}

	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (page.equals(getSite().getPage())) {
			setActive(true);
			updateActions();
			showMarkerForCurrentSelection();
			ILaunch[] launches= DebugPlugin.getDefault().getLaunchManager().getLaunches();
			if (launches.length > 0) {
				((LaunchViewEventHandler)getEventHandler()).removeTerminatedLaunches(launches[launches.length - 1]);
			}
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
	 * Returns the configured instruction pointer.
	 * Selection is based on the line number OR char start and char end.
	 */
	protected IMarker getInstructionPointer(final int lineNumber, final int charStart, final int charEnd) {
		
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
			workspace.run(runnable, null);
		} catch (CoreException ce) {
			DebugUIPlugin.logError(ce);
		}
		
		return fInstructionPointer;
	}		
	
	/**
	 * Opens a marker for the current selection if it is a stack frame.
	 * If the current selection is a thread, deselection occurs.
	 * Otherwise, nothing will happen.
	 */
	protected void showMarkerForCurrentSelection() {
		// ensure this view is visible in the active page
		if (!isActive()) {
			return;
		}		
		if (!fShowingMarker) {
			try {
				fShowingMarker = true;
				ISelection selection= getViewer().getSelection();
				Object obj= null;
				if (selection instanceof IStructuredSelection) {
					obj= ((IStructuredSelection) selection).getFirstElement();
				}
				if (!(obj instanceof IStackFrame)) {
					return;
				}
				
				IStackFrame stackFrame= (IStackFrame) obj;
				
				Object sourceElement = null;
				IEditorInput editorInput = null;
				String editorId= null;
				
				if (stackFrame.equals(getLastStackFrame())) {
					// avoid lookup
					editorId = getLastEditorId();
					editorInput = getLastEditorInput();
				} else {
					ILaunch launch = stackFrame.getLaunch();
					if (launch == null) {
						return;
					}
					ISourceLocator locator= launch.getSourceLocator();
					if (locator == null) {
						return;
					}
					sourceElement = locator.getSourceElement(stackFrame);
					if (sourceElement == null) {
						return;
					}					
					// Get the corresponding element.
		
					// translate to an editor input using the source presentation
					// provided by the source locator, or the default debug model
					// presentation
					ISourcePresentation presentation = null;
					if (locator instanceof ISourcePresentation) {
						presentation = (ISourcePresentation)locator;
					} else {
						presentation = getPresentation(stackFrame.getModelIdentifier());
					}
				
				
					if (presentation != null) {
						editorInput= presentation.getEditorInput(sourceElement);
					}
					
					if (editorInput != null) {
						editorId= presentation.getEditorId(editorInput, sourceElement);
					}
					
					setLastStackFrame(stackFrame)				;
					setLastEditorInput(editorInput);
					setLastEditorId(editorId);
				}
				

				
				if (editorInput != null && editorId != null) {
					int lineNumber= 0;
					int start = -1;
					int end = -1;
					try {
						lineNumber= stackFrame.getLineNumber();
						start= stackFrame.getCharStart();
						end= stackFrame.getCharEnd();
					} catch (DebugException de) {
						DebugUIPlugin.logError(de);
					}
					openEditorAndSetMarker(editorInput, editorId, lineNumber, start, end);
				}
			} finally {
				fShowingMarker= false;
			}
		}
	}

	/**
	 * Get the active window and open/bring to the front an editor on the source element.
	 * Selection is based on the line number OR the char start and end.
	 */
	protected void openEditorAndSetMarker(IEditorInput input, String editorId, int lineNumber, int charStart, int charEnd) {
		IWorkbenchWindow dwindow= getSite().getWorkbenchWindow();
		if (dwindow == null) {
			return;
		}
		
		IWorkbenchPage page= dwindow.getActivePage();
		if (page == null) {
			return;
		}
		
		IEditorPart editor = null;
		try {
			editor= page.getActiveEditor();
			if (editor == null || !editor.getEditorInput().equals(input)) {
				editor = page.openEditor(input, editorId, false);
			}
		} catch (PartInitException e) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getDefault().getShell(), 
			 DebugUIViewsMessages.getString("LaunchView.Error_1"), DebugUIViewsMessages.getString("LaunchView.Exception_occurred_opening_editor_for_debugger._2"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
		}		
		
		if (editor != null && (lineNumber >= 0 || charStart >= 0)) {
			//have an editor and either a lineNumber or a starting character
			IMarker marker= getInstructionPointer(lineNumber, charStart, charEnd);
			editor.gotoMarker(marker);
		}
	}

	/**
	 * Deselects any source in the active editor that was 'programmatically' selected by
	 * the debugger.
	 */
	public void clearSourceSelection() {		
		// Get the active editor
		IEditorPart editor= getSite().getPage().getActiveEditor();
		if (!(editor instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor= (ITextEditor)editor;
		
		// Get the current text selection in the editor.  If there is none, 
		// then there's nothing to do
		ITextSelection textSelection= (ITextSelection)textEditor.getSelectionProvider().getSelection();
		if (textSelection.isEmpty()) {
			return;
		}
		int startChar= textSelection.getOffset();
		int endChar= startChar + textSelection.getLength() - 1;
		int startLine= textSelection.getStartLine();
		
		// Check to see if the current selection looks the same as the last 'programmatic'
		// selection in fInstructionPointer.  If not, it must be a user selection, which
		// we leave alone.  In practice, we can leave alone any user selections on other lines,
		// but if the user makes a selection on the same line as the last programmatic selection,
		// it will get cleared.
		int lastCharStart= fInstructionPointer.getAttribute(IMarker.CHAR_START, -1);
		if (lastCharStart == -1) {
			// subtract 1 since editor is 0-based
			if (fInstructionPointer.getAttribute(IMarker.LINE_NUMBER, -1) - 1 != startLine) {
				return;
			}
		} else {
			int lastCharEnd= fInstructionPointer.getAttribute(IMarker.CHAR_END, -1);
			if ((lastCharStart != startChar) || (lastCharEnd != endChar)) {
				return;			     
			}
		}
		
		ITextSelection nullSelection= getNullSelection(startLine, startChar);
		textEditor.getSelectionProvider().setSelection(nullSelection);		
	}

	/**
	 * Creates and returns an ITextSelection that is a zero-length selection located at the
	 * start line and start char.
	 */
	protected ITextSelection getNullSelection(final int startLine, final int startChar) {
		return new ITextSelection() {
			public int getStartLine() {
				return startLine;
			}
			public int getEndLine() {
				return startLine;
			}
			public int getOffset() {
				return startChar;
			}
			public String getText() {
				return ""; //$NON-NLS-1$
			}
			public int getLength() {
				return 0;
			}
			public boolean isEmpty() {
				return true;
			}
		};
	}
	
	/**
	 * @see AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		
		menu.add(new Separator(IDebugUIConstants.EMPTY_EDIT_GROUP));
		menu.add(new Separator(IDebugUIConstants.EDIT_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_STEP_GROUP));
		menu.add(new Separator(IDebugUIConstants.STEP_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_LAUNCH_GROUP));
		menu.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		menu.add(getAction("TerminateAll")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.PROPERTY_GROUP));
		PropertyDialogAction action = (PropertyDialogAction)getAction("Properties"); //$NON-NLS-1$
		action.setEnabled(action.isApplicableForSelection());
		menu.add(action);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}	
	
	/**
	 * Auto-expand and select the given element - must be called in UI thread.
	 * This is used to implement auto-expansion-and-select on a SUSPEND event.
	 */
	public void autoExpand(Object element, boolean refreshNeeded, boolean selectNeeded) {
		Object selectee = element;
		Object[] children= null;
		if (element instanceof IThread) {
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
						DebugUIPlugin.logError(de);
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
	 * Sets the last stack frame that source was retrieved
	 * for. Used to avoid source lookups for the same stack
	 * frame when stepping.
	 * 
	 * @param frame stack frame
	 */
	private void setLastStackFrame(IStackFrame frame) {
		fLastFrame = frame;
	}
	
	/**
	 * Returns the last stack frame that source was retrieved
	 * for. Used to avoid source lookups for the same stack
	 * frame when stepping.
	 * 
	 * @return stack frame, or <code>null</code>
	 */
	protected IStackFrame getLastStackFrame() {
		return fLastFrame;
	}	
	
	/**
	 * Sets the last editor input that was resolved for the
	 * last source display. Used to avoid editor input creation
	 * when stepping through the same stack frame.
	 * 
	 * @param editorInput editor input
	 */
	private void setLastEditorInput(IEditorInput editorInput) {
		fLastInput = editorInput;
	}
	
	/**
	 * Returns the last editor input that was resolved for the
	 * last source display. Used to avoid editor input creation
	 * when stepping through the same stack frame.
	 * 
	 * @return editor input
	 */
	protected IEditorInput getLastEditorInput() {
		return fLastInput;
	}	
	
	/**
	 * Sets the id of the last editor opened when displaying
	 * source. Used to avoid editor id lookup when stepping
	 * through the same frame.
	 * 
	 * @param editorId editor id
	 */
	private void setLastEditorId(String editorId) {
		fLastEditorId = editorId;
	}
	
	/**
	 * Returns the id of the last editor opened when displaying
	 * source. Used to avoid editor id lookup when stepping
	 * through the same frame.
	 * 
	 * @return editor id
	 */
	protected String getLastEditorId() {
		return fLastEditorId;
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
}