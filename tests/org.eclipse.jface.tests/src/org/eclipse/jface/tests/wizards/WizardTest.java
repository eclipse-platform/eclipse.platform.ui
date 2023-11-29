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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		assertEquals("Wizard has wrong number of pages", NUM_PAGES, wizard.getPageCount());

		//test page name
		assertEquals("WizardPage.getName() returned wrong name", TheTestWizard.page1Name, wizard.page1.getName());

		//test getPage()
		assertSame("Wizard.getPage() returned wrong page", wizard.getPage(TheTestWizard.page1Name), wizard.page1);

		//test title
		wizard.setWindowTitle(WIZARD_TITLE);
		assertEquals("Wizard has wrong title", wizard.getWindowTitle(), WIZARD_TITLE);
		wizard.page1.setTitle(PAGE_TITLE);
		assertEquals("Wizard has wrong title", wizard.page1.getTitle(), PAGE_TITLE);

		//set+test color twice to ensure initial color didn't happen to be color1
		wizard.setTitleBarColor(color1);
		assertEquals("Wizard has wrong title color", wizard.getTitleBarColor(), color1);
		wizard.setTitleBarColor(color2);
		assertEquals("Wizard has wrong title color", wizard.getTitleBarColor(), color2);

		//test on starting page
		assertSame("Wizard has wrong starting page", wizard.page1, wizard.getStartingPage());
		assertSame("Wizard not on starting page", wizard.page1, dialog.getCurrentPage());

		//test getMessage()
		assertSame("WizardPage error message should be null", null, wizard.page1.getErrorMessage());
		wizard.page1.textInputField.setText(TheTestWizardPage.BAD_TEXT_FIELD_CONTENTS);
		assertEquals("WizardPage error message set correctly", TheTestWizardPage.BAD_TEXT_FIELD_STATUS,
				wizard.page1.getErrorMessage());

		//test page completion
		wizard.page1.textInputField.setText(TheTestWizardPage.GOOD_TEXT_FIELD_CONTENTS);
		assertTrue("Page should be completed", wizard.page1.canFlipToNextPage());
		//Setting good value should've cleared the error message
		assertSame("WizardPage error message should be null", null, wizard.page1.getErrorMessage());

		//test getNextPage() without page changes
		assertSame("WizardPage.getNexPage() wrong page", wizard.page2, wizard.page1.getNextPage());
		assertSame("Wizard.getNexPage() wrong page", wizard.page2, wizard.getNextPage(wizard.page1));
		assertSame("WizardPage.getPreviousPage() wrong page", wizard.page1, wizard.page2.getPreviousPage());
		assertSame("Wizard.getPreviousPage() wrong page", wizard.page1, wizard.getPreviousPage(wizard.page2));
		assertSame("WizardPage.getNexPage() wrong page", wizard.page3, wizard.page2.getNextPage());
		assertSame("Wizard.getPreviousPage() wrong page", wizard.page2, wizard.getPreviousPage(wizard.page3));

		//test canFinish()
		wizard.page2.textInputField.setText(TheTestWizardPage.BAD_TEXT_FIELD_CONTENTS);
		assertFalse("Wizard should not be able to finish", wizard.canFinish());
		wizard.page2.textInputField.setText(TheTestWizardPage.GOOD_TEXT_FIELD_CONTENTS);
		assertTrue("Wizard should be able to finish", wizard.canFinish());

		//test simulated Finish button hit
		//TheTestWizard's performFinish() sets DID_FINISH to true
		dialog.finishPressed();
		assertTrue("Wizard didn't perform finish", DID_FINISH);
	}

	@Test
	public void testEndingWithCancel() {
		assertSame("Wizard not on starting page", wizard.page1, dialog.getCurrentPage());

		//TheTestWizard's performFinish() sets DID_FINISH to true, ensure it was not called
		wizard.performCancel();
		assertFalse("Wizard finished but should not have", DID_FINISH);

		dialog.cancelPressed();
		assertFalse("Wizard performed finished but should not have", DID_FINISH);
	}

	@Test
	public void testPageChanging() {
		//initially on first page
		assertSame("Wizard started on wrong page", wizard.page1, dialog.getCurrentPage());
		assertFalse("Back button should be disabled on first page", dialog.getBackButton().getEnabled());
		assertTrue("Next button should be enabled on first page", dialog.getNextButton().getEnabled());

		//move to middle page 2
		dialog.nextPressed();
		assertSame("Wizard.nextPressed() set wrong page", wizard.page2, dialog.getCurrentPage());
		assertTrue("Back button should be enabled on middle page", dialog.getBackButton().getEnabled());
		assertTrue("Next button should be enabled on middle page", dialog.getNextButton().getEnabled());

		//test that can't complete by inserting bad value to be validated
		wizard.page2.textInputField.setText(TheTestWizardPage.BAD_TEXT_FIELD_CONTENTS);
		assertFalse("Finish should be disabled when bad field value", dialog.getFinishedButton().getEnabled());
		assertTrue("Cancel should always be enabled", dialog.getCancelButton().getEnabled());

		//test that can complete by inserting good value to be validated
		wizard.page2.textInputField.setText(TheTestWizardPage.GOOD_TEXT_FIELD_CONTENTS);
		assertTrue("Finish should be enabled when good field value", dialog.getFinishedButton().getEnabled());

		//move to last page 3
		dialog.nextPressed();
		assertSame("Wizard.nextPressed() set wrong page", wizard.page3, dialog.getCurrentPage());
		assertTrue("Back button should be enabled on last page", dialog.getBackButton().getEnabled());
		assertFalse("Next button should be disenabled on last page", dialog.getNextButton().getEnabled());

		//move back to page 2
		dialog.backPressed();
		assertSame("Wizard.backPressed() set wrong page", wizard.page2, dialog.getCurrentPage());
		assertTrue("Back button should be enabled on middle page", dialog.getBackButton().getEnabled());
		assertTrue("Next button should be enabled on middle page", dialog.getNextButton().getEnabled());

		//move back to page 1
		dialog.backPressed();
		assertSame("Wizard.backPressed() set wrong page", wizard.page1, dialog.getCurrentPage());
		assertFalse("Back button should be disabled on first page", dialog.getBackButton().getEnabled());
		assertTrue("Next button should be enabled on first page", dialog.getNextButton().getEnabled());

		//move Next to page 2
		dialog.buttonPressed(IDialogConstants.NEXT_ID);
		assertSame("Wizard.backPressed() set wrong page", wizard.page2, dialog.getCurrentPage());
		//move Back to page 1
		dialog.buttonPressed(IDialogConstants.BACK_ID);
		assertSame("Wizard.backPressed() set wrong page", wizard.page1, dialog.getCurrentPage());
	}

	@Test
	public void testShowPage() {
		//move to page 3
		dialog.nextPressed();
		dialog.nextPressed();
		assertSame("Wizard.nextPressed() set wrong page", wizard.page3, dialog.getCurrentPage());

		//showPage() back to page 1
		dialog.showPage(wizard.page1);

		assertSame("Wizard.showPage() set wrong page", wizard.page1, dialog.getCurrentPage());

		//TODO Next test fails due to bug #249369
//		assertEquals("Back button should be disabled on first page", false, dialog.getBackButton().getEnabled());
		assertTrue("Next button should be enabled on first page", dialog.getNextButton().getEnabled());
	}

	@Test
	public void testPageChangeListening() {
		pageChanged = false;
		pageChangingFired = false;

		IPageChangedListener changedListener = event -> pageChanged = true;

		IPageChangingListener changingListener = event -> {
			assertFalse("Page should not have changed yet", pageChanged);
			pageChangingFired = true;
		};

		//test that listener notifies us of page change
		dialog.addPageChangedListener(changedListener);
		dialog.addPageChangingListener(changingListener); //assert is in the listener
		assertFalse("Page change notified unintentially", pageChanged);
		//change to page 2
		dialog.nextPressed();
		assertTrue("Wasn't notified of page change", pageChanged);
		assertTrue("Wasn't notified of page changing", pageChangingFired);

		dialog.removePageChangingListener(changingListener); //if not removed, its assert will fail on next nextPressed()
		//change to page 2
		dialog.nextPressed();

		//test with listener removed
		pageChanged = false;
		dialog.removePageChangedListener(changedListener);
		//change to page 3
		dialog.nextPressed();
		assertFalse("Page change notified unintentially", pageChanged);
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

	@Before
	public void setUp() throws Exception {
		DID_FINISH = false;
		color1 = new RGB(255, 0, 0);
		color2 = new RGB(0, 255, 0);

		createWizardDialog();
	}

	@After
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
