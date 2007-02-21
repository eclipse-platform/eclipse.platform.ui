/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @since 3.0
 */
public class OpenExternalFileAction extends Action implements IWorkbenchWindowActionDelegate {

	static class FileLabelProvider extends LabelProvider {
		/*
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof IFile) {
				IPath path=  ((IFile) element).getFullPath();
				return path != null ? path.toString() : ""; //$NON-NLS-1$
			}
			return super.getText(element);
		}
	}


	private IWorkbenchWindow fWindow;
	private String fFilterPath;

	public OpenExternalFileAction() {
		setEnabled(true);
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow= null;
		fFilterPath= null;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow= window;
		fFilterPath= System.getProperty("user.home"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		FileDialog dialog= new FileDialog(fWindow.getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setText(TextEditorMessages.OpenExternalFileAction_title);
		dialog.setFilterPath(fFilterPath);
		dialog.open();
		String[] names= dialog.getFileNames();

		if (names != null) {
			fFilterPath= dialog.getFilterPath();

			int numberOfFilesNotFound= 0;
			StringBuffer notFound= new StringBuffer();
			for (int i= 0; i < names.length; i++) {
				IFileStore fileStore= EFS.getLocalFileSystem().getStore(new Path(fFilterPath));
				fileStore= fileStore.getChild(names[i]);
				if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
					IEditorInput input= createEditorInput(fileStore);
					String editorId= getEditorId(fileStore);
					IWorkbenchPage page= fWindow.getActivePage();
					try {
						page.openEditor(input, editorId);
					} catch (PartInitException e) {
						EditorsPlugin.log(e.getStatus());
						String msg= NLSUtility.format(TextEditorMessages.OpenExternalFileAction_message_errorOnOpen, fileStore.getName());
						MessageDialog.openError(fWindow.getShell(), TextEditorMessages.OpenExternalFileAction_title, msg);
					}
				} else {
					if (++numberOfFilesNotFound > 1)
						notFound.append('\n');
					notFound.append(fileStore.getName());
				}
			}

			if (numberOfFilesNotFound > 0) {
				String msgFmt= numberOfFilesNotFound == 1 ? TextEditorMessages.OpenExternalFileAction_message_fileNotFound : TextEditorMessages.OpenExternalFileAction_message_filesNotFound;
				String msg= NLSUtility.format(msgFmt, notFound.toString());
				MessageDialog.openError(fWindow.getShell(), TextEditorMessages.OpenExternalFileAction_title, msg);
			}
		}
	}

	private String getEditorId(IFileStore file) {
		IEditorDescriptor descriptor;
		try {
			descriptor= IDE.getEditorDescriptor(file.getName());
		} catch (PartInitException e) {
			return null;
		}
		if (descriptor != null)
			return descriptor.getId();
		return null;
	}

	private IEditorInput createEditorInput(IFileStore fileStore) {
		IFile workspaceFile= getWorkspaceFile(fileStore);
		if (workspaceFile != null)
			return new FileEditorInput(workspaceFile);
		return new FileStoreEditorInput(fileStore);
	}

	private IFile getWorkspaceFile(IFileStore fileStore) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile[] files= workspace.getRoot().findFilesForLocation(new Path(fileStore.toURI().getPath()));
		files= filterNonExistentFiles(files);
		if (files == null || files.length == 0)
			return null;
		if (files.length == 1)
			return files[0];
		return selectWorkspaceFile(files);
	}

	private IFile[] filterNonExistentFiles(IFile[] files){
		if (files == null)
			return null;

		int length= files.length;
		ArrayList existentFiles= new ArrayList(length);
		for (int i= 0; i < length; i++) {
			if (files[i].exists())
				existentFiles.add(files[i]);
		}
		return (IFile[])existentFiles.toArray(new IFile[existentFiles.size()]);
	}

	private IFile selectWorkspaceFile(IFile[] files) {
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(fWindow.getShell(), new FileLabelProvider());
		dialog.setElements(files);
		dialog.setTitle(TextEditorMessages.OpenExternalFileAction_title_selectWorkspaceFile);
		dialog.setMessage(TextEditorMessages.OpenExternalFileAction_message_fileLinkedToMultiple);
		if (dialog.open() == Window.OK)
			return (IFile) dialog.getFirstResult();
		return null;
	}
}
