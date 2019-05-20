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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 504029
 ******************************************************************************/

package org.eclipse.ui.tests.quickaccess;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.quickaccess.QuickAccessDialog;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the quick access UI
 * @since 3.4
 */
public class QuickAccessDialogTest extends UITestCase {

	// As defined in QuickAccessDialog and in SearchField
	private static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	private static final Predicate<Shell> isQuickAccessShell = shell -> shell.getText()
			.equals(QuickAccessMessages.QuickAccessContents_QuickAccess);

	/**
	 * @param testName
	 */
	public QuickAccessDialogTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		Arrays.stream(Display.getDefault().getShells()).filter(isQuickAccessShell).forEach(Shell::close);
	}

	static Optional<QuickAccessDialog> findQuickAccessDialog() {
		return Arrays.stream(Display.getDefault().getShells()).filter(isQuickAccessShell).findAny().map(Shell::getData)
				.map(QuickAccessDialog.class::cast);
	}

	@Override
	protected void doTearDown() throws Exception {
		Arrays.stream(Display.getDefault().getShells()).filter(isQuickAccessShell)
				.forEach(Shell::close);
	}

	/**
	 * Tests that the shell opens when the command is activated
	 * @throws Exception
	 */
	public void testOpenByCommand() throws Exception {
		IHandlerService handlerService = getWorkbench().getActiveWorkbenchWindow()
				.getService(IHandlerService.class);
		Set<Shell> formerShells = new HashSet<>(Arrays.asList(Display.getDefault().getShells()));
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		Set<Shell> newShells = new HashSet<>(Arrays.asList(Display.getDefault().getShells()));
		assertEquals(formerShells.size() + 1, newShells.size());
		newShells.removeAll(formerShells);
		assertEquals(1, newShells.size());
		assertTrue(isQuickAccessShell.test(newShells.iterator().next()));
	}

	/**
	 * Test that changing the filter text works correctly
	 */
	public void testTextFilter(){
		QuickAccessDialog dialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null) {
			@Override
			protected IDialogSettings getDialogSettings() {
				return new DialogSettings("not-persisted");
			}
		};
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table table = dialog.getQuickAccessContents().getTable();
		assertTrue("Quick access filter should be empty", text.getText().isEmpty());
		assertTrue("Quick access table should be empty", table.getItemCount() == 0);

		text.setText("T");
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		int oldCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", oldCount > 3);
		assertTrue("Too many quick access items for size of table", oldCount < MAXIMUM_NUMBER_OF_ELEMENTS);
		final String oldFirstItemText = table.getItem(0).getText(1);

		text.setText("E");
		processEventsUntil(() -> table.getItemCount() > 1 && !table.getItem(0).getText(1).equals(oldFirstItemText),
				200);
		String newFirstItemText = table.getItem(0).getText(1);
		assertNotSame("The quick access items should have changed", newFirstItemText, oldFirstItemText);
		int newCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", newCount > 3);
		assertTrue("Too many quick access items for size of table", newCount < MAXIMUM_NUMBER_OF_ELEMENTS);
	}

	public void testContributedElement() {
		QuickAccessDialog dialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null) {
			@Override
			protected IDialogSettings getDialogSettings() {
				return new DialogSettings("not-persisted"); //$NON-NLS-1$
			}
		};
		dialog.open();
		final Table table = dialog.getQuickAccessContents().getTable();
		Text text = dialog.getQuickAccessContents().getFilterText();
		assertTrue("Quick access filter should be empty", text.getText().isEmpty());
		assertTrue("Quick access table should be empty", table.getItemCount() == 0);

		text.setText(TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL);
		processEventsUntil(() -> table.getItemCount() > 0, 200);
		assertTrue("Missing contributed element", Arrays.stream(table.getItems())
				.anyMatch(item -> item.getText(1).equals(TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL)));
	}

	/**
	 * Tests that activating the handler again toggles the show all setting and that the setting changes the results
	 * Also tests that closing and reopening the shell resets show all
	 */
	public void testShowAll() throws Exception {
		// Open the shell
		QuickAccessDialog dialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null) {
			@Override
			protected IDialogSettings getDialogSettings() {
				return new DialogSettings("not-persisted"); //$NON-NLS-1$
			}
		};
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		final Table table = dialog.getQuickAccessContents().getTable();
		assertTrue("Quick access filter should be empty", text.getText().isEmpty());
		assertTrue("Quick access table should be empty", table.getItemCount() == 0);

		// Set a filter to get some items
		text.setText("T");
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		final int oldCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", oldCount > 3);
		assertTrue("Too many quick access items for size of table", oldCount < MAXIMUM_NUMBER_OF_ELEMENTS);
		final String oldFirstItemText = table.getItem(0).getText(1);

		IHandlerService handlerService = getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() != oldCount, 200);
		final int newCount = table.getItemCount();
		assertTrue("Turning on show all should display more items", newCount > oldCount);
		assertEquals("Turning on show all should not change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Run the handler to turn off show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() != newCount, 200);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue("Turning off show all should limit items shown", table.getItemCount() < newCount);
		assertEquals("Turning off show all should not change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() != oldCount, 200);
		assertEquals("Turning on show all twice shouldn't change the items", newCount, table.getItemCount());
		assertEquals("Turning on show all twice shouldn't change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Close and reopen the shell
		dialog.close();
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		dialog = findQuickAccessDialog().get();
		text = dialog.getQuickAccessContents().getFilterText();
		Table newTable = dialog.getQuickAccessContents().getTable();
		text.setText("T");
		processEventsUntil(() -> newTable.getItemCount() > 1, 200);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue("Show all should be turned off when the shell is closed and reopened",
				newTable.getItemCount() < newCount);
	}

	public void testPreviousChoicesAvailable() {
		// add one selection to history
		QuickAccessDialog dialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table firstTable = dialog.getQuickAccessContents().getTable();
		String quickAccessElementText = "Project Explorer";
		text.setText(quickAccessElementText);
		processEventsUntil(() -> getAllEntries(firstTable).get(0).toLowerCase()
				.contains(quickAccessElementText.toLowerCase()), 200);
		firstTable.select(0);
		Event enterPressed = new Event();
		enterPressed.widget = text;
		enterPressed.keyCode = SWT.CR;
		text.notifyListeners(SWT.KeyDown, enterPressed);
		processEventsUntil(() -> text.isDisposed(), 500);
		// then try in a new SearchField
		dialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null);
		dialog.open();
		final Table secondTable = dialog.getQuickAccessContents().getTable();
		processEventsUntil(() -> secondTable.getItemCount() > 1, 500);
		assertTrue(getAllEntries(secondTable).get(0).contains(quickAccessElementText));
	}

	public void testPreviousChoicesAvailableForExtension() {
		// add one selection to history
		QuickAccessDialog dialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		text.setText(TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL);
		final Table firstTable = dialog.getQuickAccessContents().getTable();
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return TestQuickAccessComputer.isContributedItem(getAllEntries(firstTable).get(0));
			}
		}.waitForCondition(text.getDisplay(), 200));
		firstTable.select(0);
		Event enterPressed = new Event();
		enterPressed.widget = text;
		enterPressed.keyCode = SWT.CR;
		text.notifyListeners(SWT.KeyDown, enterPressed);
		processEventsUntil(() -> text.isDisposed(), 500);
		// then try in a new SearchField
		dialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null);
		dialog.open();
		final Table secondTable = dialog.getQuickAccessContents().getTable();
		assertTrue("Contributed item not found in previous choices", new DisplayHelper() { //$NON-NLS-1$
			@Override
			protected boolean condition() {
				return getAllEntries(secondTable).stream().anyMatch(TestQuickAccessComputer::isContributedItem);
			}
		}.waitForCondition(secondTable.getDisplay(), 500));
	}

	private List<String> getAllEntries(Table table) {
		final int nbColumns = table.getColumnCount();
		return Arrays.stream(table.getItems()).map(item -> {
			StringBuilder res = new StringBuilder();
			for (int i = 0; i < nbColumns; i++) {
				res.append(item.getText(i));
				res.append(" | ");
			}
			return res.toString();
		}).collect(Collectors.toList());
	}

}
