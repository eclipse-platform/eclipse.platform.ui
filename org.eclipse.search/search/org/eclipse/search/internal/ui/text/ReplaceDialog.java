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
package org.eclipse.search.internal.ui.text;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchResultView;
import org.eclipse.search.internal.ui.SearchResultViewEntry;
import org.eclipse.search.internal.ui.SearchResultViewer;
import org.eclipse.search.internal.ui.util.ExtendedDialogWindow;

class ReplaceDialog extends ExtendedDialogWindow {
		
	/**
	 * A class wrapping a resource marker, adding a position.
	 */
	private static class ReplaceMarker {
		private Position fPosition;
		private IMarker fMarker;
		
		ReplaceMarker(IMarker marker) {
			fMarker= marker;
		}
		
		public IFile getFile() {
			return (IFile)fMarker.getResource();
		}
		
		public void deletePosition(IDocument doc) {
			if (fPosition != null) {
				MarkerUtilities.setCharStart(fMarker, fPosition.getOffset());
				MarkerUtilities.setCharEnd(fMarker, fPosition.getOffset()+fPosition.getLength());
				doc.removePosition(fPosition);
				fPosition= null;
			}
		}
		
		public void delete() throws CoreException {
			fMarker.delete();
		}
		
		public void createPosition(IDocument doc) throws BadLocationException {
			if (fPosition == null) {
				int charStart= MarkerUtilities.getCharStart(fMarker);
				fPosition= new Position(charStart, MarkerUtilities.getCharEnd(fMarker)-charStart);
				doc.addPosition(fPosition);
			}
		}
		
		public int getLength() {
			if (fPosition != null)
				return fPosition.getLength();
			return MarkerUtilities.getCharEnd(fMarker)-MarkerUtilities.getCharStart(fMarker);
		}
		
		public int getOffset() {
			if (fPosition != null)
				return fPosition.getOffset();
			return MarkerUtilities.getCharStart(fMarker);
		}
	}
	
	private abstract static class ReplaceOperation extends WorkspaceModifyOperation {
		public void execute(IProgressMonitor monitor) throws InvocationTargetException {
			try {
				doReplace(monitor);
			} catch (BadLocationException e) {
				throw new InvocationTargetException(e);
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			} catch (IOException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		protected abstract void doReplace(IProgressMonitor pm) throws BadLocationException, CoreException, IOException;
	}
		
	// various widget related constants
	private static final int REPLACE= IDialogConstants.CLIENT_ID + 1;
	private static final int REPLACE_ALL_IN_FILE= IDialogConstants.CLIENT_ID + 2;
	private static final int REPLACE_ALL= IDialogConstants.CLIENT_ID + 3;
	private static final int SKIP= IDialogConstants.CLIENT_ID + 4;
	private static final int SKIP_FILE= IDialogConstants.CLIENT_ID + 5;
	private static final int SKIP_ALL= IDialogConstants.CLIENT_ID + 6;
	
	// Widgets
	private Text fTextField;
	private Button fReplaceButton;
	private Button fReplaceAllInFileButton;
	private Button fReplaceAllButton;
	private Button fSkipButton;
	private Button fSkipFileButton;
	
	private List fMarkers;
	private TextSearchOperation fOperation;
	private boolean fSkipReadonly= false;
	
	// reuse editors stuff
	private IReusableEditor fEditor;
	
	protected ReplaceDialog(Shell parentShell, List entries, TextSearchOperation operation) {
		super(parentShell);
		Assert.isNotNull(entries);
		Assert.isNotNull(operation);
		fMarkers= new ArrayList();
		initializeMarkers(entries);
		fOperation= operation;
	}
	
	private void initializeMarkers(List entries) {
		for (Iterator elements= entries.iterator(); elements.hasNext(); ) {
			SearchResultViewEntry element= (SearchResultViewEntry)elements.next();
			List markerList= element.getMarkers();
			for (Iterator markers= markerList.iterator(); markers.hasNext(); ) {
				IMarker marker= (IMarker)markers.next();
				int charStart= MarkerUtilities.getCharStart(marker); 
				if (charStart >= 0 && MarkerUtilities.getCharEnd(marker) > charStart)
					fMarkers.add(new ReplaceMarker(marker));
			}
		}
	}
	
	// widget related stuff -----------------------------------------------------------
	public void create() {
		super.create();
		Shell shell= getShell();
		shell.setText(getDialogTitle());
		gotoCurrentMarker();
		enableButtons();
	}
		
	protected Control createPageArea(Composite parent) {
		Composite result= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		result.setLayout(layout);
		layout.numColumns= 2;
		
		layout.marginHeight =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		
		initializeDialogUnits(result);
		
		Label label= new Label(result, SWT.NONE);
		label.setText(SearchMessages.getString("ReplaceDialog.replace_label")); //$NON-NLS-1$
		Text clabel= new Text(result, SWT.BORDER);
		clabel.setEnabled(false);
		clabel.setText(fOperation.getPattern());
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
		
		
		new Label(result, SWT.NONE);
		Button replaceWithRegex= new Button(result, SWT.CHECK);
		replaceWithRegex.setText(SearchMessages.getString("ReplaceDialog.isRegex.label")); //$NON-NLS-1$
		replaceWithRegex.setEnabled(false);
		replaceWithRegex.setSelection(false);
		
		applyDialogFont(result);
		return result;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		fReplaceButton= createButton(parent, REPLACE, SearchMessages.getString("ReplaceDialog.replace"), true); //$NON-NLS-1$
		fReplaceAllInFileButton= createButton(parent, REPLACE_ALL_IN_FILE, SearchMessages.getString("ReplaceDialog.replaceAllInFile"), false); //$NON-NLS-1$

		Label filler= new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		fReplaceAllButton= createButton(parent, REPLACE_ALL, SearchMessages.getString("ReplaceDialog.replaceAll"), false); //$NON-NLS-1$
		fSkipButton= createButton(parent, SKIP, SearchMessages.getString("ReplaceDialog.skip"), false); //$NON-NLS-1$
		fSkipFileButton= createButton(parent, SKIP_FILE, SearchMessages.getString("ReplaceDialog.skipFile"), false); //$NON-NLS-1$

		filler= new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		super.createButtonsForButtonBar(parent);
		((GridLayout)parent.getLayout()).numColumns= 4;
	}
	
	protected Point getInitialLocation(Point initialSize) {
		SearchResultView view= (SearchResultView)SearchPlugin.getSearchResultView();
		if (view == null)
			return super.getInitialLocation(initialSize);
		Point result= new Point(0, 0);
		Control control= view.getViewer().getControl();
		Point size= control.getSize();
		Point location= control.toDisplay(control.getLocation());
		result.x= Math.max(0, location.x + size.x - initialSize.x);
		result.y= Math.max(0, location.y + size.y - initialSize.y);
		return result;
	}
	
	private void enableButtons() {
		fSkipButton.setEnabled(hasNextMarker());
		fSkipFileButton.setEnabled(hasNextFile());
		fReplaceButton.setEnabled(canReplace());
		fReplaceAllInFileButton.setEnabled(canReplace());
		fReplaceAllButton.setEnabled(canReplace());
	}
	
	protected void buttonPressed(int buttonId) {
		final String replaceText= fTextField.getText();
		try {
			switch (buttonId) {
				case SKIP :
					skip();
					break;
				case SKIP_FILE :
					skipFile();
					break;
				case REPLACE :
					run(false, true, new ReplaceOperation() {
					protected void doReplace(IProgressMonitor pm) throws BadLocationException, CoreException {
						replace(pm, replaceText);
					}
				});
					gotoCurrentMarker();
					break;
				case REPLACE_ALL_IN_FILE :
					run(false, true, new ReplaceOperation() {
					protected void doReplace(IProgressMonitor pm) throws BadLocationException, CoreException {
						replaceInFile(pm, replaceText);
						
					}
				});
					gotoCurrentMarker();
					break;
				case REPLACE_ALL :
					run(false, true, new ReplaceOperation() {
					protected void doReplace(IProgressMonitor pm) throws BadLocationException, CoreException {
						replaceAll(pm, replaceText);
					}
				});
					gotoCurrentMarker();
					break;
				default :
					{
					super.buttonPressed(buttonId);
					return;
				}
			}
		} catch (InvocationTargetException e) {
			SearchPlugin.log(e);
			String message= SearchMessages.getFormattedString("ReplaceDialog.error.unable_to_replace", getCurrentMarker().getFile().getName()); //$NON-NLS-1$
			MessageDialog.openError(getParentShell(), getDialogTitle(), message);
		} catch (InterruptedException e) {
			// means operation canceled
		}
		if (!hasNextMarker() && !hasNextFile() && !canReplace())
			close();
		else {
			enableButtons();
		}
	}
	
	private ReplaceMarker getCurrentMarker() {
		return (ReplaceMarker)fMarkers.get(0);
	}
	
	private void replace(IProgressMonitor pm, String replacementText) throws BadLocationException, CoreException {
		ReplaceMarker marker= getCurrentMarker();
		pm.beginTask(SearchMessages.getString("ReplaceDialog.task.replace"), 10); //$NON-NLS-1$
		replaceInFile(pm, marker.getFile(), replacementText, new ReplaceMarker[]{marker});
	}
	
	private void replaceInFile(IProgressMonitor pm, String replacementText) throws BadLocationException, CoreException {
		ReplaceMarker firstMarker= getCurrentMarker();
		ReplaceMarker[] markers= collectMarkers(firstMarker.getFile());
		pm.beginTask(SearchMessages.getFormattedString("ReplaceDialog.task.replaceInFile", firstMarker.getFile().getFullPath().toOSString()), 4); //$NON-NLS-1$
		replaceInFile(pm, firstMarker.getFile(), replacementText, markers);
	}
	
	private void replaceAll(IProgressMonitor pm, String replacementText) throws BadLocationException, CoreException {
		int resourceCount= countResources();
		pm.beginTask(SearchMessages.getString("ReplaceDialog.task.replace.replaceAll"), resourceCount); //$NON-NLS-1$
		while (fMarkers.size() > 0) {
			replaceInFile(new SubProgressMonitor(pm, 1, 0), replacementText);
		}
		pm.done();
	}
	
	private void replaceInFile(final IProgressMonitor pm, final IFile file, final String replacementText, final ReplaceMarker[] markers) throws BadLocationException, CoreException {
		if (pm.isCanceled())
			throw new OperationCanceledException();
		doReplaceInFile(pm, file, replacementText, markers);
	}
	
	private void doReplaceInFile(IProgressMonitor pm, IFile file, String replacementText, final ReplaceMarker[] markers) throws BadLocationException, CoreException {
		try {
			if (file.isReadOnly()) {
				file.getWorkspace().validateEdit(new IFile[]{file}, null);
			}
			if (file.isReadOnly()) {
				if (fSkipReadonly) {
					skipFile();
					return;
				}
				int rc= askForSkip(file);
				switch (rc) {
					case CANCEL :
						throw new OperationCanceledException();
					case SKIP_FILE :
						skipFile();
						return;
					case SKIP_ALL :
						fSkipReadonly= true;
						skipFile();
						return;
				}
			}
			ITextFileBufferManager bm= FileBuffers.getTextFileBufferManager();
			try {
				bm.connect(file.getFullPath(), new SubProgressMonitor(pm, 1));
				ITextFileBuffer fb= bm.getTextFileBuffer(file.getFullPath());
				boolean wasDirty= fb.isDirty();
				IDocument doc= fb.getDocument();
				try {
					createPositionsInFile(file, doc);
					for (int i= 0; i < markers.length; i++) {
						doc.replace(markers[i].getOffset(), markers[i].getLength(), replacementText);
						fMarkers.remove(0);
						markers[i].delete();
					}
				} finally {
					removePositonsInFile(file, doc);
				}
				if (!wasDirty)
					fb.commit(new SubProgressMonitor(pm, 1), true);
			} finally {
				bm.disconnect(file.getFullPath(), new SubProgressMonitor(pm, 1));
			}
		} finally {
			pm.done();
		}
	}
	
	private void removePositonsInFile(IFile file, IDocument doc) {
		for (Iterator markers= fMarkers.iterator(); markers.hasNext(); ) {
			ReplaceMarker marker= (ReplaceMarker)markers.next();
			if (!marker.getFile().equals(file))
				return;
			marker.deletePosition(doc);
		}
	}
	
	private void createPositionsInFile(IFile file, IDocument doc) throws BadLocationException {
		for (Iterator markers= fMarkers.iterator(); markers.hasNext(); ) {
			ReplaceMarker marker= (ReplaceMarker)markers.next();
			if (!marker.getFile().equals(file))
				return;
			marker.createPosition(doc);
		}
	}
	
	private int askForSkip(final IFile file) {
		
		String message= SearchMessages.getFormattedString("ReadOnlyDialog.message", file.getFullPath().toOSString()); //$NON-NLS-1$
		String[] buttonLabels= null;
		boolean showSkip= countResources() > 1;
		if (showSkip) {
			String skipLabel= SearchMessages.getString("ReadOnlyDialog.skipFile"); //$NON-NLS-1$
			String skipAllLabel= SearchMessages.getString("ReadOnlyDialog.skipAll"); //$NON-NLS-1$
			buttonLabels= new String[]{skipLabel, skipAllLabel, IDialogConstants.CANCEL_LABEL};
		} else {
			buttonLabels= new String[]{IDialogConstants.CANCEL_LABEL};
			
		}
		
		MessageDialog msd= new MessageDialog(getShell(), getShell().getText(), null, message, MessageDialog.ERROR, buttonLabels, 0);
		int rc= msd.open();
		switch (rc) {
			case 0 :
				return showSkip ? SKIP_FILE : CANCEL;
			case 1 :
				return SKIP_ALL;
			default :
				return CANCEL;
		}
	}
		
	private String getDialogTitle() {
		return SearchMessages.getString("ReplaceDialog.dialog.title"); //$NON-NLS-1$
	}
	
	private void skip() {
		fMarkers.remove(0);
		Assert.isTrue(fMarkers.size() > 0);
		gotoCurrentMarker();
	}
	
	private void skipFile() {
		ReplaceMarker currentMarker= getCurrentMarker();
		if (currentMarker == null)
			return;
		IResource currentFile= currentMarker.getFile();
		while (fMarkers.size() > 0 && getCurrentMarker().getFile().equals(currentFile))
			fMarkers.remove(0);
		gotoCurrentMarker();
	}
	
	private void gotoCurrentMarker() {
		if (fMarkers.size() > 0) {
			ReplaceMarker marker= getCurrentMarker();
			Control focusControl= getShell().getDisplay().getFocusControl();
			try {
				selectEntry(marker);
				ITextEditor editor= null;
				if (SearchUI.reuseEditor())
					editor= openEditorReuse(marker);
				else
					editor= openEditorNoReuse(marker);
				editor.selectAndReveal(marker.getOffset(), marker.getLength());
				if (focusControl != null && !focusControl.isDisposed())
					focusControl.setFocus();
			} catch (PartInitException e) {
				String message= SearchMessages.getFormattedString("ReplaceDialog.error.unable_to_open_text_editor", marker.getFile().getName()); //$NON-NLS-1$
				MessageDialog.openError(getParentShell(), getDialogTitle(), message);
			}
		}
	}
	
	private void selectEntry(ReplaceMarker marker) {
		SearchResultView view= (SearchResultView) SearchPlugin.getSearchResultView();
		if (view == null)
			return;
		SearchResultViewer viewer= view.getViewer();
		if (viewer == null)
			return;
		ISelection sel= viewer.getSelection();
		if (!(sel instanceof IStructuredSelection))
			return;
		IStructuredSelection ss= (IStructuredSelection) sel;
		IFile file= marker.getFile();
		if (ss.size() == 1 && file.equals(ss.getFirstElement()))
			return;
		Table table= viewer.getTable();
		if (table == null || table.isDisposed())
			return;
		int selectionIndex= table.getSelectionIndex();
		if (selectionIndex < 0)
			selectionIndex= 0;
		for (int i= 0; i < table.getItemCount(); i++) {
			int currentTableIndex= (selectionIndex+i) % table.getItemCount();
			SearchResultViewEntry entry= (SearchResultViewEntry) viewer.getElementAt(currentTableIndex);
			if (file.equals(entry.getGroupByKey())) {
				viewer.setSelection(new StructuredSelection(entry));
				return;
			}
		}
	}

	// opening editors ------------------------------------------
	private ITextEditor openEditorNoReuse(ReplaceMarker marker) throws PartInitException {
		IFile file= marker.getFile();
		IWorkbenchPage activePage= SearchPlugin.getActivePage();
		if (activePage == null)
			return null;
		ITextEditor textEditor= showOpenTextEditor(activePage, file);
		if (textEditor != null)
			return textEditor;
		return openNewTextEditor(file, activePage);
	}
	
	private ITextEditor openNewTextEditor(IFile file, IWorkbenchPage activePage) throws PartInitException {
		IEditorDescriptor desc= IDE.getDefaultEditor(file);
		if (desc != null) {
			String editorID= desc.getId();
			IEditorPart editor;
			if (desc.isInternal()) {
				editor= activePage.openEditor(new FileEditorInput(file), editorID);
				if (editor instanceof ITextEditor) {
					if (editor instanceof IReusableEditor)
						fEditor= (IReusableEditor) editor;
					return (ITextEditor)editor;
				} else
					activePage.closeEditor(editor, false);
			}
		}
		IEditorPart editor= activePage.openEditor(new FileEditorInput(file), "org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
		return (ITextEditor)editor;
	}

	private ITextEditor openEditorReuse(ReplaceMarker marker) throws PartInitException {
		IWorkbenchPage page= SearchPlugin.getActivePage();
		IFile file= marker.getFile();
		if (page == null)
			return null;

		ITextEditor textEditor= showOpenTextEditor(page, file);
		if (textEditor != null)
			return textEditor;

		String editorId= null;
		IEditorDescriptor desc= IDE.getDefaultEditor(file);
		if (desc != null && desc.isInternal())
			editorId= desc.getId();

		boolean isOpen= isEditorOpen(page, fEditor);

		boolean canBeReused= isOpen && !fEditor.isDirty() && !isPinned(fEditor);
		boolean showsSameInputType= fEditor != null && (editorId == null || fEditor.getSite().getId().equals(editorId));

		if (canBeReused) {
			if (showsSameInputType) {
				fEditor.setInput(new FileEditorInput(file));
				page.bringToTop(fEditor);
				return (ITextEditor) fEditor;
			} else {
				page.closeEditor(fEditor, false);
				fEditor= null;
			}
		}
		return openNewTextEditor(file, page);
	}

	private boolean isEditorOpen(IWorkbenchPage page, IEditorPart editor) {
		if (editor != null) {
			IEditorReference[] parts= page.getEditorReferences();
			int i= 0;
			for (int j = 0; j < parts.length; j++) {
				if (editor == parts[i++].getEditor(false))
					return true;
			}
		}
		return false;
	}

	private ITextEditor showOpenTextEditor(IWorkbenchPage page, IFile file) {
		IEditorPart editor= page.findEditor(new FileEditorInput(file));
		if (editor instanceof ITextEditor) {
			page.bringToTop(editor);
			return (ITextEditor) editor;
		}
		return null;
	}

	private boolean isPinned(IEditorPart editor) {
		if (editor == null)
			return false;
		
		IEditorReference[] editorRefs= editor.getEditorSite().getPage().getEditorReferences();
		int i= 0;
		while (i < editorRefs.length) {
			if (editor.equals(editorRefs[i].getEditor(false)))
				return editorRefs[i].isPinned();
			i++;
		}
		return false;
	}
	
	// resource related  -------------------------------------------------------------
	/**
	 * @return the number of resources referred to in fMarkers
	 */
	private int countResources() {
		IResource r= null;
		int count= 0;
		for (Iterator elements= fMarkers.iterator(); elements.hasNext(); ) {
			ReplaceMarker element= (ReplaceMarker)elements.next();
			if (!element.getFile().equals(r)) {
				count++;
				r= element.getFile();
			}
		}
		return count;
	}
	
	private ReplaceMarker[] collectMarkers(IResource resource) {
		List matching= new ArrayList();
		for (int i= 0; i < fMarkers.size(); i++) {
			ReplaceMarker marker= (ReplaceMarker)fMarkers.get(i);
			if (!marker.getFile().equals(resource))
				break;
			matching.add(marker);
		}
		ReplaceMarker[] markers= new ReplaceMarker[matching.size()];
		return (ReplaceMarker[])matching.toArray(markers);
	}
	
	
	// some queries -------------------------------------------------------------
	private boolean hasNextMarker() {
		return fMarkers.size() > 1;
	}
	
	private boolean hasNextFile() {
		if (!hasNextMarker())
			return false;
		IResource currentFile= getCurrentMarker().getFile();
		for (int i= 0; i < fMarkers.size(); i++) {
			if (!((ReplaceMarker)fMarkers.get(i)).getFile().equals(currentFile))
				return true;
		}
		return false;
	}
	
	private boolean canReplace() {
		return fMarkers.size() > 0;
	}
}
