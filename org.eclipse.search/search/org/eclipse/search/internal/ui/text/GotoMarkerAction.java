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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
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
		IEditorDescriptor desc= IDE.getDefaultEditor((IFile)resource);
		if (desc == null)
			editorId= SearchPlugin.getDefault().getWorkbench().getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID).getId();
		else
			editorId= desc.getId();

		IEditorPart editor= page.findEditor(input);
		if (editor != null)
			page.bringToTop(editor);
		else {
			boolean isOpen= false;
			if (fEditor != null) {
				IEditorReference[] parts= page.getEditorReferences();
				int i= 0;
				while (!isOpen && i < parts.length)
					isOpen= fEditor == parts[i++].getEditor(false);
			}

			boolean canBeReused= isOpen && !fEditor.isDirty() && !isPinned(fEditor);
			boolean showsSameInputType= fEditor != null && fEditor.getSite().getId().equals(editorId);

			if (canBeReused && !showsSameInputType) {
				page.closeEditor(fEditor, false);
				fEditor= null;
			}
			
			if (canBeReused && showsSameInputType) {
				((IReusableEditor)fEditor).setInput(input);
				page.bringToTop(fEditor);
				editor= fEditor;
			} else
				try {
					editor= page.openEditor(input, editorId, false);
					if (editor instanceof IReusableEditor)
						fEditor= editor;
					else
						fEditor= null;
				} catch (PartInitException ex) {
					ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.openEditor.title"), SearchMessages.getString("Search.Error.openEditor.message")); //$NON-NLS-2$ //$NON-NLS-1$
					return;
				}
		}
		
		if (editor != null) {
			IDE.gotoMarker(editor, marker);
		}
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
	
	private void showWithoutReuse(IMarker marker) {
		IWorkbenchPage page= SearchPlugin.getActivePage();
		if (page == null)
			return;

		try {
			IDE.openEditor(page, marker, false);
		} catch (PartInitException ex) {
			ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.openEditor.title"), SearchMessages.getString("Search.Error.openEditor.message")); //$NON-NLS-2$ //$NON-NLS-1$
			return;
		}
	}
}
