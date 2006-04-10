/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ui.actions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.history.AbstractHistoryCategory;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.progress.IProgressService;

public class OpenRevisionAction extends BaseSelectionListenerAction {

	private IStructuredSelection selection;
	private HistoryPage page;
	
	public OpenRevisionAction(String text) {
		super(text);
	}

	public void run() {
			IStructuredSelection structSel = selection;

			Object[] objArray = structSel.toArray();

			for (int i = 0; i < objArray.length; i++) {
				Object tempRevision = objArray[i];
				//If not a revision, don't try opening
				if (tempRevision instanceof AbstractHistoryCategory)
					continue;
				
				final IFileRevision revision = (IFileRevision) tempRevision;
				if (revision == null || !revision.exists()) {
					MessageDialog.openError(page.getSite().getShell(), TeamUIMessages.OpenRevisionAction_DeletedRevTitle, TeamUIMessages.OpenRevisionAction_DeletedRevMessage);
				} else {
					IRunnableWithProgress runnable = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							IStorage file;
							try {
								file = revision.getStorage(monitor);
								String id = getEditorID(file.getName(), file.getContents());
								FileRevisionEditorInput fileRevEditorInput = new FileRevisionEditorInput(revision); 
								if (!editorAlreadyOpenOnContents(fileRevEditorInput))
									page.getSite().getPage().openEditor(fileRevEditorInput, id);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
							
						}
					};
					
					IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
					try {
						progressService.run(false, false, runnable);
					} catch (InvocationTargetException e) {
						Utils.handleError(page.getSite().getShell(), e, TeamUIMessages.OpenRevisionAction_ErrorTitle, TeamUIMessages.OpenRevisionAction_ErrorMessage);
					} catch (InterruptedException e) {
					}
				}

			}
	}
	
	/* private */ String getEditorID(String fileName, InputStream contents) {
		IWorkbench workbench = TeamUIPlugin.getPlugin().getWorkbench();
		IEditorRegistry registry = workbench.getEditorRegistry();
		IContentType type = null;
		if (contents != null) {
			try {
				type = Platform.getContentTypeManager().findContentTypeFor(contents, fileName);
			} catch (IOException e) {

			}
		}
		if (type == null) {
			type = Platform.getContentTypeManager().findContentTypeFor(fileName);
		}
		IEditorDescriptor descriptor = registry.getDefaultEditor(fileName, type);
		String id;
		if (descriptor == null ||
			descriptor.isOpenExternal()) {
			id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			id = descriptor.getId();
		}

		return id;
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		this.selection = selection;
		return shouldShow();
	}
	
	public void setPage(HistoryPage page) {
		this.page = page;
	}
	
	private boolean shouldShow() {
		IStructuredSelection structSel = selection;
		Object[] objArray = structSel.toArray();
		
		if (objArray.length == 0)
			return false;
		
		for (int i = 0; i < objArray.length; i++) {
			//Don't bother showing if this a category
			if (objArray[i] instanceof AbstractHistoryCategory)
				return false;
			
			IFileRevision revision = (IFileRevision) objArray[i];
			//check to see if any of the selected revisions are deleted revisions
			if (revision != null && !revision.exists())
				return false;
		}
		
		return true;
	}
	
	
	private boolean editorAlreadyOpenOnContents(FileRevisionEditorInput input) {
		IEditorReference[] editorRefs = page.getSite().getPage().getEditorReferences();	
		for (int i = 0; i < editorRefs.length; i++) {
			IEditorPart part = editorRefs[i].getEditor(false);
			if(part != null 
			   && part.getEditorInput() instanceof FileRevisionEditorInput) {
				IFileRevision inputRevision = (IFileRevision) input.getAdapter(IFileRevision.class);
				IFileRevision editorRevision = (IFileRevision) part.getEditorInput().getAdapter(IFileRevision.class);
				
				if (inputRevision.equals(editorRevision)){
					//make the editor that already contains the revision current
					page.getSite().getPage().activate(part);
					return true;
				}
			}
		}
		return false;
	}

}
