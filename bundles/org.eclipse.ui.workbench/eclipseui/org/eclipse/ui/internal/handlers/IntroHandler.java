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
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.intro.IntroMessages;

/**
 *
 * @author Prakash G.R.
 *
 * @since 3.7
 */
public class IntroHandler extends AbstractHandler {

	private Workbench workbench;
	private IntroDescriptor introDescriptor;

	public IntroHandler() {
		workbench = (Workbench) PlatformUI.getWorkbench();
		introDescriptor = workbench.getIntroDescriptor();
	}

	@Override
	public Object execute(ExecutionEvent event) {

		if (introDescriptor == null) {
			MessageDialog.openWarning(HandlerUtil.getActiveShell(event), IntroMessages.Intro_missing_product_title,
					IntroMessages.Intro_missing_product_message);
		} else {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			workbench.getIntroManager().showIntro(window, false);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {

		boolean enabled = false;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			enabled = window.getPages().length > 0;
		}
		return enabled;
	}

}
