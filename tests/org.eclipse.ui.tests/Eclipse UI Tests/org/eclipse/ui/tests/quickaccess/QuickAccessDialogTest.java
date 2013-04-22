/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.quickaccess;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.internal.quickaccess.SearchField;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the quick access UI
 * @since 3.4
 */
public class QuickAccessDialogTest extends UITestCase {

	private SearchField searchField;

	/**
	 * @param testName
	 */
	public QuickAccessDialogTest(String testName) {
		super(testName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	@Override
	protected void doSetUp() throws Exception {
		WorkbenchWindow workbenchWindow = (WorkbenchWindow) getWorkbench()
				.getActiveWorkbenchWindow();
		MWindow window = workbenchWindow.getModel();
		EModelService modelService = window.getContext().get(
				EModelService.class);
		MToolControl control = (MToolControl) modelService.find(
				"SearchField", window); //$NON-NLS-1$
		searchField = (SearchField) control.getObject();
		assertNotNull("Search Field must exist", searchField);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
	@Override
	protected void doTearDown() throws Exception {
		Text text = searchField.getQuickAccessSearchText();
		if (text != null){
			text.setText("");
		}
		Shell shell = searchField.getQuickAccessShell();
		if (shell != null){
			shell.setVisible(false);
		}
	}

	/**
	 * Tests that the shell opens when the command is activated
	 * @throws Exception
	 */
	public void testOpenByCommand() throws Exception {
		IHandlerService handlerService = (IHandlerService) getWorkbench().getActiveWorkbenchWindow()
				.getService(IHandlerService.class);
		Shell shell = searchField.getQuickAccessShell();
		assertFalse("Quick access dialog should not be visible yet", shell.isVisible());
		handlerService
		.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		assertTrue("Quick access dialog should be visible now", shell.isVisible());
	}

	/**
	 * Tests that typing in the text field opens the shell
	 */
	public void testOpenByText(){
		Shell shell = searchField.getQuickAccessShell();
		assertFalse("Quick access dialog should not be visible yet", shell.isVisible());
		Text text = searchField.getQuickAccessSearchText();
		text.setText("Test");
		assertTrue("Quick access dialog should be visible now", shell.isVisible());
	}

	/**
	 * Test that changing the filter text works correctly
	 */
	public void testTextFilter(){
		final Table table = searchField.getQuickAccessTable();
		Text text = searchField.getQuickAccessSearchText();
		assertTrue("Quick access table should say to start typing", table.getItemCount() == 1);
		assertSame("Quick access table should say to start typing", QuickAccessMessages.QuickAccess_StartTypingToFindMatches, table.getItem(0).getText(1));

		text.setText("T");
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() > 1;
			};
		}, 200);
		int oldCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", oldCount > 3);
		assertTrue("Too many quick access items for size of table", oldCount < 30);
		final String oldFirstItemText = table.getItem(0).getText(1);

		text.setText("E");
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() > 1 && !table.getItem(0).getText(1).equals(oldFirstItemText);
			};
		}, 200);
		String newFirstItemText = table.getItem(0).getText(1);
		assertNotSame("The quick access items should have changed", newFirstItemText, oldFirstItemText);
		int newCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", newCount > 3);
		assertTrue("Too many quick access items for size of table", newCount < 30);

		text.setText("QWERTYUIOPTEST");
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() == 1;
			};
		}, 200);
		assertTrue("Quick access table should say no results found", table.getItemCount() == 1);
		assertSame("Quick access table should say no results found", QuickAccessMessages.QuickAccessContents_NoMatchingResults, table.getItem(0).getText());

		text.setText("");
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() == 1;
			};
		}, 200);
		assertTrue("Quick access table should say to start typing", table.getItemCount() == 1);
		assertSame("Quick access table should say to start typing", QuickAccessMessages.QuickAccess_StartTypingToFindMatches, table.getItem(0).getText(1));
	}

	/**
	 * Tests that activating the handler again toggles the show all setting and that the setting changes the results
	 * Also tests that closing and reopening the shell resets show all
	 */
	public void testShowAll() throws Exception {
		// Open the shell
		IHandlerService handlerService = (IHandlerService) getWorkbench().getActiveWorkbenchWindow()
				.getService(IHandlerService.class);
		Shell shell = searchField.getQuickAccessShell();
		assertFalse("Quick access dialog should not be visible yet", shell.isVisible());
		handlerService
		.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		assertTrue("Quick access dialog should be visible now", shell.isVisible());
		final Table table = searchField.getQuickAccessTable();
		Text text = searchField.getQuickAccessSearchText();
		assertTrue("Quick access table should say to start typing", table.getItemCount() == 1);
		assertSame("Quick access table should say to start typing", QuickAccessMessages.QuickAccess_StartTypingToFindMatches, table.getItem(0).getText(1));

		// Set a filter to get some items
		text.setText("T");
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() > 1;
			};
		}, 200);
		final int oldCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", oldCount > 3);
		assertTrue("Too many quick access items for size of table", oldCount < 30);
		final String oldFirstItemText = table.getItem(0).getText(1);

		// Run the handler to turn on show all
		handlerService
		.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() != oldCount;
			};
		}, 200);
		final int newCount = table.getItemCount();
		assertTrue("Turning on show all should display more items", newCount > oldCount);
		assertEquals("Turning on show all should not change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Run the handler to turn off show all 
		handlerService
		.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() != newCount;
			};
		}, 200);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue("Turning off show all should limit items shown", table.getItemCount() < newCount);
		assertEquals("Turning off show all should not change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Run the handler to turn on show all
		handlerService
		.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() != oldCount;
			};
		}, 200);
		assertEquals("Turning on show all twice shouldn't change the items", newCount, table.getItemCount());
		assertEquals("Turning on show all twice shouldn't change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Close and reopen the shell
		shell.setVisible(false);
		handlerService
		.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		text.setText("T");
		processEventsUntil(new Condition() {
			public boolean compute() {
				return table.getItemCount() > 1;
			};
		}, 200);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue("Show all should be turned off when the shell is closed and reopened", table.getItemCount() < newCount);
	}

}
