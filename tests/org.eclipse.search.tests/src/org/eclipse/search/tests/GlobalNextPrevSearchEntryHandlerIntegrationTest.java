/*******************************************************************************
 * Copyright (c) 2025 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.tests.filesearch.JUnitSourceSetup;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search2.internal.ui.basic.views.GlobalNextPrevSearchEntryHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Integration tests for {@link GlobalNextPrevSearchEntryHandler} that verify
 * actual navigation behaviour against a real search result in the workbench.
 *
 * <p>The navigation logic in {@link AbstractTextSearchViewPage} tracks match
 * position via an internal {@code fCurrentMatchIndex} field that is reset to
 * {@code -1} only by the JFace selection-changed listener, not by raw SWT
 * {@code table.setSelection()}. The tests here use the same approach as
 * {@code SearchResultPageTest.testTableNavigation()}: start with the viewer
 * selection at row 0 (leaving the internal match index at its initial value of
 * 0), then navigate backwards to reliably arrive at the last element, and then
 * navigate forwards to reliably arrive back at the first element.
 * </p>
 */
public class GlobalNextPrevSearchEntryHandlerIntegrationTest {

	@RegisterExtension
	static JUnitSourceSetup fgJUnitSource = new JUnitSourceSetup();

	private FileSearchPage fPage;
	private Table fTable;
	private GlobalNextPrevSearchEntryHandler fNextHandler;
	private GlobalNextPrevSearchEntryHandler fPrevHandler;

	@BeforeEach
	public void setUp() throws Exception {
		SearchTestUtil.ensureWelcomePageClosed();

		String[] fileNamePatterns = { "*.java" }; //$NON-NLS-1$
		FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);
		FileSearchQuery query = new FileSearchQuery("Test", false, true, scope); //$NON-NLS-1$
		NewSearchUI.runQueryInForeground(null, query);

		ISearchResultViewPart viewPart = NewSearchUI.getSearchResultView();
		assertNotNull(viewPart, "Search result view must be open after running a query");

		fPage = (FileSearchPage) viewPart.getActivePage();
		fPage.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
		fTable = ((TableViewer) fPage.getViewer()).getTable();
		consumeEvents();

		assertTrue(fTable.getItemCount() > 1,
				"JUnit source project must produce at least 2 results for navigation tests");

		fNextHandler = new GlobalNextPrevSearchEntryHandler();
		// default is already "next" but be explicit
		fNextHandler.setInitializationData(null, "command", "next"); //$NON-NLS-1$ //$NON-NLS-2$

		fPrevHandler = new GlobalNextPrevSearchEntryHandler();
		fPrevHandler.setInitializationData(null, "command", "previous"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@AfterEach
	public void tearDown() {
		// Drain all pending UpdateUIJobs for this page so they don't fire during
		// subsequent tests' consumeEvents() calls and corrupt their table state.
		if (fPage != null) {
			consumeEvents();
		}
		// Close any editors opened by showCurrentMatch() to leave the workbench clean.
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null && window.getActivePage() != null) {
			window.getActivePage().closeAllEditors(false);
		}
	}

	/**
	 * Going backward from the initial selection (row 0, internal match index 0)
	 * decrements the match index to -1, which causes
	 * {@link AbstractTextSearchViewPage#gotoPreviousMatch()} to wrap around to the
	 * last result. Then going forward exhausts the last result's matches and wraps
	 * back to the first result.
	 *
	 * <p>This mirrors the logic verified by
	 * {@code SearchResultPageTest.testTableNavigation()}, but exercises the
	 * handlers rather than direct page calls.
	 * </p>
	 */
	@Test
	public void testPreviousWrapsToLastThenNextWrapsToFirst() throws CoreException, Exception {
		// Start at the first element (initial state after query).
		fTable.setSelection(0);
		fTable.showSelection();
		consumeEvents();

		// Previous from initial match index (0) decrements to -1 → wraps to last.
		fPrevHandler.execute(new ExecutionEvent());
		consumeEvents();

		assertEquals(fTable.getItemCount() - 1, fTable.getSelectionIndex(),
				"previous handler should wrap from the first result to the last");

		// Next from the last result's final match increments beyond the end → wraps
		// to the first result.
		fNextHandler.execute(new ExecutionEvent());
		consumeEvents();

		assertEquals(0, fTable.getSelectionIndex(),
				"next handler should wrap from the last result to the first");
	}

	/**
	 * Two independent handler instances must not share internal state. Configuring
	 * one as "previous" must not affect one configured as "next".
	 */
	@Test
	public void testNextAndPreviousHandlersAreIndependent() throws CoreException, Exception {
		// Sanity-check: the two handlers are distinct objects.
		assertTrue(fNextHandler != fPrevHandler,
				"next and previous handlers must be separate instances");

		// Drive to last element via previous handler.
		fTable.setSelection(0);
		fTable.showSelection();
		consumeEvents();
		fPrevHandler.execute(new ExecutionEvent());
		consumeEvents();
		int lastIndex = fTable.getItemCount() - 1;
		assertEquals(lastIndex, fTable.getSelectionIndex(),
				"previous handler should reach last result");

		// Drive back to first element via next handler.
		fNextHandler.execute(new ExecutionEvent());
		consumeEvents();
		assertEquals(0, fTable.getSelectionIndex(),
				"next handler should reach first result");
	}

	/**
	 * Drains the SWT event queue and waits for all pending {@code UpdateUIJob}s
	 * belonging to the current page to complete. This is necessary because
	 * {@code UpdateUIJob} can reschedule itself with a 500 ms delay; a plain
	 * {@code Display.readAndDispatch()} loop would not wait for those deferred
	 * runs and could leave stale async work that pollutes subsequent tests.
	 */
	private void consumeEvents() {
		IJobManager manager = Job.getJobManager();
		// Drain immediately-queued display events first.
		while (Display.getDefault().readAndDispatch()) {
			// keep dispatching
		}
		// Then wait for all UpdateUIJobs that belong to this page to finish
		// (they identify themselves via belongsTo(AbstractTextSearchViewPage.this)).
		while (fPage != null && manager.find(fPage).length > 0) {
			Display.getDefault().readAndDispatch();
		}
		// Final drain for any events triggered by the completed jobs.
		while (Display.getDefault().readAndDispatch()) {
			// keep dispatching
		}
	}
}
