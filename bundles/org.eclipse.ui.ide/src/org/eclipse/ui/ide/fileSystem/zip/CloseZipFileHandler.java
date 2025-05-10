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

package org.eclipse.ui.ide.fileSystem.zip;

import java.net.URISyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ZipFileTransformer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This class represents a handler for closing an opened zip file.
 *
 * @since 3.23
 */
public class CloseZipFileHandler extends AbstractHandler {

	/**
	 * Executes the handler action, which involves closing an opened zip file.
	 *
	 * @param event The event triggering the execution of this handler.
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (!(selection instanceof IStructuredSelection structuredSelection)) {
			return null;
		}
		if (!(structuredSelection.getFirstElement() instanceof IFolder folder)) {
			return null;
		}
		try {
			ZipFileTransformer.closeZipFile(folder);
		} catch (CoreException | URISyntaxException e) {
			IDEWorkbenchPlugin.log(e.getMessage(), e);
			MessageDialog.openError(shell, "Error closing zip file", e.getMessage()); //$NON-NLS-1$
		}
		return null;
	}
}
