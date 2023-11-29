/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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

package org.eclipse.ui.examples.contributions.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.examples.contributions.ContributionMessages;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Opens a blank dialog with the InfoView title. The handler should only be
 * active when the activePart is an InfoView part.
 * <p>
 * This is used for the menu contribution example.
 * </p>
 *
 * @since 3.4
 */
public class InfoAboutHandler extends AbstractHandler {

	/**
	 * A blank dialog. This is activated by the About InfoView menu
	 * contribution. The handler is active when the InfoView part is active.
	 *
	 * @since 3.4
	 */
	private final class InfoAboutDialog extends Dialog {
		private InfoAboutDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(ContributionMessages.InfoView_about_msg);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		Dialog dialog = new InfoAboutDialog(window.getShell());
		dialog.open();
		return null;
	}

}
