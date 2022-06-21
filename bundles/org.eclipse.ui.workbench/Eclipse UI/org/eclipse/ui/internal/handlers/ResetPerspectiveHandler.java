/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.PlainMessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

/**
 * @since 3.5
 *
 */
public class ResetPerspectiveHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {

		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
			if (page != null) {
				IPerspectiveDescriptor descriptor = page.getPerspective();
				if (descriptor != null) {
					boolean offerRevertToBase = false;
					if (descriptor instanceof PerspectiveDescriptor) {
						PerspectiveDescriptor desc = (PerspectiveDescriptor) descriptor;
						offerRevertToBase = desc.isPredefined() && desc.hasCustomDefinition();
					}

					if (offerRevertToBase) {
						String message = NLS.bind(WorkbenchMessages.RevertPerspective_message, descriptor.getLabel());
						boolean toggleState = false;
						MessageDialogWithToggle dialog = MessageDialogWithToggle.open(MessageDialog.QUESTION,
								activeWorkbenchWindow.getShell(), WorkbenchMessages.RevertPerspective_title, message,
								WorkbenchMessages.RevertPerspective_option, toggleState, null, null, SWT.SHEET);
						if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
							if (dialog.getToggleState()) {
								PerspectiveRegistry reg = (PerspectiveRegistry) PlatformUI.getWorkbench()
										.getPerspectiveRegistry();
								reg.revertPerspective(descriptor);
							}
							page.resetPerspective();
						}
					} else {
						String message = NLS.bind(WorkbenchMessages.ResetPerspective_message, descriptor.getLabel());
						PlainMessageDialog dialog = PlainMessageDialog.getBuilder(activeWorkbenchWindow.getShell(),WorkbenchMessages.ResetPerspective_title).message(message).buttonLabels(List.of(WorkbenchMessages.ResetPerspective_buttonLabel, IDialogConstants.NO_LABEL)).build();
						int result = dialog.open();
						if (result == Window.OK) {
							page.resetPerspective();
						}
					}
				}
			}
		}

		return null;
	}
}
