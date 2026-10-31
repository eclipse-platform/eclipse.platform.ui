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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class DialogTest {

	/** Width the growing test content adds per size computation. */
	private static final int GROWTH = 50;

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
	 * A default-sized, non-resizable dialog adapts its size after a zoom change.
	 * The adaptation calls <code>Shell.setSize</code>, which sends the next
	 * <code>SWT.Resize</code> to the same listener. The listener must not recurse
	 * when the computed size keeps changing, must still adapt on a later resize,
	 * and must not resize when the computed size is unchanged.
	 */
	@Test
	public void testZoomChangeAdaptationOfDefaultSizedDialogTerminates() {
		GrowingContentDialog growingDialog = new GrowingContentDialog();
		dialog = growingDialog;
		growingDialog.setBlockOnOpen(false);
		growingDialog.create();
		Shell shell = growingDialog.getShell();
		Control firstChild = shell.getChildren()[0];
		Point initialSize = shell.getSize();
		// the adaptation is hooked only for a dialog that reports its default size
		// after creation; where the platform reports another size for a shell that
		// is not open yet, there is nothing to test
		assumeFalse(growingDialog.resizeOccurredBeforeInitializeBounds(),
				"a resize before initializeBounds marks the dialog as custom-sized");
		assumeTrue(initialSize.equals(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true)),
				"the dialog must report its default size for the adaptation to be hooked");

		// every computed size differs from the previous one, so a listener without
		// re-entrancy guard recurses until the stack is exhausted
		int widthBeforeZoomChange = growingDialog.content.preferredWidth;
		growingDialog.content.growOnEveryComputeSize = true;
		firstChild.notifyListeners(SWT.ZoomChanged, new Event());
		shell.notifyListeners(SWT.Resize, new Event());
		assertTrue(growingDialog.content.preferredWidth > widthBeforeZoomChange,
				"the adaptation computes the size after the zoom change");
		Point adaptedSize = shell.getSize();
		assertNotEquals(initialSize, adaptedSize, "the dialog adapts its size once after the zoom change");

		// a later independent resize adapts to the new preferred size
		growingDialog.content.growOnEveryComputeSize = false;
		growingDialog.content.preferredWidth += GROWTH;
		shell.notifyListeners(SWT.Resize, new Event());
		Point resizedSize = shell.getSize();
		assertNotEquals(adaptedSize, resizedSize, "a later resize adapts the dialog to its new preferred size");

		// an unchanged computed size causes no further resize
		int[] resizeEvents = { 0 };
		shell.addListener(SWT.Resize, _ -> resizeEvents[0]++);
		shell.notifyListeners(SWT.Resize, new Event());
		assertEquals(resizedSize, shell.getSize(), "an unchanged computed size keeps the size");
		assertEquals(1, resizeEvents[0], "an unchanged computed size sends no further resize");
	}

	/**
	 * A dialog whose content reports a preferred width that can grow with every
	 * computation, so that the computed shell size is never a fixed point while
	 * growing is on.
	 */
	private class GrowingContentDialog extends Dialog {

		GrowingContent content;

		GrowingContentDialog() {
			super((Shell) null);
		}

		boolean resizeOccurredBeforeInitializeBounds() {
			return resizeHasOccurred;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			content = new GrowingContent(area);
			content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			return area;
		}
	}

	/**
	 * The content has to be wider and higher than the rest of the dialog, so that
	 * its preferred size determines the computed shell size on every platform. A
	 * narrow content would leave the button bar as the widest part, and growing it
	 * would not change the size of the shell.
	 */
	private static class GrowingContent extends Composite {

		int preferredWidth = 600;
		boolean growOnEveryComputeSize;

		GrowingContent(Composite parent) {
			super(parent, SWT.NONE);
		}

		@Override
		public Point computeSize(int wHint, int hHint, boolean changed) {
			if (growOnEveryComputeSize) {
				preferredWidth += GROWTH;
			}
			return new Point(preferredWidth, 400);
		}
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
