/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Remy Chi Jian Suen - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class DialogTest {

	/**
	 * The dialog being tested.
	 */
	private Dialog dialog;

	@AfterEach
	public void tearDown() throws Exception {
		if (dialog != null) {
			// close the dialog
			dialog.close();
			dialog = null;
		}
	}

	/**
	 * If a layout is invoked prior to the button being shifted in
	 * {@link org.eclipse.jface.dialogs.Dialog Dialog}'s
	 * org.eclipse.jface.dialogs.Dialog#initializeBounds() invocation, the button
	 * will not be visually shifted even though getChildren() may prove otherwise.
	 * We check for this by comparing the X coordinate of the 'OK' and 'Cancel'
	 * buttons to ensure that they are in the right place if the dismissal alignment
	 * for the current platform is SWT.RIGHT.
	 */
	@Test
	public void testButtonAlignmentBug272583() {
		// instantiate a new dialog
		ForceLayoutDialog forceLayoutDialog = new ForceLayoutDialog();
		dialog = forceLayoutDialog;
		// don't block the UI/testing thread
		forceLayoutDialog.setBlockOnOpen(false);
		// open the dialog so the widgets will be realized
		forceLayoutDialog.open();

		// retrieve the 'OK' and 'Cancel' buttons
		Button okBtn = forceLayoutDialog.getButton(IDialogConstants.OK_ID);
		Button cancelBtn = forceLayoutDialog.getButton(IDialogConstants.CANCEL_ID);

		// retrieve the X coordinates of the two buttons
		int okX = okBtn.getBounds().x;
		int cancelX = cancelBtn.getBounds().x;

		if (okBtn.getDisplay().getDismissalAlignment() == SWT.LEFT) {
			assertTrue(okX < cancelX, "The 'OK' button should be to the left of the 'Cancel' button");
		} else {
			assertTrue(cancelX < okX, "The 'OK' button should be to the right of the 'Cancel' button");
		}

		forceLayoutDialog.close();
	}

	/**
	 * A dialog that explicitly invokes a layout operation prior to the shell being
	 * opened. Calls to moveBelow(Control) should be followed by a layout operation
	 * and as clients may explicitly invoke layout while subclassing Dialog, we want
	 * to be sure that this doesn't prevent the moveBelow(Control) call from
	 * working.
	 */
	private class ForceLayoutDialog extends Dialog {

		ForceLayoutDialog() {
			super((Shell) null);
		}

		@Override
		protected Control createContents(Composite parent) {
			Control contents = super.createContents(parent);
			// explicitly layout the button prior to the shell being realized
			getShell().layout(new Control[] { getButton(IDialogConstants.OK_ID) });
			return contents;
		}

		/**
		 * Overridden to allow it to be invoked locally.
		 */
		@Override
		protected Button getButton(int id) {
			return super.getButton(id);
		}

	}

}
