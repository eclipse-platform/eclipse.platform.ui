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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the quick access UI
 * @since 3.4
 */
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

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private static final int TIMEOUT = 5000;
	// As defined in QuickAccessDialog and in SearchField
	private static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	private static final Predicate<Shell> isQuickAccessShell = shell -> shell.getText()
			.equals(QuickAccessMessages.QuickAccessContents_QuickAccess);
	private IDialogSettings dialogSettings;
	private IWorkbenchWindow activeWorkbenchWindow;


	@Before
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

	@After
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
		assertTrue("Quick access filter should be empty", text.getText().isEmpty());
		assertEquals("Quick access table should be empty", 0, table.getItemCount());

		text.setText("T");
		processEventsUntil(() -> table.getItemCount() > 1, TIMEOUT);
		int oldCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", oldCount > 3);
		assertTrue("Too many quick access items for size of table", oldCount < MAXIMUM_NUMBER_OF_ELEMENTS);
		final String oldFirstItemText = table.getItem(0).getText(1);

		text.setText("B"); // The letter mustn't be part of the previous 1st proposal
		assertTrue("The quick access items should have changed", DisplayHelper.waitForCondition(table.getDisplay(),
				TIMEOUT,
				() -> table.getItemCount() > 1 && !table.getItem(0).getText(1).equals(oldFirstItemText)));
		int newCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", newCount > 3);
		assertTrue("Too many quick access items for size of table", newCount < MAXIMUM_NUMBER_OF_ELEMENTS);
	}

	@Test
	public void testContributedElement() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		final Table table = dialog.getQuickAccessContents().getTable();
		Text text = dialog.getQuickAccessContents().getFilterText();
		assertTrue("Quick access filter should be empty", text.getText().isEmpty());
		assertEquals("Quick access table should be empty", 0, table.getItemCount());

		text.setText(TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL);
		assertTrue("Missing contributed element", DisplayHelper.waitForCondition(dialog.getShell().getDisplay(), TIMEOUT, () ->
				dialogContains(dialog, TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL))
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
		assertTrue("UI Frozen on text change",
				System.currentTimeMillis() - duration < TestLongRunningQuickAccessComputer.DELAY);
		assertTrue("Missing contributed element", DisplayHelper.waitForCondition(dialog.getShell().getDisplay(), TestLongRunningQuickAccessComputer.DELAY + TIMEOUT, () ->
			dialogContains(dialog, TestLongRunningQuickAccessComputer.THE_ELEMENT.getLabel())
		));
		table.select(0);
		activateCurrentElement(dialog);
		duration = System.currentTimeMillis();
		QuickAccessDialog secondDialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		secondDialog.open();
		assertTrue(System.currentTimeMillis() - duration < TestLongRunningQuickAccessComputer.DELAY);
		AtomicLong tick = new AtomicLong(System.currentTimeMillis());
		AtomicLong maxBlockedUIThread = new AtomicLong();
		assertTrue("Missing contributed element as previous pick", DisplayHelper.waitForCondition(
				secondDialog.getShell().getDisplay(), TestLongRunningQuickAccessComputer.DELAY + TIMEOUT, () -> {
							long currentTick = System.currentTimeMillis();
							long previousTick = tick.getAndSet(currentTick);
							long currentDelayInUIThread = currentTick - previousTick;
							maxBlockedUIThread.set(Math.max(maxBlockedUIThread.get(), currentDelayInUIThread));
							return dialogContains(secondDialog,
									TestLongRunningQuickAccessComputer.THE_ELEMENT.getLabel());
						}));
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
		assertTrue("Quick access filter should be empty", text.getText().isEmpty());
		assertEquals("Quick access table should be empty", 0, table.getItemCount());

		// Set a filter to get some items
		text.setText("T");
		processEventsUntil(() -> table.getItemCount() > 1, TIMEOUT);
		final int defaultCount = table.getItemCount();
		assertTrue("Not enough quick access items for simple filter", defaultCount > 3);
		assertTrue("Too many quick access items for size of table", defaultCount < MAXIMUM_NUMBER_OF_ELEMENTS);
		final String oldFirstItemText = table.getItem(0).getText(1);

		IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getService(IHandlerService.class);
		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() != defaultCount, TIMEOUT);
		final int allCount = table.getItemCount();
		assertTrue("Turning on show all should display more items", allCount > defaultCount);
		assertEquals("Turning on show all should not change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Run the handler to turn off show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() != allCount, TIMEOUT);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue("Turning off show all should limit items shown", table.getItemCount() < allCount);
		assertEquals("Turning off show all should not change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		processEventsUntil(() -> table.getItemCount() == allCount, TIMEOUT);
		assertEquals("Turning on show all twice shouldn't change the items", allCount, table.getItemCount());
		assertEquals("Turning on show all twice shouldn't change the top item", oldFirstItemText, table.getItem(0).getText(1));

		// Close and reopen the shell
		dialog.close();
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		dialog = findQuickAccessDialog().get();
		text = dialog.getQuickAccessContents().getFilterText();
		Table newTable = dialog.getQuickAccessContents().getTable();
		text.setText("T");
		processEventsUntil(() -> newTable.getItemCount() > 1, TIMEOUT);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue("Show all should be turned off when the shell is closed and reopened",
				newTable.getItemCount() < allCount);
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
		assertTrue("Missing entry", DisplayHelper.waitForCondition(firstTable.getDisplay(),
				TIMEOUT,
				() -> dialogContains(dialog, quickAccessElementText)));
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
		assertTrue("Missing entry in previous pick",
				DisplayHelper.waitForCondition(secondDialog.getShell().getDisplay(), TIMEOUT,
						() -> dialogContains(secondDialog, quickAccessElementText)));
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
		assertTrue("Contributed item not found in previous choices",
				DisplayHelper.waitForCondition(secondDialog.getShell().getDisplay(), TIMEOUT,
						() -> getAllEntries(secondDialog.getQuickAccessContents().getTable()).stream()
								.anyMatch(TestQuickAccessComputer::isContributedItem)));
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
		assertTrue("Contributed item not found in previous choices",
				DisplayHelper.waitForCondition(secondTable.getDisplay(), TIMEOUT, //
						() -> getAllEntries(secondTable).stream()
								.anyMatch(TestIncrementalQuickAccessComputer::isContributedItem)
				));
	}

	@Test
	public void testPrefixMatchHavePriority() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table table = dialog.getQuickAccessContents().getTable();
		text.setText("P");
		assertTrue("Not enough quick access items for simple filter",
				DisplayHelper.waitForCondition(table.getDisplay(), TIMEOUT, () -> table.getItemCount() > 3));
		assertTrue("Non-prefix match first", table.getItem(0).getText(1).toLowerCase().startsWith("p"));
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
		assertTrue("Not enough quick access items for simple filter",
				DisplayHelper.waitForCondition(table.getDisplay(), TIMEOUT, () -> table.getItemCount() > 1));
		assertTrue("Non-prefix match first", table.getItem(0).getText(1).toLowerCase().startsWith("toggle split"));
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
