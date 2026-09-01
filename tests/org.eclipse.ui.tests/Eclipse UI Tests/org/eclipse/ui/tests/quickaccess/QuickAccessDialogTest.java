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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.quickaccess.QuickAccessContents;
import org.eclipse.ui.internal.quickaccess.QuickAccessDialog;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
	// Safety bound only; consumed if the compute genuinely hangs, never on a healthy run.
	private static final int COMPUTE_TIMEOUT = 30000;
	// Generous bound for the one-time cold initialization of the lazy providers.
	private static final int WARMUP_TIMEOUT = 60000;
	// As defined in QuickAccessDialog and in SearchField
	private static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	private static final Predicate<Shell> isQuickAccessShell = shell -> shell.getText()
			.equals(QuickAccessMessages.QuickAccessContents_QuickAccess);
	private IDialogSettings dialogSettings;
	private IWorkbenchWindow activeWorkbenchWindow;

	@BeforeAll
	public static void warmUpProviders() {
		Policy.DEBUG_QUICK_ACCESS = true;
		// The lazy providers do their expensive first query once per JVM. Pay it here
		// with a real filter, an empty one skips them, so it never lands in a timed wait.
		QuickAccessDialog warmupDialog = new QuickAccessDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
				null);
		warmupDialog.open();
		QuickAccessContents contents = warmupDialog.getQuickAccessContents();
		contents.getFilterText().setText("t");
		DisplayHelper.waitForCondition(contents.getTable().getDisplay(), WARMUP_TIMEOUT,
				() -> contents.isSettled() && contents.getTable().getItemCount() > 0);
		warmupDialog.close();
	}

	@AfterAll
	public static void disableDebugOutputs() {
		Policy.DEBUG_QUICK_ACCESS = false;
	}

	@BeforeEach
	public void setUp() throws Exception {
		Arrays.stream(Display.getDefault().getShells()).filter(isQuickAccessShell).forEach(Shell::close);
		dialogSettings = new DialogSettings("QuickAccessDialogTest" + System.currentTimeMillis());
		activeWorkbenchWindow = openTestWindow();
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
		waitForQuickAccessResults(dialog);
		int oldCount = table.getItemCount();
		assertTrue(oldCount > 3, "Not enough quick access items for simple filter");
		assertTrue(oldCount < MAXIMUM_NUMBER_OF_ELEMENTS, "Too many quick access items for size of table");
		final String oldFirstItemText = table.getItem(0).getText(1);

		text.setText("B"); // The letter mustn't be part of the previous 1st proposal
		waitForQuickAccessResults(dialog);
		assertTrue(table.getItemCount() > 1 && !table.getItem(0).getText(1).equals(oldFirstItemText),
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
		waitForQuickAccessResults(dialog);
		assertTrue(dialogContains(dialog, TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL),
				"Missing contributed element");
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
		// Probe UI responsiveness with a self-rescheduling timer: it keeps firing
		// while the UI thread can dispatch, so only a real freeze yields a large gap.
		// The waitForCondition tick cannot tell a freeze from an idle Display.sleep().
		Display display = secondDialog.getShell().getDisplay();
		AtomicLong lastTick = new AtomicLong(System.currentTimeMillis());
		AtomicLong maxBlockedUIThread = new AtomicLong();
		boolean[] monitoring = { true };
		Runnable[] responsivenessProbe = new Runnable[1];
		responsivenessProbe[0] = () -> {
			long now = System.currentTimeMillis();
			maxBlockedUIThread.set(Math.max(maxBlockedUIThread.get(), now - lastTick.getAndSet(now)));
			if (monitoring[0]) {
				display.timerExec(50, responsivenessProbe[0]);
			}
		};
		display.timerExec(50, responsivenessProbe[0]);
		try {
			assertTrue(DisplayHelper.waitForCondition(display, TestLongRunningQuickAccessComputer.DELAY + TIMEOUT,
					() -> dialogContains(secondDialog, TestLongRunningQuickAccessComputer.THE_ELEMENT.getLabel())),
					"Missing contributed element as previous pick");
		} finally {
			monitoring[0] = false;
			display.timerExec(-1, responsivenessProbe[0]);
		}
		assertTrue(maxBlockedUIThread.get() < TestLongRunningQuickAccessComputer.DELAY,
				"UI thread blocked for " + maxBlockedUIThread.get() + "ms while restoring a previous pick");
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
		waitForQuickAccessResults(dialog);
		final int defaultCount = table.getItemCount();
		assertTrue(defaultCount > 3, "Not enough quick access items for simple filter");
		assertTrue(defaultCount < MAXIMUM_NUMBER_OF_ELEMENTS, "Too many quick access items for size of table");
		final String oldFirstItemText = table.getItem(0).getText(1);

		IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getService(IHandlerService.class);
		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		waitForQuickAccessResults(dialog);
		final int allCount = table.getItemCount();
		assertTrue(allCount > defaultCount, "Turning on show all should display more items");
		assertEquals(oldFirstItemText, table.getItem(0).getText(1), "Turning on show all should not change the top item");

		// Run the handler to turn off show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		waitForQuickAccessResults(dialog);
		// Note: The table count may one off from the old count because of shell resizing (scroll bars being added then removed)
		assertTrue(table.getItemCount() < allCount, "Turning off show all should limit items shown");
		assertEquals(oldFirstItemText, table.getItem(0).getText(1), "Turning off show all should not change the top item");

		// Run the handler to turn on show all
		handlerService.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		waitForQuickAccessResults(dialog);
		assertEquals(allCount, table.getItemCount(), "Turning on show all twice shouldn't change the items");
		assertEquals(oldFirstItemText, table.getItem(0).getText(1), "Turning on show all twice shouldn't change the top item");

		// Reopening through the handler would yield a dialog on the shared workbench
		// settings, whose previous picks accumulate across tests.
		dialog.close();
		dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		text = dialog.getQuickAccessContents().getFilterText();
		Table newTable = dialog.getQuickAccessContents().getTable();
		text.setText("T");
		waitForQuickAccessResults(dialog);
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
		waitForQuickAccessResults(dialog);
		assertTrue(dialogContains(dialog, quickAccessElementText), "Missing entry");
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
		waitForQuickAccessResults(secondDialog);
		assertTrue(dialogContains(secondDialog, quickAccessElementText), "Missing entry in previous pick");
	}

	private void activateCurrentElement(QuickAccessDialog dialog) {
		Event enterPressed = new Event();
		enterPressed.widget = dialog.getQuickAccessContents().getFilterText();
		enterPressed.keyCode = SWT.CR;
		enterPressed.widget.notifyListeners(SWT.KeyDown, enterPressed);
		processEventsUntil(() -> enterPressed.widget.isDisposed(), TIMEOUT);
		// Otherwise the pick was never recorded and a later assertion fails for an
		// unrelated reason.
		assertTrue(enterPressed.widget.isDisposed(), "Quick access dialog did not close on Enter");
	}

	@Test
	public void testPreviousChoicesAvailableForExtension() {
		// add one selection to history
		TestQuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		Text text = dialog.getQuickAccessContents().getFilterText();
		dialog.open();
		text.setText(TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL);
		final Table firstTable = dialog.getQuickAccessContents().getTable();
		waitForQuickAccessResults(dialog);
		assertTrue(dialogContains(dialog, TestQuickAccessComputer.TEST_QUICK_ACCESS_PROPOSAL_LABEL),
				"Unexpected dialog contents: " + getAllEntries(firstTable));
		firstTable.select(0);
		activateCurrentElement(dialog);
		// then try in a new SearchField
		QuickAccessDialog secondDialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		secondDialog.open();
		waitForQuickAccessResults(secondDialog);
		assertTrue(getAllEntries(secondDialog.getQuickAccessContents().getTable()).stream()
						.anyMatch(TestQuickAccessComputer::isContributedItem),
				"Contributed item not found in previous choices");
	}

	@Test
	public void testPreviousChoicesAvailableForIncrementalExtension() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		text.setText(TestIncrementalQuickAccessComputer.ENABLEMENT_QUERY);
		final Table firstTable = dialog.getQuickAccessContents().getTable();
		waitForQuickAccessResults(dialog);
		assertTrue(firstTable.getItemCount() > 0
				&& TestIncrementalQuickAccessComputer.isContributedItem(getAllEntries(firstTable).get(0)),
				"Contributed item not first in results");
		firstTable.select(0);
		activateCurrentElement(dialog);
		// then try in a new SearchField
		dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		final Table secondTable = dialog.getQuickAccessContents().getTable();
		waitForQuickAccessResults(dialog);
		assertTrue(getAllEntries(secondTable).stream()
						.anyMatch(TestIncrementalQuickAccessComputer::isContributedItem),
				"Contributed item not found in previous choices");
	}

	@Test
	public void testResizeDuringComputeIsApplied() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		QuickAccessContents contents = dialog.getQuickAccessContents();
		Table table = contents.getTable();
		contents.getFilterText().setText("e");
		waitForQuickAccessResults(dialog);
		int fullCount = table.getItemCount();
		assertTrue(fullCount > 10, "Not enough quick access items to fill the table");

		// Shrink the shell while the recompute is still in flight
		contents.updateProposals("e");
		Shell shell = dialog.getShell();
		Point size = shell.getSize();
		shell.setSize(size.x, size.y - 5 * table.getItemHeight());
		waitForQuickAccessResults(dialog);
		assertTrue(table.getItemCount() < fullCount - 1,
				"Results were not recomputed for the shrunken table: " + table.getItemCount() + " of " + fullCount);
	}

	@Test
	public void testPrefixMatchHavePriority() {
		QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
		dialog.open();
		Text text = dialog.getQuickAccessContents().getFilterText();
		Table table = dialog.getQuickAccessContents().getTable();
		text.setText("P");
		waitForQuickAccessResults(dialog);
		assertTrue(table.getItemCount() > 3, "Not enough quick access items for simple filter");
		assertTrue(table.getItem(0).getText(1).toLowerCase().startsWith("p"), "Non-prefix match first");
	}

	@Test
	public void testCommandEnableContext() throws Exception {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("org.eclipse.ui.window.splitEditor");
		assertTrue(command.isDefined());

		File tmpFile = File.createTempFile("blah", ".txt");
		tmpFile.deleteOnExit();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IDE.openEditor(page, tmpFile.toURI(), "org.eclipse.ui.DefaultTextEditor", true);
		try {
			QuickAccessDialog dialog = new TestQuickAccessDialog(activeWorkbenchWindow, null);
			dialog.open();
			Text text = dialog.getQuickAccessContents().getFilterText();
			Table table = dialog.getQuickAccessContents().getTable();
			text.setText("Toggle Split");
			waitForQuickAccessResults(dialog);
			assertTrue(table.getItemCount() > 1, "Not enough quick access items for simple filter");
			assertTrue(table.getItem(0).getText(1).toLowerCase().startsWith("toggle split"), "Non-prefix match first");
		} finally {
			// Left open, this editor shows up as an extra proposal in every later test
			page.closeAllEditors(false);
		}
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

	/**
	 * Waits until the table holds the final results of the most recent request, so no
	 * assertion races a queued render or a recompute a resize is about to start.
	 */
	private static void waitForQuickAccessResults(QuickAccessDialog dialog) {
		QuickAccessContents contents = dialog.getQuickAccessContents();
		assertTrue(DisplayHelper.waitForCondition(contents.getTable().getDisplay(), COMPUTE_TIMEOUT,
				contents::isSettled), "Quick Access computation did not settle");
	}

	private boolean dialogContains(QuickAccessDialog dialog, String substring) {
		return getAllEntries(dialog.getQuickAccessContents().getTable()).stream()
				.anyMatch(label -> label.toLowerCase().contains(substring.toLowerCase()));
	}
}
