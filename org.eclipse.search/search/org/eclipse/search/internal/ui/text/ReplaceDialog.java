/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.ISearchResultView;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchResultView;
import org.eclipse.search.internal.ui.SearchResultViewEntry;
import org.eclipse.search.internal.ui.util.ExceptionHandler;

public class ReplaceDialog extends Dialog {

	private static final int REPLACE_NEXT= IDialogConstants.CLIENT_ID;
	private static final int REPLACE= IDialogConstants.CLIENT_ID + 1;
	private static final int NEXT= IDialogConstants.CLIENT_ID + 2;

	private String fSearchPattern;

	private IWorkbenchWindow fWindow;
	private boolean fAutobuild;
	private boolean fFatalError;
	
	// UI
	private Text fTextField;
	private Button fSaveButton;
	private Button fReplaceNextButton;
	private Button fReplaceButton;
	private Button fNextButton;

		
	private boolean fSaved;
	private ITextEditor fEditor;
	private boolean fCloseEditor;
	private IDocument fDocument;
	private AbstractMarkerAnnotationModel fAnnotationModel;

	
	private List fElements;
	private int fElementIndex;
	private SearchResultViewEntry fCurrentEntry;
	private List fCurrentMarkers;
	private int fMarkerIndex;
	private IMarker fCurrentMatch;
	
	protected ReplaceDialog(Shell parentShell, List elements, IWorkbenchWindow window, String searchPattern) {
		super(parentShell);
		Assert.isNotNull(elements);
		Assert.isNotNull(searchPattern);
		fElements= new ArrayList(elements);
		Assert.isNotNull(window);
		fWindow= window;
		fSearchPattern= searchPattern;
	}

	public void create() {
		super.create();
		Shell shell= getShell();
		shell.setText(getDialogTitle());
		updateButtons();
	}

	public int open() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		fAutobuild= workspace.isAutoBuilding();
		if (fAutobuild) {
			IWorkspaceDescription description= workspace.getDescription();
			description.setAutoBuilding(false);
			try {
				workspace.setDescription(description);
			} catch (CoreException e) {
				ExceptionHandler.handle(e, getShell(), getDialogTitle(), SearchMessages.getString("ReplaceDialog.error.auto_building")); //$NON-NLS-1$
				fFatalError= true;
			}
		}
		try {
			fCurrentMatch= getNextMatch(false);
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), getDialogTitle(), SearchMessages.getString("ReplaceDialog.error.no_matches")); //$NON-NLS-1$
			fFatalError= true;
		}
		return super.open();
	}

	public boolean close() {
		boolean result= super.close();
		restoreAutoBuildState();
		return result;
	}

	protected Control createDialogArea(Composite parent) {
		Composite result= (Composite)super.createDialogArea(parent);
		GridLayout layout= (GridLayout)result.getLayout();
		layout.numColumns= 2;
		
		initializeDialogUnits(result);
		
		Label label= new Label(result, SWT.NONE);
		label.setText(SearchMessages.getString("ReplaceDialog.replace_label")); //$NON-NLS-1$
		
		CLabel clabel= new CLabel(result, SWT.NONE);
		clabel.setText(fSearchPattern);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(50);
		clabel.setLayoutData(gd);
		
		label= new Label(result, SWT.NONE);
		label.setText(SearchMessages.getString("ReplaceDialog.with_label")); //$NON-NLS-1$
		
		fTextField= new Text(result, SWT.BORDER);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(50);
		fTextField.setLayoutData(gd);
		fTextField.setFocus();
		
		fSaveButton= new Button(result, SWT.CHECK);
		fSaveButton.setText(SearchMessages.getString("ReplaceDialog.save_changes")); //$NON-NLS-1$
		fSaveButton.setSelection(true);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		fSaveButton.setLayoutData(gd);
		
		return result;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		fReplaceNextButton= createButton(parent, REPLACE_NEXT, SearchMessages.getString("ReplaceDialog.replace_next"), false); //$NON-NLS-1$
		fReplaceButton= createButton(parent, REPLACE, SearchMessages.getString("ReplaceDialog.replace"), false); //$NON-NLS-1$
		fNextButton= createButton(parent, NEXT, SearchMessages.getString("ReplaceDialog.next"), false); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID, SearchMessages.getString("ReplaceDialog.close"), false); //$NON-NLS-1$
	}

	protected Point getInitialLocation(Point initialSize) {
		SearchResultView view= (SearchResultView)SearchPlugin.getSearchResultView();
		if (view == null)
			return super.getInitialLocation(initialSize);
		Point result= new Point(0,0);
		Control control= view.getViewer().getControl();
		Point size= control.getSize();
		Point location= control.toDisplay(control.getLocation());
		result.x= Math.max(0, location.x + size.x - initialSize.x);
		result.y= Math.max(0, location.y + size. y - initialSize.y);
		return result;
	}

	protected void buttonPressed(int buttonId) {
		try {
			boolean save= fSaveButton.getSelection();
			String text= fTextField.getText();
			switch(buttonId) {
				case REPLACE_NEXT:
					replace(fCurrentMatch, text, save);
					fCurrentMatch= getNextMatch(save);
					break;
				case REPLACE:
					replace(fCurrentMatch, text, save);
					fCurrentMatch= null;
					break;
				case NEXT:
					fCurrentMatch= getNextMatch(save);
					break;
				case IDialogConstants.CANCEL_ID:
					saveEditor(save);
					break;
			}
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), getDialogTitle(), SearchMessages.getString("ReplaceDialog.error.unexpected_exception")); //$NON-NLS-1$
			fFatalError= true;
		} catch (BadLocationException e) {
			MessageDialog.openError(getShell(), getDialogTitle(), SearchMessages.getString("ReplaceDialog.error.different_content")); //$NON-NLS-1$
			fFatalError= true;
		}
		updateButtons();
		super.buttonPressed(buttonId);
	}
	
	private void replace(IMarker source, String text, boolean save) throws CoreException, BadLocationException {
		Position position= fAnnotationModel.getMarkerPosition(source);
		fDocument.replace(position.getOffset(), position.getLength(), text);
		SearchPlugin.getWorkspace().deleteMarkers(new IMarker[] {source});
	}
	
	private boolean isLastMatch() {
		return fCurrentMatch != null && fCurrentMarkers == null;
	}
	
	private boolean hasNextMatch() {
		if (fCurrentMarkers != null)
			return true;
		return fElementIndex < fElements.size();
	}
	
	private IMarker getNextMatch(boolean save) throws CoreException {
		if (fCurrentMarkers == null) {
			if (fElementIndex >= fElements.size())
				return null;
			saveEditor(save);
			fCurrentEntry= (SearchResultViewEntry)fElements.get(fElementIndex++);
			fCurrentMarkers= new ArrayList(fCurrentEntry.getMarkers());
			fMarkerIndex= 0;
		}
		IMarker result= (IMarker)fCurrentMarkers.get(fMarkerIndex);
		if (fEditor == null) {
			IWorkbenchPage activePage = fWindow.getActivePage();
			int openEditors= activePage.getEditorReferences().length;
			fEditor= (ITextEditor)activePage.openEditor(result, false);
			IDocumentProvider provider= fEditor.getDocumentProvider();
			IEditorInput input = fEditor.getEditorInput();
			fDocument= provider.getDocument(input);
			fAnnotationModel= (AbstractMarkerAnnotationModel)provider.getAnnotationModel(input);
			fCloseEditor= openEditors < activePage.getEditorReferences().length;
		} else {
			fEditor.gotoMarker(result);
		}
		if (fMarkerIndex == fCurrentMarkers.size() - 1) {
			fCurrentMarkers= null;
		} else {
			fMarkerIndex++;
		}
		return result;
	}

	private void saveEditor(boolean save) throws CoreException {
		if (fEditor != null)
			save= save && fEditor.isDirty();
		if (save) {
			IDocumentProvider provider= fEditor.getDocumentProvider();
			IEditorInput input = fEditor.getEditorInput();
			try {
				provider.aboutToChange(input);
				provider.saveDocument(new NullProgressMonitor(), input, fDocument, true);
				fSaved= true;
			} finally {
				provider.changed(input);
			}
		}
		if (fEditor != null && fCloseEditor && save)
			fEditor.close(false);
		fEditor= null;
		fDocument= null;
		fAnnotationModel= null;
		fCloseEditor= false;
		
	}
	
	private void updateButtons() {
		boolean hasNext= hasNextMatch();
		fReplaceNextButton.setEnabled(!fFatalError && fCurrentMatch != null);
		fReplaceButton.setEnabled(!fFatalError && fCurrentMatch != null);
		fNextButton.setEnabled(!fFatalError && hasNext);
	}
	
	private void restoreAutoBuildState() {
		if (!fAutobuild)
			return;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description= workspace.getDescription();
		description.setAutoBuilding(fAutobuild);
		try {
			workspace.setDescription(description);
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), getDialogTitle(), SearchMessages.getString("ReplaceDialog.error.reenable_auto_build_failed")); //$NON-NLS-1$
			return;
		}
		
		ISearchResultView view= SearchPlugin.getSearchResultView();
		if (fSaved && view != null) {
			new GlobalBuildAction(
				view.getSite().getWorkbenchWindow(),
				IncrementalProjectBuilder.INCREMENTAL_BUILD).run();
		}
	}
	
	private String getDialogTitle() {
		return SearchMessages.getString("ReplaceDialog.dialog.title"); //$NON-NLS-1$
	}
}
