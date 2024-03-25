/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     Robert Roth (robert.roth.off@gmail.com) - Bug 487093: You can too easily clear the search history
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;


class RemoveAllSearchesAction extends Action {

	public RemoveAllSearchesAction() {
		super(SearchMessages.RemoveAllSearchesAction_label);
		setToolTipText(SearchMessages.RemoveAllSearchesAction_tooltip);
	}

	/**
	 * Returns whether to ask for confirmation on search history clear. Consults the preference and prompts the user if
	 * necessary.
	 *
	 * @return <code>true</code> if clear search history should be confirmed by user, and <code>false</code>
	 *         otherwise.
	 */
	private boolean promptForConfirmation() {

		MessageDialog dialog= new MessageDialog(SearchPlugin.getActiveWorkbenchShell(),
				SearchMessages.RemoveAllSearchesAction_tooltip, // title
				null, // image
				SearchMessages.RemoveAllSearchesAction_confirm_message, // message
				MessageDialog.CONFIRM,
				new String[] {SearchMessages.RemoveAllSearchesAction_confirm_label, IDialogConstants.CANCEL_LABEL},
				IDialogConstants.OK_ID);

		dialog.open();
		if (dialog.getReturnCode() != IDialogConstants.OK_ID) {
			return false;
		}
		return true;
	}

	@Override
	public void run() {
		ISearchQuery[] queries= NewSearchUI.getQueries();
		if (promptForConfirmation()) {
			for (ISearchQuery querie : queries) {
				if (!NewSearchUI.isQueryRunning(querie))
					InternalSearchUI.getInstance().removeQuery(querie);
			}
		}
	}
}
