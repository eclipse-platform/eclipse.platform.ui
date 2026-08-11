/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.tests.SearchTestUtil;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

/**
 * Tests the label shown for a file search result, see
 * https://github.com/eclipse-platform/eclipse.platform.ui/issues/3720
 */
public class SearchResultPageLabelTest {

	private static final String PROJECT_NAME= "search-result-label-test";

	private static final String SEARCH_STRING= "xy";

	/**
	 * One line element of the tree, contributing {@link #MATCHES_PER_LINE} matches
	 * at the offsets 0 and {@link #SECOND_MATCH_OFFSET_IN_LINE}.
	 */
	private static final String MATCHING_LINE= "xy and xy\n";

	private static final int SECOND_MATCH_OFFSET_IN_LINE= MATCHING_LINE.indexOf(SEARCH_STRING, 1);

	private static final int MATCHES_PER_LINE= 2;

	private static final int LINE_COUNT= 4;

	private static final int MATCH_COUNT= LINE_COUNT * MATCHES_PER_LINE;

	private static final String FILE_CONTENT= MATCHING_LINE.repeat(LINE_COUNT);

	/**
	 * Filters the second match of every line, so that every line keeps exactly one
	 * visible match and no line is removed from the tree.
	 */
	private static final class SecondMatchInLineFilter extends MatchFilter {

		@Override
		public boolean filters(Match match) {
			return match.getOffset() % MATCHING_LINE.length() == SECOND_MATCH_OFFSET_IN_LINE;
		}

		@Override
		public String getName() {
			return "Second match in line";
		}

		@Override
		public String getDescription() {
			return "Filters the second match of every line";
		}

		@Override
		public String getActionLabel() {
			return getName();
		}

		@Override
		public String getID() {
			return "org.eclipse.search.tests.secondMatchInLineFilter";
		}
	}

	/** Number of matches the {@link SecondMatchInLineFilter} rejects. */
	private static final int FILTERED_MATCH_COUNT= LINE_COUNT;

	/** Number of matches per line that survive the {@link SecondMatchInLineFilter}. */
	private static final int UNFILTERED_MATCHES_PER_LINE= MATCHES_PER_LINE - 1;

	private IProject fProject;

	private FileSearchPage fPage;

	private MatchFilter[] fLastUsedFilters;

	private Integer fPreviousElementLimit;

	private int fPreviousLayout;

	@BeforeEach
	public void setUp() throws Exception {
		SearchTestUtil.ensureWelcomePageClosed();
		fLastUsedFilters= FileSearchResult.getLastUsedFilters();
		// new search results pick up the last used filters, start without any
		FileSearchResult.setLastUsedFilters(new MatchFilter[0]);
		fProject= ResourceHelper.createProject(PROJECT_NAME);
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("src"));
		ResourceHelper.createFile(folder, "test.txt", FILE_CONTENT);
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (fPage != null) {
			// the element limit and the layout are shared by all file search pages
			if (fPreviousElementLimit != null) {
				fPage.setElementLimit(fPreviousElementLimit);
			}
			if (fPreviousLayout != 0) {
				fPage.setLayout(fPreviousLayout);
			}
			fPage= null;
		}
		// setActiveMatchFilters(..) persists the filters in the dialog settings
		FileSearchResult.setLastUsedFilters(fLastUsedFilters);
		ResourceHelper.deleteProject(PROJECT_NAME);
	}

	/**
	 * If all matches are shown the label must not claim that only some of them are
	 * shown, even if some lines contain more than one match.
	 */
	@Test
	public void testLabelWithSeveralMatchesPerLine() throws Exception {
		AbstractTextSearchResult result= runTextQuery(-1);

		assertEquals(MATCH_COUNT, result.getMatchCount());
		assertEquals(result.getLabel(), fPage.getLabel(),
				"nothing is hidden, so the label must not report a limited result");
	}

	/**
	 * If the element limit hides elements the label must report the number of shown
	 * matches and not the number of shown elements.
	 */
	@Test
	public void testLabelWithTruncatedResult() throws Exception {
		int shownLines= LINE_COUNT - 1;
		AbstractTextSearchResult result= runTextQuery(shownLines);

		int shownMatches= shownLines * MATCHES_PER_LINE;
		assertEquals(limitedLabel(result, shownMatches), fPage.getLabel());
	}

	/**
	 * Matches hidden by a match filter are reported by the "filtered" qualifier, and
	 * the result is not reported as limited as long as the element limit doesn't
	 * hide anything.
	 */
	@Test
	public void testLabelWithMatchFilter() throws Exception {
		AbstractTextSearchResult result= runTextQuery(-1);
		activateMatchFilter(result);

		assertEquals(filteredLabel(result.getLabel(), FILTERED_MATCH_COUNT), fPage.getLabel(),
				"nothing is hidden by the element limit, so all filtered matches must be reported as filtered");
	}

	/**
	 * If a match filter and the element limit hide matches, both must be reported
	 * independently of each other: the "filtered" qualifier counts only the matches
	 * rejected by the filter, while the "limited" qualifier reports how many of all
	 * matches are shown.
	 */
	@Test
	public void testLabelWithMatchFilterAndTruncatedResult() throws Exception {
		int shownLines= LINE_COUNT - 1; // the element limit hides one line
		AbstractTextSearchResult result= runTextQuery(shownLines);
		activateMatchFilter(result);

		// only one match per line is visible, the other one is rejected by the filter
		int shownMatches= shownLines * UNFILTERED_MATCHES_PER_LINE;
		String expected= filteredLabel(limitedLabel(result, shownMatches), FILTERED_MATCH_COUNT);
		assertEquals(expected, fPage.getLabel(),
				"matches hidden by the element limit must not be counted as filtered from view");
	}

	/**
	 * A file name search shows files, so nothing is hidden if all files are shown.
	 */
	@Test
	public void testLabelOfFileNameSearch() throws Exception {
		AbstractTextSearchResult result= runQuery(new FileSearchQuery("", false, true, createScope()), -1);

		assertEquals(1, result.getElementsCount());
		assertEquals(result.getLabel(), fPage.getLabel());
	}

	private String limitedLabel(AbstractTextSearchResult result, int shownMatches) {
		return Messages.format(SearchMessages.FileSearchPage_limited_format_matches, new Object[] {
				result.getLabel(), Integer.valueOf(shownMatches), Integer.valueOf(result.getMatchCount()) });
	}

	private String filteredLabel(String label, int filteredOut) {
		return Messages.format(SearchMessages.FileSearchPage_filteredWithCount_message,
				new Object[] { label, String.valueOf(filteredOut) });
	}

	private void activateMatchFilter(AbstractTextSearchResult result) {
		result.setActiveMatchFilters(new MatchFilter[] { new SecondMatchInLineFilter() });
		consumeEvents();
	}

	private AbstractTextSearchResult runTextQuery(int elementLimit) throws Exception {
		return runQuery(new FileSearchQuery(SEARCH_STRING, false, true, createScope()), elementLimit);
	}

	private FileTextSearchScope createScope() {
		return FileTextSearchScope.newSearchScope(new IResource[] { fProject }, new String[] { "*.txt" }, false);
	}

	private AbstractTextSearchResult runQuery(FileSearchQuery query, int elementLimit) throws Exception {
		NewSearchUI.runQueryInForeground(null, query);
		ISearchResultViewPart view= NewSearchUI.getSearchResultView();
		fPage= (FileSearchPage) view.getActivePage();
		fPreviousElementLimit= fPage.getElementLimit();
		fPreviousLayout= fPage.getLayout();
		fPage.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		fPage.setElementLimit(Integer.valueOf(elementLimit));
		consumeEvents();
		return (AbstractTextSearchResult) query.getSearchResult();
	}

	private void consumeEvents() {
		IJobManager manager= Job.getJobManager();
		while (manager.find(fPage).length > 0) {
			runEventLoop();
		}
		runEventLoop();
	}

	private static void runEventLoop() {
		Display display= Display.getCurrent();
		while (display != null && display.readAndDispatch()) {
			// process all pending events
		}
	}
}
