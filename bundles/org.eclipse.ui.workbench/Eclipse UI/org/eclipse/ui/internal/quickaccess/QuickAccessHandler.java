/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     René Brandstetter - Bug 431707 - [QuickAccess] Quick Access should open a dialog if hidden
 *******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.Arrays;
import java.util.Optional;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for quick access pop-up dialog, showing UI elements such as editors,
 * views, commands.
 */
public class QuickAccessHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent executionEvent) {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(executionEvent);
		if (window == null) {
			return null;
		}

		Optional<QuickAccessContents> existingContents = Arrays.stream(window.getShell().getDisplay().getShells()) //
				.filter(Shell::isVisible) //
				.map(Shell::getData) //
				.filter(QuickAccessDialog.class::isInstance) //
				.map(QuickAccessDialog.class::cast) //
				.map(QuickAccessDialog::getQuickAccessContents) //
				.findAny();
		if (existingContents.isPresent()) {
			QuickAccessContents contents = existingContents.get();
			contents.setShowAllMatches(!contents.getShowAllMatches());
		} else {
			displayQuickAccessDialog(window, executionEvent.getCommand());
		}
		return null;
	}

	/**
	 * Utility method to displays the original/legacy QuickAccess dialog.
	 *
	 * @param window  the active workbench window
	 * @param command the command which invokes the open of the dialog
	 */
	private static void displayQuickAccessDialog(IWorkbenchWindow window, Command command) {
		PopupDialog popupDialog = new QuickAccessDialog(window, command);
		popupDialog.open();
	}

}