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
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.debug.internal.ui.ControlAction;
import org.eclipse.debug.internal.ui.CopyToClipboardActionDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.DisconnectActionDelegate;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.RelaunchActionDelegate;
import org.eclipse.debug.internal.ui.RemoveTerminatedAction;
import org.eclipse.debug.internal.ui.ResumeActionDelegate;
import org.eclipse.debug.internal.ui.ShowQualifiedAction;
import org.eclipse.debug.internal.ui.StepIntoActionDelegate;
import org.eclipse.debug.internal.ui.StepOverActionDelegate;
import org.eclipse.debug.internal.ui.StepReturnActionDelegate;
import org.eclipse.debug.internal.ui.SuspendActionDelegate;
import org.eclipse.debug.internal.ui.TerminateActionDelegate;
import org.eclipse.debug.internal.ui.TerminateAllAction;
import org.eclipse.debug.internal.ui.TerminateAndRemoveActionDelegate;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.texteditor.ITextEditor;

public class LaunchView extends AbstractDebugView implements ISelectionChangedListener, IPartListener {

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
	 * Updates the state of the buttons in the view
	 */
	protected void updateButtons() {
		updateSelectionActions();
		updateActions();
	}

	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		StructuredViewer viewer = getViewer();
		
		IAction action;
		
		action = new ControlAction(viewer, new TerminateActionDelegate());
		action.setEnabled(false);
		setAction("Terminate", action);

		action = new ControlAction(viewer, new DisconnectActionDelegate());
		action.setEnabled(false);
		setAction("Disconnect", action);

		action = new RemoveTerminatedAction();
		action.setEnabled(false);
		setAction("RemoveAll", action);

		action = new ControlAction(viewer, new RelaunchActionDelegate());
		action.setEnabled(false);
		setAction("Relaunch",action);

		setAction(REMOVE_ACTION, new ControlAction(viewer, new TerminateAndRemoveActionDelegate()));
		setAction("TerminateAll", new TerminateAllAction());
		setAction("Properties", new PropertyDialogAction(getSite().getWorkbenchWindow().getShell(), getSite().getSelectionProvider()));
		
		ControlAction cAction = new ControlAction(viewer, new ResumeActionDelegate());
		viewer.addSelectionChangedListener(cAction);
		cAction.setEnabled(false);
		setAction("Resume", cAction);

		cAction = new ControlAction(viewer, new SuspendActionDelegate());
		viewer.addSelectionChangedListener(cAction);
		cAction.setEnabled(false);
		setAction("Suspend", cAction);

		cAction = new ControlAction(viewer, new StepIntoActionDelegate());
		viewer.addSelectionChangedListener(cAction);
		cAction.setEnabled(false);
		setAction("StepInto", cAction);

		cAction = new ControlAction(viewer, new StepOverActionDelegate());
		viewer.addSelectionChangedListener(cAction);
		cAction.setEnabled(false);
		setAction("StepOver", cAction);

		cAction = new ControlAction(viewer, new StepReturnActionDelegate());
		viewer.addSelectionChangedListener(cAction);
		cAction.setEnabled(false);
		setAction("StepReturn", cAction);

		setAction("CopyToClipboard", new ControlAction(viewer, new CopyToClipboardActionDelegate()));

		IAction qAction = new ShowQualifiedAction(viewer);
		qAction.setChecked(false);
		setAction("ShowQualifiedNames", qAction);		
	}

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {
		LaunchViewer lv = new LaunchViewer(parent);
		lv.addSelectionChangedListener(this);
		lv.setContentProvider(createContentProvider());
		lv.setLabelProvider(new DelegatingModelPresentation());
		lv.setUseHashlookup(true);
		// add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(lv);
		lv.expandToLevel(2);
		lv.setInput(DebugPlugin.getDefault().getLaunchManager());
		setEventHandler(new LaunchViewEventHandler(this, lv));
		return lv;
	}
	
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(getAction("Resume"));
		tbm.add(getAction("Suspend"));
		tbm.add(getAction("Terminate"));
		tbm.add(getAction("Disconnect"));
		tbm.add(getAction("RemoveAll"));
		tbm.add(new Separator("#StepGroup")); //$NON-NLS-1$
		tbm.add(getAction("StepInto"));
		tbm.add(getAction("StepOver"));
		tbm.add(getAction("StepReturn"));
		tbm.add(new Separator("#RenderGroup")); //$NON-NLS-1$
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
		getEventHandler().dispose();
		super.dispose();
	}
	
	public void autoExpand(Object element) {
		Object selectee = element;
		if (element instanceof ILaunch) {
			IProcess[] ps= ((ILaunch)element).getProcesses();
				if (ps != null && ps.length > 0) {
					selectee= ps[0];
				}
		}
		getViewer().setSelection(new StructuredSelection(selectee), true);
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
		updateButtons();
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
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part == this) {
			showMarkerForCurrentSelection();
		}		
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
				if (obj == null || !(obj instanceof IDebugElement)) {
					return;
				}
					
				IDebugElement debugElement= (IDebugElement) obj;
				if (debugElement.getElementType() != IDebugElement.STACK_FRAME) {
					return;
				}
				
				IStackFrame stackFrame= (IStackFrame) debugElement;
				
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
			 "Error", "Exception occurred opening editor for debugger.", e.getStatus());
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
		updateActions();
		
		menu.add(new Separator(IDebugUIConstants.EMPTY_EDIT_GROUP));
		menu.add(new Separator(IDebugUIConstants.EDIT_GROUP));
		menu.add(getAction("CopyToClipboard"));
		menu.add(new Separator(IDebugUIConstants.EMPTY_STEP_GROUP));
		menu.add(new Separator(IDebugUIConstants.STEP_GROUP));
		menu.add(getAction("StepInto"));
		menu.add(getAction("StepOver"));
		menu.add(getAction("StepReturn"));
		menu.add(new Separator(IDebugUIConstants.EMPTY_THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.THREAD_GROUP));
		menu.add(getAction("Resume"));
		menu.add(getAction("Suspend"));
		menu.add(getAction("Terminate"));
		menu.add(getAction("Disconnect"));
		menu.add(new Separator(IDebugUIConstants.EMPTY_LAUNCH_GROUP));
		menu.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		menu.add(getAction(REMOVE_ACTION));
		menu.add(getAction("TerminateAll"));
		menu.add(getAction("RemoveAll"));
		menu.add(getAction("Relaunch"));
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowQualifiedNames"));
		menu.add(new Separator(IDebugUIConstants.PROPERTY_GROUP));
		PropertyDialogAction action = (PropertyDialogAction)getAction("Properties");
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
			getViewer().refresh(element);
		}
		if (selectNeeded) {
			getViewer().setSelection(new StructuredSelection(selectee), true);
		}
		if (children != null && children.length > 0) {
			//reveal the thread children of a debug target
			getViewer().reveal(children[0]);
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
}