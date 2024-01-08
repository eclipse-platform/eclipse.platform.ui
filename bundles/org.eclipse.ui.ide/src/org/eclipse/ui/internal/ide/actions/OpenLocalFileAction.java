/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;


/**
 * Standard action for opening an editor on local file(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenLocalFileAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;
	private String filterPath;

	/**
	 * Creates a new action for opening a local file.
	 */
	public OpenLocalFileAction() {
		setEnabled(true);
	}

	@Override
	public void dispose() {
		workbenchWindow =  null;
		filterPath =  null;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.workbenchWindow =  window;
		filterPath =  System.getProperty("user.home"); //$NON-NLS-1$
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void run() {
		FileDialog dialog =  new FileDialog(workbenchWindow.getShell(), SWT.OPEN | SWT.MULTI | SWT.SHEET);
		dialog.setText(IDEWorkbenchMessages.OpenLocalFileAction_title);
		dialog.setFilterPath(filterPath);
		dialog.open();
		String[] names =  dialog.getFileNames();

		if (names != null) {
			filterPath =  dialog.getFilterPath();

			int numberOfFilesNotFound =  0;
			StringBuilder notFound =  new StringBuilder();
			for (String name : names) {
				IFileStore fileStore =  EFS.getLocalFileSystem().getStore(IPath.fromOSString(filterPath));
				fileStore =  fileStore.getChild(name);
				IFileInfo fetchInfo = fileStore.fetchInfo();
				if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
					IWorkbenchPage page =  workbenchWindow.getActivePage();
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException e) {
						String msg =  NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen, fileStore.getName());
						IDEWorkbenchPlugin.log(msg,e.getStatus());
						MessageDialog.open(MessageDialog.ERROR,workbenchWindow.getShell(), IDEWorkbenchMessages.OpenLocalFileAction_title, msg, SWT.SHEET);
					}
				} else {
					if (++numberOfFilesNotFound > 1)
						notFound.append('\n');
					notFound.append(fileStore.getName());
				}
			}

			if (numberOfFilesNotFound > 0) {
				String msgFmt =  numberOfFilesNotFound == 1 ? IDEWorkbenchMessages.OpenLocalFileAction_message_fileNotFound : IDEWorkbenchMessages.OpenLocalFileAction_message_filesNotFound;
				String msg =  NLS.bind(msgFmt, notFound.toString());
				MessageDialog.open(MessageDialog.ERROR, workbenchWindow.getShell(), IDEWorkbenchMessages.OpenLocalFileAction_title, msg, SWT.SHEET);
			}
		}
	}
}
