/*******************************************************************************
 * Copyright (c) 2008, 2022 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.quickaccess.QuickAccessDialog;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests the quick access UI
 * @since 3.4
 */
@ExtendWith(CloseTestWindowsExtension.class)
public class QuickAccessDialogTest {

	private class TestQuickAccessDialog extends QuickAccessDialog {

		public TestQuickAccessDialog(IWorkbenchWindow activeWorkbenchWindow, Command command) {
			super(activeWorkbenchWindow, command);
		}

		@Override
		protected IDialogSettings getDialogSettings() {
			return dialogSettings;
		}
	}

	private static final int TIMEOUT = 5000;
	// As defined in QuickAccessDialog and in SearchField
	private static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	private static final Predicate<Shell> isQuickAccessShell = shell -> shell.getText()
			.equals(QuickAccessMessages.QuickAccessContents_QuickAccess);
	private IDialogSettings dialogSettings;
	private IWorkbenchWindow activeWorkbenchWindow;


	@BeforeEach
	public void setUp() throws Exception {
		Arrays.stream(Display.getDefault().getShells()).filter(isQuickAccessShell).forEach(Shell::close);
		dialogSettings = new DialogSettings("QuickAccessDialogTest" + System.currentTimeMillis());
		activeWorkbenchWindow = openTestWindow();
		QuickAccessDialog warmupDialog = new QuickAccessDialog(activeWorkbenchWindow, null);
		warmupDialog.open();
		warmupDialog.close();
	}

	static Optional<QuickAccessDialog> findQuickAccessDialog() {
		return Arrays.stream(Display.getDefault().getShells()).filter(isQuickAccessShell).findAny().map(Shell::getData)
				.map(QuickAccessDialog.class::cast);
	}

	@AfterEach
	public void tearDown() throws Exception {
		Arrays.stream(Display.getDefault().getShells()).filter(isQuickAccessShell)
				.forEach(Shell::close);
	}

	/**
	 * Tests that the shell opens when the command is activated
	 */
	@Test
	public void testOpenByCommand() throws Exception {
		IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
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
	@Test
	public void testTextFilter(){
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table table = dialog.getQuickAccessContents().getTable();
		assertTrue(text.getText().isEmpty(), "Quick access filter should be empty");
		assertEquals(0, table.getItemCount(), "Quick access table should be empty");

		text.setText("T");
		processEventsUntil(() -> table.getItemCount() > 1, TIMEOUT);
		int oldCount = table.getItemCount();
		assertTrue(oldCount > 3, "Not enough quick access items for simple filter");
		assertTrue(oldCount < MAXIMUM_NUMBER_OF_ELEMENTS, "Too many quick access items for size of table");
		final String oldFirstItemText = table.getItem(0).getText(1);

		text.setText("B"); // The letter mustn't be part of the previous 1st proposal
		assertTrue(DisplayHelper.waitForCondition(table.getDisplay(),
				TIMEOUT,
				() -> table.getItemCount() > 1 && !table.getItem(0).getText(1).equals(oldFirstItemText)),
				"The quick access items should have changed");
		int newCount = table.getItemCount();
		assertTrue(newCount > 3, "Not enough quick access items for simple filter");
		assertTrue(newCount < MAXIMUM_NUMBER_OF_ELEMENTS, "Too many quick access items for size of table");
	}

	@Test
	public void testContributedElement() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		final Table table = dialog.getQuickAccessContents().getTable();
		Text text = dialog.getQuickAccessContents().getFilterText();
		assertTrue(text.getText().isEmpty(), "Quick access filter should be empty");
		assertEquals(0, table.getItemCount(), "Quick access table should be empty");

		text.setText(TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL);
		assertTrue(DisplayHelper.waitForCondition(dialog.getShell().getDisplay(), TIMEOUT, () ->
				dialogContains(dialog, TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL)),
				"Missing contributed element"
		);
	}

	@Test
	public void testLongRunningComputerDoesntFreezeUI() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		final Table table = dialog.getQuickAccessContents().getTable();
		Text text = dialog.getQuickAccessContents().getFilterText();
		long duration = System.currentTimeMillis();
		text.setText(TestLongRunningQuickAccessComputer.THE_ELEMENT.getId());
		assertTrue(System.currentTimeMillis() - duration < TestLongRunningQuickAccessComputer.DELAY,
				"UI Frozen on text change");
		assertTrue(DisplayHelper.waitForCondition(dialog.getShell().getDisplay(), TestLongRunningQuickAccessComputer.DELAY + TIMEOUT, () ->
			dialogContains(dialog, TestLongRunningQuickAccessComputer.THE_ELEMENT.getLabel())
		), "Missing contributed element");
		table.select(0);
		activateCurrentElement(dialog);
		duration = System.currentTimeMillis();
		QuickAccessDialog secondDialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		secondDialog.open();
		assertTrue(System.currentTimeMillis() - duration < TestLongRunningQuickAccessComputer.DELAY);
		AtomicLong tick = new AtomicLong(System.currentTimeMillis());
		AtomicLong maxBlockedUIThread = new AtomicLong();
		assertTrue(DisplayHelper.waitForCondition(
				secondDialog.getShell().getDisplay(), TestLongRunningQuickAccessComputer.DELAY + TIMEOUT, () -> {
							long currentTick = System.currentTimeMillis();
							long previousTick = tick.getAndSet(currentTick);
							long currentDelayInUIThread = currentTick - previousTick;
							maxBlockedUIThread.set(Math.max(maxBlockedUIThread.get(), currentDelayInUIThread));
							return dialogContains(secondDialog,
									TestLongRunningQuickAccessComputer.THE_ELEMENT.getLabel());
						}), "Missing contributed element as previous pick");
		assertTrue(maxBlockedUIThread.get() < TestLongRunningQuickAccessComputer.DELAY);
	}

	/**
	 * Tests that activating the handler again toggles the show all setting and that the setting changes the results
	 * Also tests that closing and reopening the shell resets show all
	 */
	@Test
	public void testShowAll() throws Exception {
		// Open the shell
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		final Table table = dialog.getQuickAccessContents().getTable();
		assertTrue(text.getText().isEmpty(), "Quick access filter should be empty");
		assertEquals(0, table.getItemCount(), "Quick access table should be empty");

		// Set a filter to get some items
		text.setText("T");
		processEventsUntil(() -> table.getItemCount() > 1, TIMEOUT);
		final int defaultCount = table.getItemCount();
		assertTrue(defaultCount > 3, "Not enough quick access items for simple filter");
		assertTrue(defaultCount < MAXIMUM_NUMBER_OF_ELEMENTS, "Too many quick access items for size of table");
		final String oldFirstItemText = table.getItem(0).getText(1);

		IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getService(IHandlerService.class);
		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() != defaultCount, TIMEOUT);
		final int allCount = table.getItemCount();
		assertTrue(allCount > defaultCount, "Turning on show all should display more items");
		assertEquals(oldFirstItemText, table.getItem(0).getText(1), "Turning on show all should not change the top item");

		// Run the handler to turn off show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() != allCount, TIMEOUT);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue(table.getItemCount() < allCount, "Turning off show all should limit items shown");
		assertEquals(oldFirstItemText, table.getItem(0).getText(1), "Turning off show all should not change the top item");

		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() == allCount, TIMEOUT);
		assertEquals(allCount, table.getItemCount(), "Turning on show all twice shouldn't change the items");
		assertEquals(oldFirstItemText, table.getItem(0).getText(1), "Turning on show all twice shouldn't change the top item");

		// Close and reopen the shell
		dialog.close();
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		dialog = findQuickAccessDialog().get();
		text = dialog.getQuickAccessContents().getFilterText();
		Table newTable = dialog.getQuickAccessContents().getTable();
		text.setText("T");
		processEventsUntil(() -> newTable.getItemCount() > 1, TIMEOUT);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue(newTable.getItemCount() < allCount,
				"Show all should be turned off when the shell is closed and reopened");
	}

	@Test
	public void testPreviousChoicesAvailable() {
		// add one selection to history
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table firstTable = dialog.getQuickAccessContents().getTable();
		String quickAccessElementText = "Project Explorer";
		text.setText(quickAccessElementText);
		assertTrue(DisplayHelper.waitForCondition(firstTable.getDisplay(),
				TIMEOUT,
				() -> dialogContains(dialog, quickAccessElementText)), "Missing entry");
		firstTable.select(0);
		activateCurrentElement(dialog);
		assertNotEquals(0, dialogSettings.getArray("orderedElements").length);
		processEventsUntil(
				() -> activeWorkbenchWindow.getActivePage() != null
						&& activeWorkbenchWindow.getActivePage().getActivePart() != null
						&& quickAccessElementText
								.equalsIgnoreCase(activeWorkbenchWindow.getActivePage().getActivePart().getTitle()),
				TIMEOUT);
		// then try in a new SearchField
		QuickAccessDialog secondDialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		secondDialog.open();
		assertTrue(DisplayHelper.waitForCondition(secondDialog.getShell().getDisplay(), TIMEOUT,
						() -> dialogContains(secondDialog, quickAccessElementText)),
				"Missing entry in previous pick");
	}

	private void activateCurrentElement(QuickAccessDialog dialog) {
		Event enterPressed = new Event();
		enterPressed.widget = dialog.getQuickAccessContents().getFilterText();
		enterPressed.keyCode = SWT.CR;
		enterPressed.widget.notifyListeners(SWT.KeyDown, enterPressed);
		processEventsUntil(() -> enterPressed.widget.isDisposed(), 500);
	}

	@Test
	public void testPreviousChoicesAvailableForExtension() {
		// add one selection to history
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		text.setText(TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL);
		final Table firstTable = dialog.getQuickAccessContents().getTable();
		assertTrue(DisplayHelper.waitForCondition(text.getDisplay(), TIMEOUT,
				() -> dialogContains(dialog, TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL)));
		firstTable.select(0);
		activateCurrentElement(dialog);
		// then try in a new SearchField
		QuickAccessDialog secondDialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		secondDialog.open();
		assertTrue(DisplayHelper.waitForCondition(secondDialog.getShell().getDisplay(), TIMEOUT,
						() -> getAllEntries(secondDialog.getQuickAccessContents().getTable()).stream()
								.anyMatch(TestQuickAccessComputer::isContributedItem)),
				"Contributed item not found in previous choices");
	}

	@Test
	public void testPreviousChoicesAvailableForIncrementalExtension() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		text.setText(TestIncrementalQuickAccessComputer.ENABLEMENT_QUERY);
		final Table firstTable = dialog.getQuickAccessContents().getTable();
		assertTrue(DisplayHelper.waitForCondition(text.getDisplay(), //
				TIMEOUT, //
				() -> firstTable.getItemCount() > 0
						&& TestIncrementalQuickAccessComputer.isContributedItem(getAllEntries(firstTable).get(0))
		));
		firstTable.select(0);
		activateCurrentElement(dialog);
		// then try in a new SearchField
		dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		final Table secondTable = dialog.getQuickAccessContents().getTable();
		assertTrue(DisplayHelper.waitForCondition(secondTable.getDisplay(), TIMEOUT, //
						() -> getAllEntries(secondTable).stream()
								.anyMatch(TestIncrementalQuickAccessComputer::isContributedItem)),
				"Contributed item not found in previous choices");
	}

	@Test
	public void testPrefixMatchHavePriority() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table table = dialog.getQuickAccessContents().getTable();
		text.setText("P");
		assertTrue(DisplayHelper.waitForCondition(table.getDisplay(), TIMEOUT, () -> table.getItemCount() > 3),
				"Not enough quick access items for simple filter");
		assertTrue(table.getItem(0).getText(1).toLowerCase().startsWith("p"), "Non-prefix match first");
	}

	@Test
	public void testCommandEnableContext() throws Exception {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("org.eclipse.ui.window.splitEditor");
		assertTrue(command.isDefined());

		File tmpFile = File.createTempFile("blah", ".txt");
		tmpFile.deleteOnExit();
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), tmpFile.toURI(),
				"org.eclipse.ui.DefaultTextEditor", true);

		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table table = dialog.getQuickAccessContents().getTable();
		text.setText("Toggle Split");
		assertTrue(DisplayHelper.waitForCondition(table.getDisplay(), TIMEOUT, () -> table.getItemCount() > 1),
				"Not enough quick access items for simple filter");
		assertTrue(table.getItem(0).getText(1).toLowerCase().startsWith("toggle split"), "Non-prefix match first");
	}

	private List<String> getAllEntries(Table table) {
		if (table == null || table.isDisposed()) {
			return Collections.emptyList();
		}
		final int nbColumns = table.getColumnCount();
		return Arrays.stream(table.getItems()).map(item -> {
			StringBuilder res = new StringBuilder();
			for (int i = 0; i < nbColumns; i++) {
				res.append(item.getText(i));
				res.append(" | ");
			}
			return res.toString();
		}).toList();
	}

	private boolean dialogContains(QuickAccessDialog dialog, String substring) {
		return getAllEntries(dialog.getQuickAccessContents().getTable()).stream()
				.anyMatch(label -> label.toLowerCase().contains(substring.toLowerCase()));
	}
}
