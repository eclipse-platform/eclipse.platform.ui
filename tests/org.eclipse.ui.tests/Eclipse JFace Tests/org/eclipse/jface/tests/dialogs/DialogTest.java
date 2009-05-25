/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class DialogTest extends TestCase {

	/**
	 * The dialog being tested.
	 */
	private Dialog dialog;

	protected void tearDown() throws Exception {
		if (dialog != null) {
			// close the dialog
			dialog.close();
			dialog = null;
		}
		super.tearDown();
	}

	/**
	 * If a layout is invoked prior to the button being shifted in
	 * {@link org.eclipse.jface.dialogs.Dialog Dialog}'s
	 * {@link org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 * initializeBounds()} invocation, the button will not be visually shifted
	 * even though getChildren() may prove otherwise. We check for this by
	 * comparing the X coordinate of the 'OK' and 'Cancel' buttons to ensure
	 * that they are in the right place if the dismissal alignment for the
	 * current platform is SWT.RIGHT.
	 */
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
		Button cancelBtn = forceLayoutDialog
				.getButton(IDialogConstants.CANCEL_ID);

		// retrieve the X coordinates of the two buttons
		int okX = okBtn.getBounds().x;
		int cancelX = cancelBtn.getBounds().x;

		if (okBtn.getDisplay().getDismissalAlignment() == SWT.LEFT) {
			assertTrue(
					"The 'OK' button should be to the left of the 'Cancel' button",
					okX < cancelX);
		} else {
			assertTrue(
					"The 'OK' button should be to the right of the 'Cancel' button",
					cancelX < okX);
		}

		forceLayoutDialog.close();
	}

	/**
	 * A dialog that explicitly invokes a layout operation prior to the shell
	 * being opened. Calls to moveBelow(Control) should be followed by a layout
	 * operation and as clients may explicitly invoke layout while subclassing
	 * Dialog, we want to be sure that this doesn't prevent the
	 * moveBelow(Control) call from working.
	 */
	private class ForceLayoutDialog extends Dialog {

		ForceLayoutDialog() {
			super((Shell) null);
		}

		protected Control createContents(Composite parent) {
			Control contents = super.createContents(parent);
			// explicitly layout the button prior to the shell being realized
			getShell().layout(
					new Control[] { getButton(IDialogConstants.OK_ID) });
			return contents;
		}

		/**
		 * Overridden to allow it to be invoked locally.
		 */
		protected Button getButton(int id) {
			return super.getButton(id);
		}

	}

}
