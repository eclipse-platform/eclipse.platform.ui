/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IFile;import org.eclipse.core.resources.IMarker;import org.eclipse.core.resources.IResource;import org.eclipse.jface.action.Action;import org.eclipse.jface.viewers.ISelection;import org.eclipse.jface.viewers.IStructuredSelection;import org.eclipse.ui.IEditorInput;import org.eclipse.ui.IEditorPart;import org.eclipse.ui.IWorkbenchPage;import org.eclipse.ui.PartInitException;import org.eclipse.ui.part.FileEditorInput;import org.eclipse.search.internal.ui.SearchPlugin;import org.eclipse.search.internal.ui.util.ExceptionHandler;import org.eclipse.search.ui.ISearchResultView;import org.eclipse.search.ui.ISearchResultViewEntry;import org.eclipse.search.ui.SearchUI;

class GotoMarkerAction extends Action {

	private IEditorPart fEditor;

	public GotoMarkerAction() {
		super(SearchPlugin.getResourceString("SearchResultView.gotoMarker.text"));
	}
	
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
	
	public void show(IMarker marker) {
		IWorkbenchPage page= SearchPlugin.getActivePage();
		IResource resource= marker.getResource();
		if (page == null || !(resource instanceof IFile))
			return;
		
		IEditorInput input= new FileEditorInput((IFile)resource);
		String editorId= SearchPlugin.getDefault().getWorkbench().getEditorRegistry().getDefaultEditor((IFile)resource).getId();
		IEditorPart editor= null;
		IEditorPart[] editorParts= page.getEditors();
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
				editor= page.openEditor(input, editorId, false);
				fEditor = editor;
			} catch (PartInitException ex) {
				ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.openEditor.");
			}

		} else {
			page.bringToTop(editor);
		}
		editor.gotoMarker(marker);
	}
}