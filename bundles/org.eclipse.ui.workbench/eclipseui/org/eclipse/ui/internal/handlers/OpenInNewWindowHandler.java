/*******************************************************************************
 * Copyright (c) 2008, 2026 IBM Corporation and others.
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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import jakarta.inject.Named;

public class OpenInNewWindowHandler {

	@Execute
	public void execute(
			@Optional @Named(ISources.ACTIVE_WORKBENCH_WINDOW_NAME) IWorkbenchWindow activeWorkbenchWindow) {
		if (activeWorkbenchWindow == null) {
			return;
		}
		try {
			String perspId = null;

			IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
			IAdaptable pageInput = ((Workbench) activeWorkbenchWindow.getWorkbench()).getDefaultPageInput();
			if (page != null && page.getPerspective() != null) {
				perspId = page.getPerspective().getId();
				pageInput = page.getInput();
			} else {
				perspId = activeWorkbenchWindow.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();
			}

			activeWorkbenchWindow.getWorkbench().openWorkbenchWindow(perspId, pageInput);
		} catch (WorkbenchException e) {
			StatusUtil.handleStatus(e.getStatus(),
					WorkbenchMessages.OpenInNewWindowAction_errorTitle + ": " + e.getMessage(), //$NON-NLS-1$
					StatusManager.SHOW);
		}
	}

}
