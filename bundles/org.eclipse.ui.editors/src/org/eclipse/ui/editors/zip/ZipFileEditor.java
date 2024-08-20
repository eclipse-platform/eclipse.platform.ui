/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.zip;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ZipFileTransformer;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.ide.fileSystem.zip.ZipFileHandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.editors.text.TextEditor;

/**
 * @since 3.18
 */
public class ZipFileEditor extends TextEditor {

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		IWorkbenchPartSite site= getSite();
		Shell shell= site.getShell();
		if (input instanceof FileEditorInput fileEditorInput) {
			IFile file= fileEditorInput.getFile();
			openAndRefresh(file, shell);
		}
		Display display= shell.getDisplay();
		display.asyncExec(() -> {
			site.getPage().closeEditor(this, false);
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		return;
	}

	public static void openAndRefresh(IFile file, Shell shell) {
		try {
			ZipFileTransformer.openZipFile(file, true);
			ZipFileHandlerUtil.refreshAllViewers();
		} catch (CoreException e) {
			MessageDialog.openError(shell, "Error opening zip file", e.getMessage()); //$NON-NLS-1$
		}
	}
}