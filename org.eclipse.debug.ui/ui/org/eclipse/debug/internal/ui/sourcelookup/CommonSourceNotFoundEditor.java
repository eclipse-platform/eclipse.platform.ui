/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.sourcelookup.SourceLookupDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Editor for when source is not found. Shows the source name and has 
 * a button to add new containers.
 * Editor ID: IInternalDebugUIConstants.ID_COMMON_SOURCE_NOT_FOUND_EDITOR = org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditor
 * 
 * May be subclassed if a debugger requires additional buttons on the editor. For example,
 * a button may be added if the user has the additional option of using generated source
 * for debugging.
 * 
 * @see AbstractSourceLookupDirector
 * @see CommonSourceNotFoundEditorInput
 * @since 3.0
 */
public class CommonSourceNotFoundEditor extends EditorPart implements IReusableEditor, IDebugEventSetListener {
	
	/**
	 * Text widgets used for this editor
	 */
	private Text fText;	
    /**
     * object for which the source is showing for (i.e., stackframe, breakpoint)
     */
	protected Object fObject; 
	
	/**
	 * @see org.eclipse.ui.IEditorPart#doSave(IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
	}
	
	/**
	 * @see org.eclipse.ui.IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
	}
	
	/**
	 * @see org.eclipse.ui.IEditorPart#gotoMarker(IMarker)
	 */
	public void gotoMarker(IMarker marker) {
	}
	
	/**
	 * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * @see org.eclipse.ui.IEditorPart#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}
	
	/**
	 * @see org.eclipse.ui.IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		GridLayout topLayout = new GridLayout();
		GridData data = new GridData();	
		topLayout.numColumns = 1;
		topLayout.verticalSpacing = 10;
		parent.setLayout(topLayout);
		parent.setLayoutData(data);		
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		fText = new Text(parent,SWT.READ_ONLY|SWT.WRAP);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        fText.setLayoutData(data);
		fText.setForeground(JFaceColors.getErrorText(fText.getDisplay()));	
		fText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));	
		if (getEditorInput() != null) {
			setInput(getEditorInput());
		}
		
		Button button = new Button(parent, SWT.PUSH);
		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.grabExcessVerticalSpace = false;
		button.setLayoutData(data);
		button.setText(SourceLookupUIMessages.addSourceLocation_addButton2); 
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				buttonSelected();
			}
		});		
		
		Dialog.applyDialogFont(parent);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugHelpContextIds.NO_SOURCE_EDITOR);
	}
	
	/**
	 * Handles the event when the "add source container" button is selected.
	 * Displays the <code>EditSourceLookupPathDialog</code> so the user 
	 * can add additional source containers that should be searched.
	 */
	private void buttonSelected(){
		ISourceLocator locator = null;		
		ILaunch launch = null;		
		IAdaptable selection = DebugUITools.getDebugContext();
		
		if(selection == null) return;
		
		if (selection.getAdapter(ILaunch.class) != null ) {
			launch = (ILaunch) selection.getAdapter(ILaunch.class);
			locator = launch.getSourceLocator();			
		} 
		else if (selection.getAdapter(IDebugElement.class) != null ) {
			launch = ((IDebugElement)selection.getAdapter(IDebugElement.class)).getLaunch();
			locator = launch.getSourceLocator();					
		}
		else return;  //should not occur
		if (locator == null || !(locator instanceof AbstractSourceLookupDirector))
			return; 
		
		final SourceLookupDialog dialog =
			new SourceLookupDialog(DebugUIPlugin.getShell(),(AbstractSourceLookupDirector) locator);
		
		int result = dialog.open();		
		if(result == Window.OK) {
			resetEditor();				
		}
	}
	/**
	 * Clears the (source not found) editor inputs associated with the stack and
	 * opening the editor again, if the item is not a stackframe. A marker will be added
	 * and the editor will be told to scroll to the marker.
	 * 
	 * If it is a stackframe, the launch view will pick up the change event and open a new
	 * editor automatically.
	 * 
	 */
	public void resetEditor()
	{
		fireChangeEventsOnStack();
		
		if(fObject instanceof IStackFrame) {
			return; //launch view will pick up from here
		}

		// close this editor and open a new editor
		final int lineNumber = getLineNumber();
		Runnable open = new Runnable() {
			public void run() {
				IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
				if (dwindow != null) {
					IWorkbenchPage page= dwindow.getActivePage();
					if (page != null) {
						IDebugModelPresentation modelPres = DebugUITools.newDebugModelPresentation();	
						IEditorInput input = modelPres.getEditorInput(fObject);
						if (input != null) {
							String id = modelPres.getEditorId(input, fObject);
							if (id != null) {
								try {
									IEditorPart editorPart = page.openEditor(input, id);
									if (editorPart instanceof ITextEditor && lineNumber >= 0) {
										// position to line number
										ITextEditor textEditor = (ITextEditor) editorPart;
										IRegion region= getLineInformation(textEditor, lineNumber);
										if (region != null) {
											textEditor.selectAndReveal(region.getOffset(), 0);
										}
									}
								} catch (PartInitException e1) {
								}		
							}
						}
						modelPres.dispose();
					}
				}
			}
		};
		closeEditor();	
		// get new editor input
		DebugUIPlugin.getStandardDisplay().asyncExec(open);
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
	 * Returns the line number associated with the breakpoint (marker).
	 * @return the line number to scroll to
	 */
	protected int getLineNumber(){
		int line = -1;
		if(fObject instanceof IMarker) {
			try{
				line=((Integer)((IMarker)fObject).getAttribute(IMarker.LINE_NUMBER)).intValue();							
			} catch(CoreException e){}
		}
		return line;
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (fText != null) {
			fText.setFocus();
		}
	}
	
	/**
	 * @see IReusableEditor#setInput(org.eclipse.ui.IEditorInput)
	 */
	public void setInput(IEditorInput input) {
		super.setInput(input);
		if(input instanceof CommonSourceNotFoundEditorInput) {
			fObject = ((CommonSourceNotFoundEditorInput)input).getObject();
		}
		setPartName(input.getName());
		if (fText != null) {			
			fText.setText(input.getToolTipText()+"\n"); //$NON-NLS-1$
		}
	}

	/**
	 * Fires change event(s) to clear the source file history of the items in the stack.
	 */
	protected void fireChangeEventsOnStack(){
		if(fObject instanceof IStackFrame) {
			fireChangeEvent(DebugEvent.CONTENT, (IStackFrame)fObject);				
		} else if(fObject instanceof IDebugElement) { //loop through all threads and clear the cached source files
			try{		
				IThread[] threads =((IDebugElement)fObject).getDebugTarget().getThreads();
				for(int i=0; i< threads.length; i++)
				{
					fireChangeEvent(DebugEvent.CONTENT, threads[i].getTopStackFrame());
				}
			} catch(DebugException e){}
		}			
	}
	
	/**
	 * Fire a debug change event with detail
	 * @param detail @see DebugEvent
	 */
	public void fireChangeEvent(int detail, IDebugElement source) {
		fireEvent(new DebugEvent(source, DebugEvent.CHANGE, detail));
	}		
	
	
	/**
	 * Fire a debug event
	 */
	private void fireEvent(DebugEvent event) {
		if(DebugPlugin.getDefault() != null)
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event});
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			Object source= event.getSource();			
			switch (event.getKind()) {
			case DebugEvent.TERMINATE :
				if(checkIfEditorShouldClose(source))
					closeEditor();			 
				break;
				
			case DebugEvent.CHANGE :
				if(!source.equals(fObject))
					return;
				//	Trigger a selectionChange event
				IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
				if (window == null) {
					return;
				}
				IWorkbenchPage p= window.getActivePage();
				if (p == null) {
					return;
				}
				IViewPart fLaunchView= p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
				if (fLaunchView instanceof ISelectionChangedListener) {						
					ISelection fSelection = ((LaunchView)fLaunchView).getViewer().getSelection();
					//	To clear the stackframe stored in the launchView
					((LaunchView)fLaunchView).clearSourceSelection(((IStackFrame)source).getThread());
					((LaunchView)fLaunchView).getViewer().setSelection(fSelection, true);					
				}			
				break;
			}					
		}
	}
	
	
	/**
	 * Checks if the source of the terminate event is associated with this editor
	 * object.
	 * @param source the source of the event
	 * @return true if the <code>source</code> is related to this editor, false otherwise
	 */
	protected boolean checkIfEditorShouldClose(Object source) {
		//Make sure terminate event is for me
		if (fObject instanceof IDebugElement && source instanceof IDebugElement) {
			IDebugElement element = (IDebugElement)fObject;
			IDebugElement sourceElement = (IDebugElement)source;
			return sourceElement.equals(element.getDebugTarget());
		}
		return false;
	}
	
	/**
	 * Closes this editor.
	 */
	protected void closeEditor()
	{
		final IEditorPart editor = this;
		DebugUIPlugin.getStandardDisplay().syncExec(
				new Runnable() {
					public void run() {											
						IWorkbenchWindow activeWorkbenchWindow = DebugUIPlugin.getActiveWorkbenchWindow();
						if (activeWorkbenchWindow != null) {
							IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
							if (activePage != null) {
								activePage.closeEditor(editor,false);
							}
						}										
					}						
				});			
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		super.dispose();
	}
	
	
}


