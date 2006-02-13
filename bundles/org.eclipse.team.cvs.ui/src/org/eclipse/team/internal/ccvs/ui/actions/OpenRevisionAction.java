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

package org.eclipse.team.internal.ccvs.ui.actions;

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
import org.eclipse.team.internal.ccvs.ui.CVSHistoryPage;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.progress.IProgressService;

public class OpenRevisionAction extends BaseSelectionListenerAction {

	private IStructuredSelection selection;
	private CVSHistoryPage page;
	
	public OpenRevisionAction(String text) {
		super(text);
	}

	public void run() {
			IStructuredSelection structSel = selection;

			Object[] objArray = structSel.toArray();

			for (int i = 0; i < objArray.length; i++) {
				final IFileRevision revision = (IFileRevision) objArray[i];
				if (revision == null || !revision.exists()) {
					MessageDialog.openError(page.getSite().getShell(), TeamUIMessages.OpenRevisionAction_DeletedRevisionTitle, TeamUIMessages.OpenRevisionAction_DeletedRevisionMessage);
				} else {
					IRunnableWithProgress runnable = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							IStorage file;
							try {
								file = revision.getStorage(monitor);
								String id = getEditorID(file.getName(), file.getContents());
								page.getSite().getPage().openEditor(new FileRevisionEditorInput(revision), id);
							} catch (CoreException e) {
							}
							
						}
					};
					
					IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
					try {
						progressService.run(false, false, runnable);
					} catch (InvocationTargetException e) {
					} catch (InterruptedException e) {
					}
				}

			}
	}
	
	public boolean isEnabled() {
		return true;
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
		if (descriptor == null) {
			id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			id = descriptor.getId();
		}

		return id;
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		this.selection = selection;
		return true;
	}
	
	public void setPage(CVSHistoryPage page) {
		this.page = page;
	}
}
