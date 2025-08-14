/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.internal.util.VerifyDialog;

/**
 * A <code>DialogCheck</code> is used test a dialog in various ways.
 * <p>
 * For interactive tests use <code>assertDialog</code>. For automated tests use
 * <code>assert DialogTexts</code>.
 * </p>
 */
public class DialogCheck {
	private DialogCheck() {
	}

	private static VerifyDialog _verifyDialog;

	/**
	 * Asserts that a given dialog is not null and that it passes certain visual
	 * tests. These tests will be verified manually by the tester using an input
	 * dialog. Use this assert method to verify a dialog's sizing, initial focus, or
	 * accessibility. To ensure that both the input dialog and the test dialog are
	 * accessible by the tester, the getShell() method should be used when creating
	 * the test dialog.
	 *
	 * Example usage:
	 * <code>Dialog dialog = new AboutDialog( DialogCheck.getShell() );
	 * DialogCheck.assertDialog(dialog);</code>
	 *
	 * @param dialog
	 *            the test dialog to be verified.
	 */
	public static void assertDialog(Dialog dialog) {
		assertNotNull(dialog);
		if (_verifyDialog.getShell() == null) {
			//force the creation of the verify dialog
			getShell();
		}
		if (_verifyDialog.open(dialog) == IDialogConstants.NO_ID) {
			fail(_verifyDialog.getFailureText());
		}
	}

	/**
	 * Automated test that checks all the labels and buttons of a dialog to make
	 * sure there is enough room to display all the text. Any text that wraps is
	 * only approximated and is currently not accurate.
	 *
	 * @param dialog
	 *            the test dialog to be verified.
	 */
	public static void assertDialogTexts(Dialog dialog) {
		assertNotNull(dialog);
		dialog.setBlockOnOpen(false);
		dialog.open();
		Shell shell = dialog.getShell();
		processEvents();
		try {
			verifyCompositeText(shell);
		} finally {
			dialog.close();
			// close "verify results" dialog, it makes other tests unhappy
			_verifyDialog.buttonPressed(IDialogConstants.YES_ID);
		}
	}

	/**
	 * This method should be called when creating dialogs to test.  This
	 * ensures that the dialog's parent shell will be that of the
	 * verification dialog.
	 *
	 * @return Shell The shell of the verification dialog to be used as
	 * the parent shell of the test dialog.
	 */
	public static Shell getShell() {
		Shell shell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		_verifyDialog = new VerifyDialog(shell);
		_verifyDialog.create();
		return _verifyDialog.getShell();
	}

	/*
	 * Looks at all the child widgets of a given composite and
	 * verifies the text on all labels and widgets.
	 * @param composite The composite to look through
	 */
	private static void verifyCompositeText(Composite composite) {
		Control children[] = composite.getChildren();
		for (Control child : children) {
			if (child instanceof TabFolder folder) {
				int numPages = folder.getItemCount();
				for (int j = 0; j < numPages; j++) {
					folder.setSelection(j);
				}
			} else if (child instanceof CTabFolder folder) {
				int numPages = folder.getItemCount();
				for (int j = 0; j < numPages; j++) {
					folder.setSelection(j);
				}
			}
			else if (child instanceof Button b) {
				//verify the text if the child is a button
				verifyButtonText(b);
			}
			else if (child instanceof Label l) {
				//child is not a button, maybe a label
				verifyLabelText(l);
			}
			else if (child instanceof Composite comp) {
				//child is not a label, make a recursive call if it is a composite
				verifyCompositeText(comp);
			}
		}
	}

	/*
	 * Verifies that a given button is large enough to display its text.
	 * @param button The button to verify,
	 */
	private static void verifyButtonText(Button button) {
		String widget = button.toString();
		Point size = button.getSize();

		//compute the size with no line wrapping
		Point preferred = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//if (size.y/preferred.y) == X, then label spans X lines, so divide
		//the calculated value of preferred.x by X
		if (preferred.y * size.y > 0) {
			String buttonText = button.getText();
			preferred.y /= buttonText.isEmpty() ? 1 : buttonText.lines().count(); // check for '\n\'
			if (size.y / preferred.y > 1) {
				preferred.x /= (size.y / preferred.y);
			}
		}

		String message = new StringBuilder("Warning: ").append(widget).append(
				"\n\tActual Width -> ").append(size.x).append(
				"\n\tRecommended Width -> ").append(preferred.x).toString();
		if (preferred.x > size.x) {
			//close the dialog
			button.getShell().dispose();
			fail(message);
		}
	}

	/*
	 * Verifies that a given label is large enough to display its text.
	 * @param label The label to verify,
	 */
	private static void verifyLabelText(Label label) {
		if (!label.isVisible())
			return;
		String widget = label.toString();
		Point size = label.getSize();
		String labelText = label.getText();
		if (labelText == null || labelText.isEmpty())
			return;
		//compute the size with no line wrapping
		Point preferred = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//if (size.y/preferred.y) == X, then label spans X lines, so divide
		//the calculated value of preferred.x by X
		if (preferred.y * size.y > 0) {
			preferred.y /= label.getText().lines().count();
			if (size.y / preferred.y > 1) {
				preferred.x /= (size.y / preferred.y);
			}
		}
		String message = new StringBuilder("Warning: ").append(widget).append(
				"\n\tActual Width -> ").append(size.x).append(
				"\n\tRecommended Width -> ").append(preferred.x).toString();
		if (preferred.x > size.x) {
			//close the dialog
			label.getShell().dispose();
			fail(message);
		}
	}

}
