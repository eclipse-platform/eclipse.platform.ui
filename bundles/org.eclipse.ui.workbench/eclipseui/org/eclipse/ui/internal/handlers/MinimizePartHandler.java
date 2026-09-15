/*******************************************************************************
 * Copyright (c) 2007, 2026 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440136
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import jakarta.inject.Named;

public class MinimizePartHandler {

	@Execute
	public void execute(
			@Optional @Named(ISources.ACTIVE_WORKBENCH_WINDOW_NAME) IWorkbenchWindow activeWorkbenchWindow) {
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
			if (page != null) {
				IWorkbenchPartReference partRef = page.getActivePartReference();
				if (partRef != null) {
					page.setPartState(partRef, IWorkbenchPage.STATE_MINIMIZED);
				}
			}
		}
	}

}
