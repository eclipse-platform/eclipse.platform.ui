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

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ZipFileTransformer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This class represents a handler for opening zip files.
 *
 * @since 3.132
 */
public class OpenZipFileHandler extends AbstractHandler {

	/**
	 * Executes the handler action, which involves opening a zip file selected by
	 * the user.
	 *
	 * @param event The event triggering the execution of this handler.
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		Object element = ((IStructuredSelection) selection).getFirstElement();

		if (!(element instanceof IFile)) {
			return null;
		}
			try {
				dialog.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InterruptedException {
						monitor.beginTask("Opening Zip File", 5); //$NON-NLS-1$
						try {
							ZipFileTransformer.openZipFile((IFile) element, monitor, true);
						} catch (URISyntaxException | CoreException e) {
							throw new InterruptedException(e.getMessage());
						}
						monitor.worked(1);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				MessageDialog.openError(shell, "Error opening zip file", e.getMessage()); //$NON-NLS-1$
			}
		return null;
	}
}
