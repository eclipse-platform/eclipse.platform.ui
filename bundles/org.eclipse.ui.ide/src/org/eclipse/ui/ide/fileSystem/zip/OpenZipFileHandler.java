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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ZipFileTransformer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

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
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (!(element instanceof IFile)) {
			return null;
		}

		try {
			ZipFileTransformer.openZipFile((IFile) element, true);

			if (shell != null && !shell.isDisposed()) {
				shell.getDisplay().asyncExec(() -> {
					refreshAllViewers();

					// only for active viewer
//					StructuredViewer viewer = (StructuredViewer) HandlerUtil.getActivePart(event).getSite()
//							.getSelectionProvider();
//					if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
//						viewer.refresh();
//					}
				});
			}
		} catch (CoreException e) {
			IDEWorkbenchPlugin.log(e.getMessage(), e);
			MessageDialog.openError(shell, "Error opening zip file", e.getMessage()); //$NON-NLS-1$
		}
		return null;
	}

	private static void refreshAllViewers() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				for (IViewReference viewReference : page.getViewReferences()) {
					IWorkbenchPart part = viewReference.getPart(false);
					if (part != null) {
						ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
						if (selectionProvider instanceof StructuredViewer viewer) {
							if (viewer.getControl() != null && !viewer.getControl().isDisposed()) {
								viewer.refresh();
							}
						}
					}
				}
			}
		}
	}
}
