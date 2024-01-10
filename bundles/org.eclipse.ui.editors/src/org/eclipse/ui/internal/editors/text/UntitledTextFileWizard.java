/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Wizard with no page that creates the untitled text file
 * and opens the text editor.
 *
 * @since 3.1
 */
public class UntitledTextFileWizard extends Wizard implements INewWizard {

	private static final String TEXT_CONTENT_TYPE_ID= "org.eclipse.core.runtime.text"; //$NON-NLS-1$

	private IWorkbenchWindow fWindow;

	public UntitledTextFileWizard() {
	}

	@Override
	public void dispose() {
		fWindow= null;
	}

	private IFileStore queryFileStore() {
		IPath stateLocation= EditorsPlugin.getDefault().getStateLocation();
		IPath path= stateLocation.append("/_" + new Object().hashCode()); //$NON-NLS-1$
		return EFS.getLocalFileSystem().getStore(path);
	}

	private String getEditorId(IFileStore fileStore) {
		IWorkbench workbench= fWindow.getWorkbench();
		IEditorRegistry editorRegistry= workbench.getEditorRegistry();
		IContentType textContentType= Platform.getContentTypeManager().getContentType(TEXT_CONTENT_TYPE_ID);
		IEditorDescriptor descriptor= editorRegistry.getDefaultEditor(fileStore.getName(), textContentType);
		if (descriptor != null)
			return descriptor.getId();
		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}

	private IEditorInput createEditorInput(IFileStore fileStore) {
		return new NonExistingFileEditorInput(fileStore, TextEditorMessages.NewTextEditorAction_namePrefix);
	}

	@Override
	public boolean performFinish() {
		IFileStore fileStore= queryFileStore();
		IEditorInput input= createEditorInput(fileStore);
		String editorId= getEditorId(fileStore);
		IWorkbenchPage page= fWindow.getActivePage();
		try {
			page.openEditor(input, editorId);
		} catch (PartInitException e) {
			EditorsPlugin.log(e);
			return false;
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fWindow= workbench.getActiveWorkbenchWindow();
	}
}
