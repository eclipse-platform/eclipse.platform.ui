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
import org.eclipse.debug.internal.ui.ControlAction;
import org.eclipse.debug.internal.ui.CopyToClipboardActionDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.RelaunchActionDelegate;
import org.eclipse.debug.internal.ui.RemoveTerminatedAction;
import org.eclipse.debug.internal.ui.ShowQualifiedAction;
import org.eclipse.debug.internal.ui.TerminateAllAction;
import org.eclipse.debug.internal.ui.TerminateAndRemoveActionDelegate;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
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

public class LaunchView extends AbstractDebugView implements ISelectionChangedListener, IPartListener, IPerspectiveListener, IPageListener {

	/**
	 * Event handler for this view
	 */
	private LaunchViewEventHandler fEventHandler;
	
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
	 * Creates a lanuch view and an instruction pointer marker for the view
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
		StructuredViewer viewer = getStructuredViewer();
		
		IAction action;
		
		action = new RemoveTerminatedAction(this);
		action.setEnabled(false);
		setAction("RemoveAll", action); //$NON-NLS-1$

		action = new ControlAction(viewer, new RelaunchActionDelegate());
		action.setEnabled(false);
		setAction("Relaunch",action); //$NON-NLS-1$

		setAction(REMOVE_ACTION, new ControlAction(viewer, new TerminateAndRemoveActionDelegate()));
		setAction("TerminateAll", new TerminateAllAction()); //$NON-NLS-1$
		setAction("Properties", new PropertyDialogAction(getSite().getWorkbenchWindow().getShell(), getSite().getSelectionProvider())); //$NON-NLS-1$
		
		setAction("CopyToClipboard", new ControlAction(viewer, new CopyToClipboardActionDelegate()));
 //$NON-NLS-1$
		IAction qAction = new ShowQualifiedAction(viewer);
		qAction.setChecked(false);
		setAction("ShowQualifiedNames", qAction);	 //$NON-NLS-1$
				
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
		setEventHandler(new LaunchViewEventHandler(this, lv));
		
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
	 * Returns the first stack frame in the first suspened
	 * thread of the given lanuch, or <code>null</code> if
	 * none.
	 * 
	 * @param lanuch a launch in this view
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
		tbm.add(getAction("RemoveAll"));
		tbm.add(new Separator(IDebugUIConstants.STEP_GROUP));
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		tbm.add(getAction("ShowQualifiedNames"));
	}	

	/**
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		if (getViewer() != null) {
			getViewer().removeSelectionChangedListener(this);
		}
		getSite().getPage().removePartListener(this);
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
		getSite().getWorkbenchWindow().removePageListener(this);
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}
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
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
	}
	
	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {			
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * When the perspective is changed back to a page containing
	 * a debug view, we must update the actions and source.
	 * 
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part == this) {
			updateActions();
			showMarkerForCurrentSelection();
		}		
	}	
		
	/**
	 * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(
		IWorkbenchPage page,
		IPerspectiveDescriptor perspective) {
			if (page.equals(getSite().getPage())) {
				updateActions();
			}
	}

	/**
	 * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	public void perspectiveChanged(
		IWorkbenchPage page,
		IPerspectiveDescriptor perspective,
		String changeId) {
	}

	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (page.equals(getSite().getPage())) {
			updateActions();
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
		if (!fShowingMarker) {
			try {
				fShowingMarker = true;
				ISelection selection= getViewer().getSelection();
				Object obj= null;
				if (selection instanceof IStructuredSelection) {
					obj= ((IStructuredSelection) selection).getFirstElement();
				}
				if (obj == null || !(obj instanceof IStackFrame)) {
					return;
				}
				
				IStackFrame stackFrame= (IStackFrame) obj;
				
				// Get the corresponding element.
				ILaunch launch = stackFrame.getLaunch();
				if (launch == null) {
					return;
				}
				ISourceLocator locator= launch.getSourceLocator();
				if (locator == null) {
					return;
				}
				Object sourceElement = locator.getSourceElement(stackFrame);
				if (sourceElement == null) {
					return;
				}
		
				// translate to an editor input using the model presentation
				IDebugModelPresentation presentation = getPresentation(stackFrame.getModelIdentifier());
				IEditorInput editorInput = null;
				String editorId= null;
				if (presentation != null) {
					editorInput= presentation.getEditorInput(sourceElement);
					editorId= presentation.getEditorId(editorInput, sourceElement);
				}
				
				if (editorInput != null) {
					int lineNumber= 0;
					try {
						lineNumber= stackFrame.getLineNumber();
					} catch (DebugException de) {
						DebugUIPlugin.logError(de);
					}
					openEditorAndSetMarker(editorInput, editorId, lineNumber, -1, -1);
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
			editor = page.openEditor(input, editorId, false);
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
		menu.add(getAction("CopyToClipboard")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_STEP_GROUP));
		menu.add(new Separator(IDebugUIConstants.STEP_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_LAUNCH_GROUP));
		menu.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		menu.add(getAction(REMOVE_ACTION));
		menu.add(getAction("TerminateAll")); //$NON-NLS-1$
		menu.add(getAction("RemoveAll")); //$NON-NLS-1$
		menu.add(getAction("Relaunch")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowQualifiedNames")); //$NON-NLS-1$
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
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	private void setEventHandler(LaunchViewEventHandler eventHandler) {
		fEventHandler = eventHandler;
	}
	
	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected LaunchViewEventHandler getEventHandler() {
		return fEventHandler;
	}	
	
	/**
	 * Added here to make visible for event handler
	 * 
	 * @see AbstractDebugView#asyncExec(Runnable)
	 */
	protected void asyncExec(Runnable r) {
		super.asyncExec(r);
	}
	
	/**
	 * Added here to make visible for event handler
	 * 
	 * @see AbstractDebugView#syncExec(Runnable)
	 */
	protected void syncExec(Runnable r) {
		super.syncExec(r);
	}

}