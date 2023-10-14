/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 ******************************************************************************/

package org.eclipse.jface.tests.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ButtonAlignmentTest {

	private TheTestWizard wizard;
	private TheTestWizardDialog dialog;

	@Before
	public void setUp() throws Exception {
		// ensure we've initialized a display for this thread
		Display.getDefault();
	}

	@After
	public void tearDown() throws Exception {
		if (dialog != null && dialog.getShell() != null
				&& !dialog.getShell().isDisposed()) {
			dialog.close();
		}
	}

	@Test
	public void testButtonAlignment() {
		wizard = new TheTestWizard();
		dialog = new TheTestWizardDialog(null, wizard);
		dialog.create();
		dialog.open();

		// retrieve the parent control for the button bar
		Composite parent = dialog.getFinishedButton().getParent();
		Control[] children = parent.getChildren();
		assertEquals(
				"There should be three children, a composite for back/next buttons, the finish button, and the cancel button", //$NON-NLS-1$
				3, children.length);

		// first children should be the Composite holding the 'Back' and 'Next'
		// buttons
		assertTrue(children[0] instanceof Composite);
		Composite backNextParent = (Composite) children[0];

		// retrieve its children and verify its contents
		Control[] backNextChildren = backNextParent.getChildren();
		assertEquals("Back button should be the first button", dialog //$NON-NLS-1$
				.getBackButton(), backNextChildren[0]);
		assertEquals("Next button should be the second button", dialog //$NON-NLS-1$
				.getNextButton(), backNextChildren[1]);

		// verify button alignment based on the platform's dismissal alignment
		int finishIndex = parent.getDisplay().getDismissalAlignment() == SWT.LEFT ? 1
				: 2;
		int cancelIndex = parent.getDisplay().getDismissalAlignment() == SWT.LEFT ? 2
				: 1;

		assertEquals(
				"Finish button's alignment is off", dialog.getFinishedButton(), children[finishIndex]); //$NON-NLS-1$
		assertEquals(
				"Cancel button's alignment is off", dialog.getCancelButton(), children[cancelIndex]); //$NON-NLS-1$
	}

	@Test
	public void testButtonAlignmentWithoutBackNextButtons() {
		wizard = new TheTestWizard() {
			@Override
			public void addPages() {
				// only add one page so there are no 'Back' or 'Next' buttons
				addPage(new TheTestWizardPage(page1Name));
			}
		};
		dialog = new TheTestWizardDialog(null, wizard);
		dialog.create();
		dialog.open();

		// retrieve the parent control for the button bar
		Composite parent = dialog.getFinishedButton().getParent();
		Control[] children = parent.getChildren();
		assertEquals(
				"There should be two children, the finish button, and the cancel button", //$NON-NLS-1$
				2, children.length);

		// verify button alignment based on the platform's dismissal alignment
		int finishIndex = parent.getDisplay().getDismissalAlignment() == SWT.LEFT ? 0
				: 1;
		int cancelIndex = parent.getDisplay().getDismissalAlignment() == SWT.LEFT ? 1
				: 0;

		assertEquals(
				"Finish button's alignment is off", dialog.getFinishedButton(), children[finishIndex]); //$NON-NLS-1$
		assertEquals(
				"Cancel button's alignment is off", dialog.getCancelButton(), children[cancelIndex]); //$NON-NLS-1$
	}

	@Test
	public void testBug270174() {
		wizard = new TheTestWizard() {
			@Override
			public boolean canFinish() {
				// make sure the wizard can't finish early, this will ensure
				// that the 'Next' button is the default button
				return false;
			}
		};
		dialog = new TheTestWizardDialog(null, wizard);
		dialog.create();
		dialog.open();

		// retrieve the parent control for the button bar
		Composite parent = dialog.getFinishedButton().getParent();
		Control[] children = parent.getChildren();
		assertEquals(
				"There should be three children, a composite for back/next buttons, the finish button, and the cancel button", //$NON-NLS-1$
				3, children.length);

		// first children should be the Composite holding the 'Back' and 'Next'
		// buttons
		assertTrue(children[0] instanceof Composite);
		Composite backNextParent = (Composite) children[0];

		// retrieve its children and verify its contents
		Control[] backNextChildren = backNextParent.getChildren();
		assertEquals("Back button should be the first button", dialog //$NON-NLS-1$
				.getBackButton(), backNextChildren[0]);
		assertEquals("Next button should be the second button", dialog //$NON-NLS-1$
				.getNextButton(), backNextChildren[1]);

		// verify button alignment based on the platform's dismissal alignment
		int finishIndex = parent.getDisplay().getDismissalAlignment() == SWT.LEFT ? 1
				: 2;
		int cancelIndex = parent.getDisplay().getDismissalAlignment() == SWT.LEFT ? 2
				: 1;

		assertEquals(
				"Finish button's alignment is off", dialog.getFinishedButton(), children[finishIndex]); //$NON-NLS-1$
		assertEquals(
				"Cancel button's alignment is off", dialog.getCancelButton(), children[cancelIndex]); //$NON-NLS-1$
	}

}
