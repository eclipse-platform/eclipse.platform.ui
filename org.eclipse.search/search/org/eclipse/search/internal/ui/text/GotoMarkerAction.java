/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;

class GotoMarkerAction extends Action {

	private IEditorPart fEditor;

	public void run() {
		ISearchResultView view= SearchUI.getSearchResultView();		
		ISelection selection= view.getSelection();
		Object element= null;
		if (selection instanceof IStructuredSelection)
			element= ((IStructuredSelection)selection).getFirstElement();
		if (element instanceof ISearchResultViewEntry) {
			ISearchResultViewEntry entry= (ISearchResultViewEntry)element;
			show(entry.getSelectedMarker());
		}
	}

	private void show(IMarker marker) {
		if (SearchUI.reuseEditor())
			showWithReuse(marker);
		else
			showWithoutReuse(marker);
	}

	private void showWithReuse(IMarker marker) {
		IWorkbenchPage page= SearchPlugin.getActivePage();
		IResource resource= marker.getResource();
		if (page == null || !(resource instanceof IFile))
			return;
		
		IEditorInput input= new FileEditorInput((IFile)resource);
		String editorId= null;
		IEditorDescriptor desc= SearchPlugin.getDefault().getWorkbench().getEditorRegistry().getDefaultEditor((IFile)resource);
		if (desc == null)
			editorId= SearchPlugin.getDefault().getWorkbench().getEditorRegistry().getDefaultEditor().getId();
		else
			editorId= desc.getId();

		IEditorPart editor= page.findEditor(input);
		if (editor == null) {
				if (fEditor != null && !fEditor.isDirty())
					page.closeEditor(fEditor, false);
			try {
				editor= page.openEditor(input, editorId, false);
			} catch (PartInitException ex) {
				ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.openEditor.title"), SearchMessages.getString("Search.Error.openEditor.message")); //$NON-NLS-2$ //$NON-NLS-1$
				return;
			}

		} else {
			page.bringToTop(editor);
		}
		if (editor != null) {
			editor.gotoMarker(marker);
			fEditor= editor;
		}
	}
	
	private void showWithoutReuse(IMarker marker) {
		IWorkbenchPage page= SearchPlugin.getActivePage();
		if (page == null)
			return;

		try {
			page.openEditor(marker, false);
		} catch (PartInitException ex) {
			ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.openEditor.title"), SearchMessages.getString("Search.Error.openEditor.message")); //$NON-NLS-2$ //$NON-NLS-1$
			return;
		}
	}
}