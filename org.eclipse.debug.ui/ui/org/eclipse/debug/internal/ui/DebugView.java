package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.IProgressMonitor;import org.eclipse.debug.core.DebugException;import org.eclipse.debug.core.ILaunch;import org.eclipse.debug.core.model.*;import org.eclipse.debug.ui.IDebugModelPresentation;import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.action.*;import org.eclipse.jface.text.ITextSelection;import org.eclipse.jface.viewers.*;import org.eclipse.swt.widgets.Composite;import org.eclipse.ui.*;import org.eclipse.ui.help.ViewContextComputer;import org.eclipse.ui.help.WorkbenchHelp;import org.eclipse.ui.texteditor.ITextEditor;

public class DebugView extends LaunchesView implements IPartListener {
	
	protected final static String PREFIX= "debug_view.";

	protected ControlAction fResumeAction;
	protected ControlAction fSuspendAction;
	protected ControlAction fStepIntoAction;
	protected ControlAction fStepOverAction;
	protected ControlAction fStepReturnAction;
	protected ControlAction fCopyToClipboardAction;

	protected ShowQualifiedAction fShowQualifiedAction;

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
	 * The editor associated with this debug view,
	 * or <code>null</code> if none.
	 */
	private IEditorPart fEditor;
	private Integer fEditorMemento;
	private static final String DEBUG_EDITOR= DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier() + ".DEBUG_EDITOR";
	
	
	/**
	 * Creates a debug view and an instruction pointer marker for the view
	 */
	public DebugView() {
		try {
			fInstructionPointer = ResourcesPlugin.getWorkspace().getRoot().createMarker(IInternalDebugUIConstants.INSTRUCTION_POINTER);
		} catch (CoreException e) {
			DebugUIUtils.logError(e);
		}
	}
	
	/**
	 * @see IViewPart
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addPartListener(this);
	}
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.DEBUG_VIEW ));
	}
	
	/**
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		super.dispose();
		getSite().getPage().removePartListener(this);
		fEditor = null;
	}

	/**
	 * Returns the configured instruction pointer.
	 * Selection is based on the line number OR char start and char end.
	 */
	protected IMarker getInstructionPointer(final int lineNumber, final int charStart, final int charEnd, final boolean isTopStackFrame) {
		
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
			DebugUIUtils.logError(ce);
		}
		
		return fInstructionPointer;
	}	
	
	/**
	 * Initializes the actions of this view.
	 */
	protected void initializeActions(LaunchesViewer viewer) {
		super.initializeActions(viewer);

		fResumeAction= new ControlAction(viewer, new ResumeActionDelegate());
		viewer.addSelectionChangedListener(fResumeAction);
		fResumeAction.setEnabled(false);

		fSuspendAction= new ControlAction(viewer, new SuspendActionDelegate());
		viewer.addSelectionChangedListener(fSuspendAction);
		fSuspendAction.setEnabled(false);

		fStepIntoAction= new ControlAction(viewer, new StepIntoActionDelegate());
		viewer.addSelectionChangedListener(fStepIntoAction);
		fStepIntoAction.setEnabled(false);

		fStepOverAction= new ControlAction(viewer, new StepOverActionDelegate());
		viewer.addSelectionChangedListener(fStepOverAction);
		fStepOverAction.setEnabled(false);

		fStepReturnAction= new ControlAction(viewer, new StepReturnActionDelegate());
		viewer.addSelectionChangedListener(fStepReturnAction);
		fStepReturnAction.setEnabled(false);

		fCopyToClipboardAction= new ControlAction(viewer, new CopyToClipboardActionDelegate());

		fShowQualifiedAction= new ShowQualifiedAction(viewer);
		fShowQualifiedAction.setChecked(false);
	}

	/**
	 * Configures the toolBar
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(fResumeAction);
		tbm.add(fSuspendAction);
		tbm.add(fTerminateAction);
		tbm.add(fDisconnectAction);
		tbm.add(fRemoveTerminatedAction);
		tbm.add(new Separator("#StepGroup"));
		tbm.add(fStepIntoAction);
		tbm.add(fStepOverAction);
		tbm.add(fStepReturnAction);
		tbm.add(new Separator("#RenderGroup"));
		tbm.add(fShowQualifiedAction);
	}

	/**
	 * The selection has changed in the viewer. Show the
	 * associated source code if it is a stack frame.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		showMarkerForCurrentSelection();
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
				ISelection selection= fViewer.getSelection();
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
				ISourceLocator locator= stackFrame.getSourceLocator();
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
					boolean isTopStackFrame= false;
					int lineNumber= 0;
					try {
						isTopStackFrame= stackFrame.getThread().getTopStackFrame() == stackFrame;
					} catch (DebugException de) {
					}
					try {
						lineNumber= stackFrame.getLineNumber();
					} catch (DebugException de) {
					}
					openEditorAndSetMarker(editorInput, editorId, lineNumber, -1, -1, isTopStackFrame);
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
	protected void openEditorAndSetMarker(IEditorInput input, String editorId, int lineNumber, int charStart, int charEnd, boolean isTopStackFrame) {
		IWorkbenchWindow dwindow= getSite().getWorkbenchWindow();
		if (dwindow == null) {
			return;
		}
		
		
		IWorkbenchPage page= dwindow.getActivePage();
		if (page == null) {
			return;
		}
		IEditorPart editor= null;
		IEditorPart[] editorParts= page.getEditors();
		
		
		// restore editor on startup
		if (fEditorMemento != null) {
			if (editorParts.length > fEditorMemento.intValue())
				fEditor = editorParts[fEditorMemento.intValue()];
			fEditorMemento = null;
		}


		for (int i= 0; i < editorParts.length; i++) {
			IEditorPart part= editorParts[i];
			if (input.equals(part.getEditorInput())) {
				editor= part;
				break;
			}
		}
		
		if (editor == null) {
			if (fEditor != null) {
				if (!fEditor.isDirty()) {
					page.closeEditor(fEditor, false);
				}
			}
			try {
				editor= page.openEditor(input, editorId);
				fEditor = editor;
				page.activate(this);
			} catch (PartInitException e) {
				DebugUIUtils.logError(e);
			}

		} else {
			page.bringToTop(editor);
		}
		
		if (editor != null && (lineNumber >= 0 || charStart >= 0)) {
			//have an editor and either a lineNumber or a starting character
			IMarker marker= getInstructionPointer(lineNumber, charStart, charEnd, isTopStackFrame);
			editor.gotoMarker(marker);
		}
	}

	/**
	 * Deselect any source in the active editor that was 'programmatically' selected by
	 * the debugger.
	 */
	public void clearSourceSelection() {		
		// Get the active editor
		IEditorPart editor= getSite().getPage().getActiveEditor();
		if ((editor == null) || !(editor instanceof ITextEditor)) {
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
		// selection in fSourceLocationMarker.  If not, it must be a user selection, which
		// we leave alone.  In practice, we can leave alone any user selections on other lines,
		// but if the user makes a selection on the same line as the last programmatic selection,
		// it will get cleared.
		int lastCharStart= fInstructionPointer.getAttribute(IMarker.CHAR_START, -1);
		int lastCharEnd= fInstructionPointer.getAttribute(IMarker.CHAR_END, -1);;
		if (lastCharStart == -1) {
			// subtract 1 since editor is 0-based
			if (fInstructionPointer.getAttribute(IMarker.LINE_NUMBER, -1) - 1 != startLine) {
				return;
			}
		} else if ((lastCharStart != startChar) ||
			     (lastCharEnd != endChar)) {
			return;			     
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
				return "";
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
	 * Updates the enablement of the buttons/actions in the view
	 */
	protected void updateButtons() {
		ISelection s= fViewer.getSelection();
		if (s instanceof IStructuredSelection) {
			IStructuredSelection selection= (IStructuredSelection) s;
			fTerminateAction.selectionChanged(selection);
			fDisconnectAction.selectionChanged(selection);
			fSuspendAction.selectionChanged(selection);
			fResumeAction.selectionChanged(selection);
			fStepIntoAction.selectionChanged(selection);
			fStepOverAction.selectionChanged(selection);
			fStepReturnAction.selectionChanged(selection);
		}
		fRemoveTerminatedAction.update();
		fTerminateAllAction.update();
	}

	/**
	 * Adds items to the context menu.
	 */
	protected void fillContextMenu(IMenuManager menu) {
		fRemoveTerminatedAction.update();
		fTerminateAllAction.update();
		
		menu.add(new Separator(IDebugUIConstants.EMPTY_EDIT_GROUP));
		menu.add(new Separator(IDebugUIConstants.EDIT_GROUP));
		menu.add(fCopyToClipboardAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_STEP_GROUP));
		menu.add(new Separator(IDebugUIConstants.STEP_GROUP));
		menu.add(fStepIntoAction);
		menu.add(fStepOverAction);
		menu.add(fStepReturnAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_THREAD_GROUP));
		menu.add(new Separator(IDebugUIConstants.THREAD_GROUP));
		menu.add(fResumeAction);
		menu.add(fSuspendAction);
		menu.add(fTerminateAction);
		menu.add(fDisconnectAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_LAUNCH_GROUP));
		menu.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		menu.add(fTerminateAndRemoveAction);
		menu.add(fTerminateAllAction);
		menu.add(fRemoveTerminatedAction);
		menu.add(fRelaunchAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(fShowQualifiedAction);
		menu.add(new Separator(IDebugUIConstants.PROPERTY_GROUP));
		fPropertyDialogAction.setEnabled(fPropertyDialogAction.isApplicableForSelection());
		menu.add(fPropertyDialogAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Returns the content provider to use for this viewer 
	 * of this view.
	 */
	protected DebugContentProvider getContentProvider() {
		return new DebugContentProvider();
	}
	
	/**
	 * Auto-expand and select the given element - must be called in UI thread.
	 * This is used to implement auto-expansion-and-select on a SUSPEND event.
	 */
	public void autoExpand(Object element, boolean refreshNeeded) {
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
						children= dt.getChildren();
					} catch (DebugException de) {
						DebugUIUtils.logError(de);
					}
				}
		}
		
		if (refreshNeeded) {
			//ensures that the child item exists in the viewer widget
			//set selection only works if the child exists
			fViewer.refresh(element);
		}
		fViewer.setSelection(new StructuredSelection(selectee), true);
		if (children != null && children.length > 0) {
			//reveal the thread children of a debug target
			fViewer.reveal(children[0]);
		}
	}
	
	/**
	 * Returns the resource bundle prefix for this action
	 */
	protected String getPrefix(){
		return PREFIX;
	}

	/**
	 * @see IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(fEditor)) {
			fEditor = null;
		}
	}
	
	/**
	 * @see IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part == this) {
			showMarkerForCurrentSelection();
		}		
	}	

	/**
	 * @see IViewPart
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site,memento);
		if (memento != null) {
			fEditorMemento = memento.getInteger(DEBUG_EDITOR);
		}
	}
	
	/**
	 * @see IViewPart
	 */
	public void saveState(IMemento memento) {
		if (fEditor != null) {
			IWorkbenchPage page = getSite().getPage();
			if (page != null) {
				IEditorPart[] editors = page.getEditors();
				for (int i = 0; i < editors.length; i++) {
					if (fEditor.equals(editors[i])) {
						memento.putInteger(DEBUG_EDITOR, i);
						break;
					}
				}
			}
		}
	}
}