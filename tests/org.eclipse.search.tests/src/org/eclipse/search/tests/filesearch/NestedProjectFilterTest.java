/*******************************************************************************
 * Copyright (c) 2026 Andrey Loskutov <loskutov@gmx.de> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.viewers.AbstractTreeViewer;

import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.internal.ui.text.OuterProjectFileFilter;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.tests.SearchTestUtil;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

/**
 * Tests the match filter that hides the matches of files which are reported for
 * an outer project although they belong to a nested project, see
 * https://github.com/eclipse-platform/eclipse.platform.text/issues/143
 * <p>
 * The tests use two projects that share the same file on disk: the location of
 * the inner project is a folder of the outer project, so the very same file is
 * represented by two resources and is reported twice by a search.
 * </p>
 */
public class NestedProjectFilterTest {

	private static final String OUTER_PROJECT_NAME= "nested-project-filter-outer";

	private static final String INNER_PROJECT_NAME= "nested-project-filter-inner";

	private static final String FILE_NAME= "test.txt";

	private static final String SEARCH_STRING= "nestedProjectFilterNeedle";

	private IProject outerProject;

	private IProject innerProject;

	/** The file as seen by the inner (innermost) project. */
	private IFile innerFile;

	/** The very same file on disk, as seen by the enclosing outer project. */
	private IFile outerFile;

	private FileSearchPage page;

	private int previousLayout;

	private MatchFilter[] lastUsedFilters;

	@BeforeEach
	public void setUp() throws Exception {
		SearchTestUtil.ensureWelcomePageClosed();
		// new search results pick up the last used filters, start without any
		lastUsedFilters= FileSearchResult.getLastUsedFilters();
		FileSearchResult.setLastUsedFilters(new MatchFilter[0]);

		outerProject= ResourceHelper.createProject(OUTER_PROJECT_NAME);
		IFolder nestedFolder= ResourceHelper.createFolder(outerProject.getFolder(INNER_PROJECT_NAME));
		outerFile= ResourceHelper.createFile(nestedFolder, FILE_NAME, SEARCH_STRING);
		innerProject= createProjectAt(INNER_PROJECT_NAME, nestedFolder);
		innerFile= innerProject.getFile(FILE_NAME);

		assertTrue(innerFile.exists(), "the nested project must see the file of the outer project");
		assertEquals(outerFile.getLocationURI(), innerFile.getLocationURI(),
				"both resources must represent the same file on disk");
		assertTrue(innerFile.getFullPath().segmentCount() < outerFile.getFullPath().segmentCount(),
				"the file of the innermost project must have the shortest path");
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (page != null) {
			// the layout is shared by all file search pages
			page.setLayout(previousLayout);
			page= null;
		}
		// setActiveMatchFilters(..) persists the filters in the dialog settings
		FileSearchResult.setLastUsedFilters(lastUsedFilters);
		// the inner project is located inside the outer one, delete it first
		ResourceHelper.deleteProject(INNER_PROJECT_NAME);
		ResourceHelper.deleteProject(OUTER_PROJECT_NAME);
	}

	/**
	 * Only the file of the outer project is a duplicate, the file of the innermost
	 * project is the one to show.
	 */
	@Test
	public void testDuplicateOfOuterProjectIsFiltered() {
		OuterProjectFileFilter filter= new OuterProjectFileFilter();

		assertTrue(filter.filters(new FileMatch(outerFile)),
				"the file reported for the outer project must be filtered");
		assertFalse(filter.filters(new FileMatch(innerFile)),
				"the file of the innermost project must not be filtered");
	}

	/**
	 * The filter remembers its answer per file, repeated evaluations must not
	 * change the result.
	 */
	@Test
	public void testRepeatedEvaluationIsStable() {
		OuterProjectFileFilter filter= new OuterProjectFileFilter();

		for (int i= 0; i < 3; i++) {
			assertTrue(filter.filters(new FileMatch(outerFile)), "evaluation " + i);
			assertFalse(filter.filters(new FileMatch(innerFile)), "evaluation " + i);
		}
	}

	/**
	 * The matches of a closed project cannot be shown, so the file of the outer
	 * project is not a duplicate anymore once the inner project is closed. The
	 * filter must not answer with an outdated (remembered) state.
	 */
	@Test
	public void testFilterIsUpdatedWhenInnerProjectIsClosed() throws Exception {
		OuterProjectFileFilter filter= new OuterProjectFileFilter();
		assertTrue(filter.filters(new FileMatch(outerFile)), "precondition: the file is a duplicate");

		innerProject.close(null);

		assertFiltersEventually(filter, outerFile, false,
				"the file of the outer project is the only one that can be shown now");
	}

	/**
	 * The remembered filter states are shared by all instances of the filter, they
	 * must be invalidated for all of them if the inner project is deleted.
	 */
	@Test
	public void testFilterIsUpdatedWhenInnerProjectIsDeleted() throws Exception {
		OuterProjectFileFilter filter= new OuterProjectFileFilter();
		assertTrue(filter.filters(new FileMatch(outerFile)), "precondition: the file is a duplicate");

		// deletes the project but keeps its content, which is owned by the outer
		// project as well
		innerProject.delete(false, true, null);

		assertFiltersEventually(new OuterProjectFileFilter(), outerFile, false,
				"the file of the outer project is the only one left");
		assertFiltersEventually(filter, outerFile, false, "the remembered state must be invalidated");
	}

	/**
	 * A file that exists only once must never be filtered.
	 */
	@Test
	public void testUniqueFileIsNotFiltered() throws Exception {
		IFolder folder= ResourceHelper.createFolder(outerProject.getFolder("unique"));
		IFile uniqueFile= ResourceHelper.createFile(folder, FILE_NAME, SEARCH_STRING);
		OuterProjectFileFilter filter= new OuterProjectFileFilter();

		assertFalse(filter.filters(new FileMatch(uniqueFile)));
	}

	/**
	 * A linked resource represents the file of another project without any change
	 * of the project itself: the link is stored in the project description, the
	 * flags of the project delta don't report it.
	 */
	@Test
	public void testFilterIsUpdatedWhenLinkIsCreated() throws Exception {
		IFolder folder= ResourceHelper.createFolder(outerProject.getFolder("linkTarget"));
		IFile linkTarget= ResourceHelper.createFile(folder, "linked.txt", SEARCH_STRING);
		OuterProjectFileFilter filter= new OuterProjectFileFilter();
		assertFalse(filter.filters(new FileMatch(linkTarget)), "precondition: the file exists only once");

		IFile link= innerProject.getFile("linked.txt");
		link.createLink(linkTarget.getLocationURI(), IResource.NONE, null);

		assertTrue(link.getFullPath().segmentCount() < linkTarget.getFullPath().segmentCount(),
				"the link must be the innermost representation of the file");
		assertFiltersEventually(filter, linkTarget, true,
				"the file is represented by the link of the inner project as well now");
	}

	/**
	 * Without the filter the same file is reported twice, with the filter only the
	 * matches of the innermost project are shown.
	 */
	@Test
	public void testFilterStateOfSearchResult() throws Exception {
		FileSearchQuery query= createQuery();
		NewSearchUI.runQueryInForeground(null, query);
		FileSearchResult result= (FileSearchResult) query.getSearchResult();

		assertEquals(2, result.getMatchCount(), "the same file must be found in both projects");
		assertEquals(1, result.getMatchCount(innerFile));
		assertEquals(1, result.getMatchCount(outerFile));

		result.setActiveMatchFilters(new MatchFilter[] { getInnermostProjectFilter(result) });

		assertTrue(isFiltered(result, outerFile), "the duplicate of the outer project must be filtered");
		assertFalse(isFiltered(result, innerFile), "the file of the innermost project must be shown");
		assertEquals(2, result.getMatchCount(), "filtered matches are still part of the result");
	}

	/**
	 * The filtered matches must not be shown in the tree of the search view.
	 */
	@Test
	public void testFilteredFileIsNotShownInTree() throws Exception {
		FileSearchQuery query= createQuery();
		NewSearchUI.runQueryInForeground(null, query);
		FileSearchResult result= (FileSearchResult) query.getSearchResult();

		ISearchResultViewPart view= NewSearchUI.getSearchResultView();
		page= (FileSearchPage) view.getActivePage();
		previousLayout= page.getLayout();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		result.setActiveMatchFilters(new MatchFilter[] { getInnermostProjectFilter(result) });
		consumeEvents();

		AbstractTreeViewer viewer= (AbstractTreeViewer) page.getViewer();
		viewer.expandAll();

		assertNotNull(viewer.testFindItem(innerFile), "the file of the innermost project must be shown");
		assertNull(viewer.testFindItem(outerFile), "the duplicate of the outer project must not be shown");
	}

	/**
	 * The remembered filter states are shared by all search results and are keyed
	 * by the file handles, which are equal for all searches: a state must never be
	 * reused by a search that is started later.
	 */
	@Test
	public void testStatesAreNotReusedByNextSearch() throws Exception {
		FileSearchQuery query= createQuery();
		NewSearchUI.runQueryInForeground(null, query);
		FileSearchResult result= (FileSearchResult) query.getSearchResult();
		result.setActiveMatchFilters(new MatchFilter[] { getInnermostProjectFilter(result) });
		assertTrue(isFiltered(result, outerFile), "precondition: the duplicate is filtered");

		innerProject.close(null);
		NewSearchUI.runQueryInForeground(null, query);

		assertEquals(1, result.getMatchCount(), "only the file of the outer project can be found now");
		assertFalse(isFiltered(result, outerFile), "the file of the outer project must be shown now");
	}

	/**
	 * The filter state of a match is evaluated by the search result, the tree of the
	 * search view must not evaluate the (potentially expensive) match filters again
	 * in the UI thread, see
	 * https://github.com/eclipse-platform/eclipse.platform.ui/issues/4337
	 */
	@Test
	public void testTreeUpdateDoesNotReevaluateMatchFilters() throws Exception {
		FileSearchQuery query= createQuery();
		NewSearchUI.runQueryInForeground(null, query);
		FileSearchResult result= (FileSearchResult) query.getSearchResult();

		ISearchResultViewPart view= NewSearchUI.getSearchResultView();
		page= (FileSearchPage) view.getActivePage();
		previousLayout= page.getLayout();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		CountingMatchFilter filter= new CountingMatchFilter();
		result.setActiveMatchFilters(new MatchFilter[] { filter });
		consumeEvents();

		FileMatch existingMatch= (FileMatch) result.getMatches(innerFile)[0];
		filter.evaluations.set(0);

		// updates the line element of the match in the tree
		result.addMatch(new FileMatch(innerFile, existingMatch.getOffset(), existingMatch.getLength(),
				existingMatch.getLineElement()));
		consumeEvents();

		assertEquals(1, filter.evaluations.get(),
				"the filter must be evaluated once for the added match and not again for the tree update");
	}

	private static void assertFiltersEventually(MatchFilter filter, IFile file, boolean expected, String message) {
		// the resource change notification is sent when the operation that changed the
		// workspace is finished, which may happen asynchronously
		long timeout= System.currentTimeMillis() + 10_000;
		boolean actual= filter.filters(new FileMatch(file));
		while (actual != expected && System.currentTimeMillis() < timeout) {
			runEventLoop();
			Thread.yield();
			actual= filter.filters(new FileMatch(file));
		}
		assertEquals(expected, actual, message);
	}

	private static boolean isFiltered(FileSearchResult result, IFile file) {
		Match[] matches= result.getMatches(file);
		assertEquals(1, matches.length, "unexpected number of matches for " + file.getFullPath());
		return matches[0].isFiltered();
	}

	private static MatchFilter getInnermostProjectFilter(FileSearchResult result) {
		for (MatchFilter filter : result.getAllMatchFilters()) {
			if (filter instanceof OuterProjectFileFilter) {
				return filter;
			}
		}
		throw new AssertionError("the file search result must provide the innermost project filter");
	}

	private FileSearchQuery createQuery() {
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(
				new IResource[] { innerProject, outerProject }, new String[] { "*.txt" }, false);
		return new FileSearchQuery(SEARCH_STRING, false, true, scope);
	}

	/**
	 * Creates a project at the location of the given folder, so that the content of
	 * the folder belongs to two projects.
	 */
	private static IProject createProjectAt(String projectName, IFolder folder) throws Exception {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IProject project= workspace.getRoot().getProject(projectName);
		IProjectDescription description= workspace.newProjectDescription(projectName);
		description.setLocation(folder.getLocation());
		project.create(description, null);
		project.open(null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		folder.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		return project;
	}

	private void consumeEvents() {
		IJobManager manager= Job.getJobManager();
		while (manager.find(page).length > 0) {
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

	/**
	 * Counts how often the filter state of a match is evaluated.
	 */
	private static final class CountingMatchFilter extends MatchFilter {

		final AtomicInteger evaluations= new AtomicInteger();

		@Override
		public boolean filters(Match match) {
			evaluations.incrementAndGet();
			return false;
		}

		@Override
		public String getName() {
			return "counting";
		}

		@Override
		public String getDescription() {
			return "counts the evaluations of the filter";
		}

		@Override
		public String getActionLabel() {
			return getName();
		}

		@Override
		public String getID() {
			return "org.eclipse.search.tests.countingFilter";
		}
	}
}
