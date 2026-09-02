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

		assertFalse(filter.filters(new FileMatch(outerFile)),
				"the file of the outer project is the only one that can be shown now");
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
}
