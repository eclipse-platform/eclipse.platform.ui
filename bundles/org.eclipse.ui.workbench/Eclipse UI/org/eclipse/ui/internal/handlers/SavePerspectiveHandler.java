/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import jakarta.inject.Inject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.SavePerspectiveDialog;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

/**
 *
 * @author Prakash G.R.
 *
 * @since 3.7
 *
 */
public class SavePerspectiveHandler extends AbstractHandler {

	@Inject
	EModelService modelService;

	@Override
	public Object execute(ExecutionEvent event) {

		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
			if (page != null) {
				PerspectiveDescriptor descriptor = (PerspectiveDescriptor) page.getPerspective();
				if (descriptor != null) {
					if (descriptor.isSingleton()) {
						saveSingleton(page);
					} else {
						saveNonSingleton(page, descriptor);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Save a singleton over itself.
	 */
	private void saveSingleton(IWorkbenchPage page) {
		MessageDialog d = new MessageDialog(page.getWorkbenchWindow().getShell(),
				WorkbenchMessages.SavePerspective_overwriteTitle, null,
				WorkbenchMessages.SavePerspective_singletonQuestion, MessageDialog.QUESTION, 0,
				IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL);
		if (d.open() == 0) {
			page.savePerspective();
		}
	}

	/**
	 * Save a singleton over the user selection.
	 */
	private void saveNonSingleton(IWorkbenchPage page, PerspectiveDescriptor oldDesc) {
		// Get reg.
		PerspectiveRegistry reg = (PerspectiveRegistry) WorkbenchPlugin.getDefault().getPerspectiveRegistry();

		// Get persp name.
		SavePerspectiveDialog dlg = new SavePerspectiveDialog(page.getWorkbenchWindow().getShell(), reg);
		// Look up the descriptor by id again to ensure it is still valid.
		IPerspectiveDescriptor description = reg.findPerspectiveWithId(oldDesc.getId());
		dlg.setInitialSelection(description);
		if (dlg.open() != IDialogConstants.OK_ID) {
			return;
		}

		// Create descriptor.
		PerspectiveDescriptor newDesc = (PerspectiveDescriptor) dlg.getPersp();
		if (newDesc == null) {
			String name = dlg.getPerspName();
			newDesc = reg.createPerspective(name, (PerspectiveDescriptor) description);
			if (newDesc == null) {
				MessageDialog.openError(dlg.getShell(), WorkbenchMessages.SavePerspective_errorTitle,
						WorkbenchMessages.SavePerspective_errorMessage);
				return;
			}
		}

		// Save state.
		page.savePerspectiveAs(newDesc);
	}
}
