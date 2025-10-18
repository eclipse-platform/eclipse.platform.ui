/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WizardTest {
	protected static final int NUM_PAGES = 3;

	static boolean DID_FINISH = false; //accessed from this test AND wizard

	protected static final String WIZARD_TITLE = "TEST WIZARD TITLE";
	protected static final String PAGE_TITLE = "TEST PAGE TITLE";
	protected RGB color1;
	protected RGB color2;

	protected TheTestWizard wizard;
	protected TheTestWizardDialog dialog;

	boolean pageChanged = false;
	boolean pageChangingFired = false;

	@Test
	public void testEndingWithFinish() {
		//test page count
		assertEquals(NUM_PAGES, wizard.getPageCount(), "Wizard has wrong number of pages");

		//test page name
		assertEquals(TheTestWizard.page1Name, wizard.page1.getName(), "WizardPage.getName() returned wrong name");

		//test getPage()
		assertSame(wizard.page1, wizard.getPage(TheTestWizard.page1Name), "Wizard.getPage() returned wrong page");

		//test title
		wizard.setWindowTitle(WIZARD_TITLE);
		assertEquals(WIZARD_TITLE, wizard.getWindowTitle(), "Wizard has wrong title");
		wizard.page1.setTitle(PAGE_TITLE);
		assertEquals(PAGE_TITLE, wizard.page1.getTitle(), "Wizard has wrong title");

		//set+test color twice to ensure initial color didn't happen to be color1
		wizard.setTitleBarColor(color1);
		assertEquals(color1, wizard.getTitleBarColor(), "Wizard has wrong title color");
		wizard.setTitleBarColor(color2);
		assertEquals(color2, wizard.getTitleBarColor(), "Wizard has wrong title color");

		//test on starting page
		assertSame(wizard.page1, wizard.getStartingPage(), "Wizard has wrong starting page");
		assertSame(wizard.page1, dialog.getCurrentPage(), "Wizard not on starting page");

		//test getMessage()
		assertSame(null, wizard.page1.getErrorMessage(), "WizardPage error message should be null");
		wizard.page1.textInputField.setText(TheTestWizardPage.BAD_TEXT_FIELD_CONTENTS);
		assertEquals(TheTestWizardPage.BAD_TEXT_FIELD_STATUS,
				wizard.page1.getErrorMessage(), "WizardPage error message set correctly");

		//test page completion
		wizard.page1.textInputField.setText(TheTestWizardPage.GOOD_TEXT_FIELD_CONTENTS);
		assertTrue(wizard.page1.canFlipToNextPage(), "Page should be completed");
		//Setting good value should've cleared the error message
		assertSame(null, wizard.page1.getErrorMessage(), "WizardPage error message should be null");

		//test getNextPage() without page changes
		assertSame(wizard.page2, wizard.page1.getNextPage(), "WizardPage.getNexPage() wrong page");
		assertSame(wizard.page2, wizard.getNextPage(wizard.page1), "Wizard.getNexPage() wrong page");
		assertSame(wizard.page1, wizard.page2.getPreviousPage(), "WizardPage.getPreviousPage() wrong page");
		assertSame(wizard.page1, wizard.getPreviousPage(wizard.page2), "Wizard.getPreviousPage() wrong page");
		assertSame(wizard.page3, wizard.page2.getNextPage(), "WizardPage.getNexPage() wrong page");
		assertSame(wizard.page2, wizard.getPreviousPage(wizard.page3), "Wizard.getPreviousPage() wrong page");

		//test canFinish()
		wizard.page2.textInputField.setText(TheTestWizardPage.BAD_TEXT_FIELD_CONTENTS);
		assertFalse(wizard.canFinish(), "Wizard should not be able to finish");
		wizard.page2.textInputField.setText(TheTestWizardPage.GOOD_TEXT_FIELD_CONTENTS);
		assertTrue(wizard.canFinish(), "Wizard should be able to finish");

		//test simulated Finish button hit
		//TheTestWizard's performFinish() sets DID_FINISH to true
		dialog.finishPressed();
		assertTrue(DID_FINISH, "Wizard didn't perform finish");
	}

	@Test
	public void testEndingWithCancel() {
		assertSame(wizard.page1, dialog.getCurrentPage(), "Wizard not on starting page");

		//TheTestWizard's performFinish() sets DID_FINISH to true, ensure it was not called
		wizard.performCancel();
		assertFalse(DID_FINISH, "Wizard finished but should not have");

		dialog.cancelPressed();
		assertFalse(DID_FINISH, "Wizard performed finished but should not have");
	}

	@Test
	public void testPageChanging() {
		//initially on first page
		assertSame(wizard.page1, dialog.getCurrentPage(), "Wizard started on wrong page");
		assertFalse(dialog.getBackButton().getEnabled(), "Back button should be disabled on first page");
		assertTrue(dialog.getNextButton().getEnabled(), "Next button should be enabled on first page");

		//move to middle page 2
		dialog.nextPressed();
		assertSame(wizard.page2, dialog.getCurrentPage(), "Wizard.nextPressed() set wrong page");
		assertTrue(dialog.getBackButton().getEnabled(), "Back button should be enabled on middle page");
		assertTrue(dialog.getNextButton().getEnabled(), "Next button should be enabled on middle page");

		//test that can't complete by inserting bad value to be validated
		wizard.page2.textInputField.setText(TheTestWizardPage.BAD_TEXT_FIELD_CONTENTS);
		assertFalse(dialog.getFinishedButton().getEnabled(), "Finish should be disabled when bad field value");
		assertTrue(dialog.getCancelButton().getEnabled(), "Cancel should always be enabled");

		//test that can complete by inserting good value to be validated
		wizard.page2.textInputField.setText(TheTestWizardPage.GOOD_TEXT_FIELD_CONTENTS);
		assertTrue(dialog.getFinishedButton().getEnabled(), "Finish should be enabled when good field value");

		//move to last page 3
		dialog.nextPressed();
		assertSame(wizard.page3, dialog.getCurrentPage(), "Wizard.nextPressed() set wrong page");
		assertTrue(dialog.getBackButton().getEnabled(), "Back button should be enabled on last page");
		assertFalse(dialog.getNextButton().getEnabled(), "Next button should be disenabled on last page");

		//move back to page 2
		dialog.backPressed();
		assertSame(wizard.page2, dialog.getCurrentPage(), "Wizard.backPressed() set wrong page");
		assertTrue(dialog.getBackButton().getEnabled(), "Back button should be enabled on middle page");
		assertTrue(dialog.getNextButton().getEnabled(), "Next button should be enabled on middle page");

		//move back to page 1
		dialog.backPressed();
		assertSame(wizard.page1, dialog.getCurrentPage(), "Wizard.backPressed() set wrong page");
		assertFalse(dialog.getBackButton().getEnabled(), "Back button should be disabled on first page");
		assertTrue(dialog.getNextButton().getEnabled(), "Next button should be enabled on first page");

		//move Next to page 2
		dialog.buttonPressed(IDialogConstants.NEXT_ID);
		assertSame(wizard.page2, dialog.getCurrentPage(), "Wizard.backPressed() set wrong page");
		//move Back to page 1
		dialog.buttonPressed(IDialogConstants.BACK_ID);
		assertSame(wizard.page1, dialog.getCurrentPage(), "Wizard.backPressed() set wrong page");
	}

	@Test
	public void testShowPage() {
		//move to page 3
		dialog.nextPressed();
		dialog.nextPressed();
		assertSame(wizard.page3, dialog.getCurrentPage(), "Wizard.nextPressed() set wrong page");

		//showPage() back to page 1
		dialog.showPage(wizard.page1);

		assertSame(wizard.page1, dialog.getCurrentPage(), "Wizard.showPage() set wrong page");

		//TODO Next test fails due to bug #249369
//		assertEquals(false, dialog.getBackButton().getEnabled(), "Back button should be disabled on first page");
		assertTrue(dialog.getNextButton().getEnabled(), "Next button should be enabled on first page");
	}

	@Test
	public void testPageChangeListening() {
		pageChanged = false;
		pageChangingFired = false;

		IPageChangedListener changedListener = event -> pageChanged = true;

		IPageChangingListener changingListener = event -> {
			assertFalse(pageChanged, "Page should not have changed yet");
			pageChangingFired = true;
		};

		//test that listener notifies us of page change
		dialog.addPageChangedListener(changedListener);
		dialog.addPageChangingListener(changingListener); //assert is in the listener
		assertFalse(pageChanged, "Page change notified unintentially");
		//change to page 2
		dialog.nextPressed();
		assertTrue(pageChanged, "Wasn't notified of page change");
		assertTrue(pageChangingFired, "Wasn't notified of page changing");

		dialog.removePageChangingListener(changingListener); //if not removed, its assert will fail on next nextPressed()
		//change to page 2
		dialog.nextPressed();

		//test with listener removed
		pageChanged = false;
		dialog.removePageChangedListener(changedListener);
		//change to page 3
		dialog.nextPressed();
		assertFalse(pageChanged, "Page change notified unintentially");
	}

	@Test
	public void testWizardDispose() {
		wizard.setThrowExceptionOnDispose(true);

		final boolean logged[] = new boolean[1];
		Shell shell;
		ILogger oldLogger = Policy.getLog();
		try {
			Policy.setLog(status -> logged[0] = true);
			shell = dialog.getShell();
			dialog.close();
		} finally {
			Policy.setLog(oldLogger);
		}
		assertTrue(logged[0]);

		shell.dispose();
	}

	@Test
	public void testWizardPageDispose() {
		wizard.page2.setThrowExceptionOnDispose(true);
		final boolean logged[] = new boolean[1];
		ILogger oldLogger = Policy.getLog();
		try {
			Policy.setLog(status -> logged[0] = true);
			dialog.close();
		} finally {
			Policy.setLog(oldLogger);
		}
		assertTrue(logged[0]);
		assertTrue(wizard.page1.getControl().isDisposed());
		assertTrue(wizard.page3.getControl().isDisposed());

	}

	//----------------------------------------------------

	@BeforeEach
	public void setUp() throws Exception {
		DID_FINISH = false;
		color1 = new RGB(255, 0, 0);
		color2 = new RGB(0, 255, 0);

		createWizardDialog();
	}

	@AfterEach
	public void tearDown() throws Exception {
		if(dialog.getShell() != null && ! dialog.getShell().isDisposed()) {
			dialog.close();
		}
	}

	//Create and open the wizard
	protected void createWizardDialog() {
		//ensure we've initialized a display for this thread
		Display.getDefault();

		wizard = new TheTestWizard();
		dialog = new TheTestWizardDialog(null, wizard);
		dialog.create();

		dialog.open();
	}

}
