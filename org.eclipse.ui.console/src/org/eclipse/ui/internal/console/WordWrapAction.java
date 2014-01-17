/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gaetano Santoro - initial implementation
 *     Matthias Mailänder - rebase onto Mars
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 287303
 *******************************************************************************/

package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;

/**
 * Activates line breaks in the Console view so that the full log statement is
 * always visible
 */
public class WordWrapAction extends Action {

	private IConsoleView fConsoleView = null;

	public WordWrapAction(IConsoleView consoleView) {
		super(ConsoleMessages.WordWrapAction_0);
		fConsoleView = consoleView;

		setToolTipText(ConsoleMessages.WordWrapAction_1);
		setHoverImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_LCL_WRAP));
		setDisabledImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_WRAP));
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_WRAP));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CONSOLE_WORD_WRAP_ACTION);

		boolean checked = fConsoleView.getWordWrap();
		setChecked(checked);
	}

	@Override
	public void run() {
		fConsoleView.setWordWrap(isChecked());
	}

	public void dispose() {
		fConsoleView = null;
	}
}