/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.sync.compare.SyncInfoCompareInput;
import org.eclipse.team.internal.ui.sync.views.SyncSet;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;

/**
 * OpenInCompareEditor
 * 
 * TODO: Compare editor should implement IReusableEditor so that the non-dirty editor doesn't have to be closed
 * and the compare editor finding should be cleaned up.
 */
public class OpenInCompareAction extends Action {
	
	private SyncViewer viewer;
	
	public OpenInCompareAction(SyncViewer viewer) {
		this.viewer = viewer;
		setText("Open With Compare Editor");
	}

	public void run() {	
		openEditor();
	}
	
	private void openEditor() {
		CompareEditorInput input = getCompareInput();
		if(input != null) { 
			IEditorPart editor = reuseCompareEditor((SyncInfoCompareInput)input);
			if(editor != null && editor instanceof IReusableEditor) {
				CompareUI.openCompareEditor(input);
				// should be enabled once  Bug 38770 is fixed
				//((IReusableEditor)editor).setInput(input);
			} else {
				CompareUI.openCompareEditor(input);
			}
		}		
	}
	
	private CompareEditorInput getCompareInput() {
		ISelection selection = viewer.getViewer().getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		SyncInfo info = SyncSet.getSyncInfo(obj);
		if (info != null && info.getLocal() instanceof IFile) {
			return new SyncInfoCompareInput(info);								
		}
		return null;
	}				

	private IEditorPart reuseCompareEditor(SyncInfoCompareInput input) {
		IWorkbenchPage page = viewer.getSite().getPage();
		IEditorReference[] editorRefs = page.getEditorReferences();
		
		IEditorPart editor = page.findEditor(input);
		if(editor == null) {
			for (int i = 0; i < editorRefs.length; i++) {
				IEditorPart part = editorRefs[i].getEditor(true);
				if(part != null && part.getEditorInput() instanceof SyncInfoCompareInput) {
					if(! part.isDirty()) {	
						// should be removed once Bug 38770 is fixed					
						page.closeEditor(part, true /*save changes if required */);		
					}
				}
			}
		}
		return editor;
	}
}