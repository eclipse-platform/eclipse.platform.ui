/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IFile;

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

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.ui.SearchPlugin;

public class EditorOpener {

	private IEditorReference fReusedEditor;
	
	public IEditorPart open(IFile file, boolean activate) throws PartInitException {
		IWorkbenchPage wbPage= SearchPlugin.getActivePage();
		if (NewSearchUI.reuseEditor())
			return showWithReuse(file, wbPage, activate);
		return showWithoutReuse(file, wbPage, activate);
	}
	
	private IEditorPart showWithoutReuse(IFile file, IWorkbenchPage wbPage, boolean activate) throws PartInitException {
		return IDE.openEditor(wbPage, file, activate);
	}

	private IEditorPart showWithReuse(IFile file, IWorkbenchPage wbPage, boolean activate) throws PartInitException {
		String editorID= getEditorID(file);
		return showInEditor(wbPage, file, editorID, activate);
	}


	private String getEditorID(IFile file) throws PartInitException {
		IEditorDescriptor desc= IDE.getEditorDescriptor(file);
		if (desc == null)
			return SearchPlugin.getDefault().getWorkbench().getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID).getId();
		return desc.getId();
	}
	
	private IEditorPart showInEditor(IWorkbenchPage page, IFile file, String editorId, boolean activate) throws PartInitException {
		IEditorInput input= new FileEditorInput(file);
		IEditorPart editor= page.findEditor(input);
		if (editor != null) {
			page.bringToTop(editor);
			if (activate) {
				page.activate(editor);
			}
			return editor;
		}
		IEditorReference reusedEditorRef= fReusedEditor;
		if (reusedEditorRef !=  null) {
			boolean isOpen= reusedEditorRef.getEditor(false) != null;
			boolean canBeReused= isOpen && !reusedEditorRef.isDirty() && !reusedEditorRef.isPinned();
			if (canBeReused) {
				boolean showsSameInputType= reusedEditorRef.getId().equals(editorId);
				if (!showsSameInputType) {
					page.closeEditors(new IEditorReference[] { reusedEditorRef }, false);
					fReusedEditor= null;
				} else {
					editor= reusedEditorRef.getEditor(true);
					if (editor instanceof IReusableEditor) {
						((IReusableEditor) editor).setInput(input);
						page.bringToTop(editor);
						if (activate) {
							page.activate(editor);
						}
						return editor;
					}
				}
			}
		}
		editor= page.openEditor(input, editorId, activate);
		if (editor instanceof IReusableEditor) {
			IEditorReference reference= (IEditorReference) page.getReference(editor);
			fReusedEditor= reference;
		} else {
			fReusedEditor= null;
		}
		return editor;
	}

	
}
