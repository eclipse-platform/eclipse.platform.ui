/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
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
				File file= new File(fFilterPath + File.separator + names[i]);
				if (file.isFile() && file.exists()) {
					IEditorInput input= createEditorInput(file);
					String editorId= getEditorId(file);
					IWorkbenchPage page= fWindow.getActivePage();
					try {
						page.openEditor(input, editorId);
					} catch (PartInitException e) {
						EditorsPlugin.log(e.getStatus());
					}
				} else {
					if (++numberOfFilesNotFound > 1)
						notFound.append('\n');
					notFound.append(file.getName());
				}
			}

			if (numberOfFilesNotFound > 0) {
				String msgFmt= numberOfFilesNotFound == 1 ? TextEditorMessages.OpenExternalFileAction_message_fileNotFound : TextEditorMessages.OpenExternalFileAction_message_filesNotFound;
				String msg= MessageFormat.format(msgFmt, new Object[] { notFound.toString() });
				MessageDialog.openError(fWindow.getShell(), TextEditorMessages.OpenExternalFileAction_title, msg);
			}
		}
	}

	/*
	 * XXX: Requested a helper to get the correct editor descriptor
	 *		see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=110203
	 */
	private String getEditorId(File file) {
		IWorkbench workbench= fWindow.getWorkbench();
		IEditorRegistry editorRegistry= workbench.getEditorRegistry();
		IEditorDescriptor descriptor= editorRegistry.getDefaultEditor(file.getName(), getContentType(file));

		// check the OS for in-place editor (OLE on Win32)
		if (descriptor == null && editorRegistry.isSystemInPlaceEditorAvailable(file.getName()))
			descriptor= editorRegistry.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		
		// check the OS for external editor
		if (descriptor == null && editorRegistry.isSystemExternalEditorAvailable(file.getName()))
			descriptor= editorRegistry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		
		if (descriptor != null)
			return descriptor.getId();
		
		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}

	private IContentType getContentType (File file) {
		if (file == null)
			return null;

		InputStream stream= null;
		try {
			stream= new FileInputStream(file);
			return Platform.getContentTypeManager().findContentTypeFor(stream, file.getName());
		} catch (IOException x) {
			EditorsPlugin.log(x);
			return null;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException x) {
				EditorsPlugin.log(x);
			}
		}
	}

	private IEditorInput createEditorInput(File file) {
		IFile workspaceFile= getWorkspaceFile(file);
		if (workspaceFile != null)
			return new FileEditorInput(workspaceFile);
		return new JavaFileEditorInput(file);
	}

	private IFile getWorkspaceFile(File file) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IPath location= Path.fromOSString(file.getAbsolutePath());
		IFile[] files= workspace.getRoot().findFilesForLocation(location);
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
