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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.internal.ui.text.LineElement;
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

	/** Filters every match of the first line, removing that line from the tree. */
	private static final class FirstLineFilter extends MatchFilter {

		@Override
		public boolean filters(Match match) {
			return ((FileMatch) match).getLineElement().getLine() == 1;
		}

		@Override
		public String getName() {
			return "First line";
		}

		@Override
		public String getDescription() {
			return "Filters all matches of the first line";
		}

		@Override
		public String getActionLabel() {
			return getName();
		}

		@Override
		public String getID() {
			return "org.eclipse.search.tests.firstLineFilter";
		}
	}

	/** Number of matches the {@link SecondMatchInLineFilter} rejects. */
	private static final int FILTERED_MATCH_COUNT= LINE_COUNT;

	/** Number of matches per line that survive the {@link SecondMatchInLineFilter}. */
	private static final int MATCHES_PER_LINE_NOT_FILTERED_OUT= MATCHES_PER_LINE - 1;

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
	 * rejected by the filter, while the "limited" qualifier reports how many of the
	 * matches that pass the filter are shown.
	 */
	@Test
	public void testLabelWithMatchFilterAndTruncatedResult() throws Exception {
		int shownLines= LINE_COUNT - 1; // the element limit hides one line
		AbstractTextSearchResult result= runTextQuery(shownLines);
		activateMatchFilter(result);

		// only one match per line is visible, the other one is rejected by the filter
		int shownMatches= shownLines * MATCHES_PER_LINE_NOT_FILTERED_OUT;
		// filtered matches are not hidden by the element limit
		int matchesNotFilteredOut= MATCH_COUNT - FILTERED_MATCH_COUNT;
		String expected= filteredLabel(limitedLabel(result, shownMatches, matchesNotFilteredOut), FILTERED_MATCH_COUNT);
		assertEquals(expected, fPage.getLabel(),
				"matches hidden by the element limit must not be counted as filtered from view, "
						+ "and filtered matches must not be counted as hidden by the element limit");
	}

	/**
	 * Reinserting a fully filtered line must evict the sibling that no longer fits
	 * within the element limit, not just add another item to the viewer.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testLimitedChildSelectionAfterFilterToggle() throws Exception {
		IFile file= ResourceHelper.createFile(fProject.getFolder("src"), "toggle.txt",
				"xy xy\nxy\nxy xy xy\n");
		AbstractTextSearchResult result= runQuery(createFileQuery(file), 2);
		TreeViewer viewer= (TreeViewer) fPage.getViewer();
		viewer.expandAll();
		assertEquals(List.of(1, 2), shownLineNumbers(viewer.getTree().getItems()));
		assertEquals(limitedLabel(result, 3), fPage.getLabel());

		result.setActiveMatchFilters(new MatchFilter[] { new FirstLineFilter() });
		consumeEvents();
		assertEquals(List.of(2, 3), shownLineNumbers(viewer.getTree().getItems()),
				"fully filtering the first line must reveal the next line hidden by the limit");
		assertEquals(filteredLabel(result.getLabel(), 2), fPage.getLabel());

		// Only the first line changes filter state, exercising single-element insertion.
		result.setActiveMatchFilters(new MatchFilter[0]);
		consumeEvents();
		assertEquals(List.of(1, 2), shownLineNumbers(viewer.getTree().getItems()),
				"restoring the first line must displace the third line, not exceed the limit");
		assertEquals(limitedLabel(result, 3), fPage.getLabel());
	}

	/**
	 * A filter hiding all matches of a file must remove all lines of that file from
	 * the tree, including the lines hidden by the element limit, and the file
	 * itself.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testFilteringAllMatchesOfFileRemovesAllItsLines() throws Exception {
		IFolder folder= fProject.getFolder("src");
		IFile testFile= folder.getFile("test.txt");
		IFile otherFile= ResourceHelper.createFile(folder, "other.txt", "xy\n");
		int elementLimit= LINE_COUNT - 2; // hides lines of test.txt
		AbstractTextSearchResult result= runTextQuery(elementLimit);
		assertEquals(MATCH_COUNT + 1, result.getMatchCount());
		assertEquals(Set.of(testFile, otherFile), new HashSet<>(shownFiles()));

		result.setActiveMatchFilters(new MatchFilter[] { new FileNameFilter(testFile.getName()) });
		consumeEvents();
		ITreeContentProvider provider= (ITreeContentProvider) fPage.getViewer().getContentProvider();
		assertEquals(0, provider.getChildren(testFile).length,
				"all lines of the filtered file must be removed, also the ones hidden by the element limit");
		assertEquals(List.of(otherFile), shownFiles(), "the filtered file must be removed from the tree");
		assertEquals(List.of(1), shownLineNumbers(((TreeViewer) fPage.getViewer()).getTree().getItems()));
		assertEquals(filteredLabel(result.getLabel(), MATCH_COUNT), fPage.getLabel(),
				"nothing is hidden by the element limit anymore");
	}

	private List<Integer> shownLineNumbers(TreeItem[] items) {
		List<Integer> lines= new ArrayList<>();
		for (TreeItem item : items) {
			if (item.getData() instanceof LineElement line) {
				lines.add(Integer.valueOf(line.getLine()));
			}
			lines.addAll(shownLineNumbers(item.getItems()));
		}
		return lines;
	}

	/**
	 * The matches the tree shows for its lines must agree with the matches the
	 * label of the page reports: a match hidden by a match filter must not be
	 * counted as shown and as filtered at the same time.
	 */
	@Test
	public void testDisplayedMatchCountOfLinesRespectsMatchFilter() throws Exception {
		AbstractTextSearchResult result= runTextQuery(-1);
		activateMatchFilter(result);

		ITreeContentProvider provider= (ITreeContentProvider) fPage.getViewer().getContentProvider();
		assertEquals(MATCH_COUNT - FILTERED_MATCH_COUNT, sumDisplayedMatchCount(provider, result),
				"the tree must not show matches that are reported as filtered from view");
	}

	private int sumDisplayedMatchCount(ITreeContentProvider provider, Object element) {
		Object[] children= provider.getChildren(element);
		if (children.length == 0) {
			return fPage.getDisplayedMatchCount(element);
		}
		int count= 0;
		for (Object child : children) {
			count+= sumDisplayedMatchCount(provider, child);
		}
		return count;
	}

	/**
	 * The elements hidden by the element limit must be chosen in a deterministic
	 * order, so that repeating the very same search reports the same numbers.
	 */
	@Test
	public void testTruncatedResultIsReproducible() throws Exception {
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("varying"));
		// lines with a different number of matches: an arbitrary selection of the
		// shown lines would report a different number of matches
		IFile file= ResourceHelper.createFile(folder, "varying.txt", "xy\nxy xy\nxy xy xy\n");
		int elementLimit= 2; // hides the last line of the file
		int shownMatches= 1 + 2; // the matches of the first two lines

		AbstractTextSearchResult result= runQuery(createFileQuery(file), elementLimit);
		assertEquals(6, result.getMatchCount());
		assertEquals(limitedLabel(result, shownMatches), fPage.getLabel(),
				"the shown lines must be the first ones of the file");

		AbstractTextSearchResult repeated= runQuery(createFileQuery(file), elementLimit);
		assertEquals(limitedLabel(repeated, shownMatches), fPage.getLabel(),
				"repeating the same search must report the same number of shown matches");
	}

	private FileSearchQuery createFileQuery(IFile file) {
		return new FileSearchQuery(SEARCH_STRING, false, true,
				FileTextSearchScope.newSearchScope(new IResource[] { file }, new String[] { "*.txt" }, false));
	}

	/**
	 * The children shown within the element limit must be the first ones in the
	 * order of the viewer, which ignores the case of the names: a case sensitive
	 * order would hide "b.txt" although the viewer sorts it before "C.txt".
	 */
	@Test
	public void testShownChildrenIgnoreCase() throws Exception {
		IFolder folder= fProject.getFolder("src");
		IFile upperA= ResourceHelper.createFile(folder, "A.txt", "a");
		IFile lowerB= ResourceHelper.createFile(folder, "b.txt", "b");
		ResourceHelper.createFile(folder, "C.txt", "c");
		runQuery(new FileSearchQuery("", false, true, createScope()), 2);

		assertEquals(List.of(upperA, lowerB), shownFiles(),
				"the first two files in case insensitive order must be shown");
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

	/**
	 * Removing the matches of the shown file must remove it from the tree, even if
	 * another file still has matches, and the hidden file must take its place.
	 */
	@Test
	public void testRemovingShownFileOfLimitedResultInTree() throws Exception {
		assertRemovingFileOfLimitedResult(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE, true);
	}

	/**
	 * Removing the matches of the file hidden by the element limit must remove it
	 * from the tree, so that the result is no longer reported as limited.
	 */
	@Test
	public void testRemovingHiddenFileOfLimitedResultInTree() throws Exception {
		assertRemovingFileOfLimitedResult(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE, false);
	}

	/**
	 * Removing the matches of the shown file must remove it from the table, and the
	 * hidden file must take its place.
	 */
	@Test
	public void testRemovingShownFileOfLimitedResultInTable() throws Exception {
		assertRemovingFileOfLimitedResult(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT, true);
	}

	private void assertRemovingFileOfLimitedResult(int layout, boolean removeShownFile) throws Exception {
		IFile otherFile= ResourceHelper.createFile(fProject.getFolder("src"), "other.txt", "o");
		IFile testFile= fProject.getFolder("src").getFile("test.txt");
		// only one of the two files fits into the element limit, no match filter is active
		AbstractTextSearchResult result= runQuery(new FileSearchQuery("", false, true, createScope()), 1, layout);
		assertEquals(2, result.getElementsCount());
		List<IFile> shownFiles= shownFiles();
		assertEquals(1, shownFiles.size(), "the element limit allows to show one file");
		assertEquals(limitedFilesLabel(result, 1), fPage.getLabel());

		IFile shownFile= shownFiles.get(0);
		IFile hiddenFile= shownFile.equals(testFile) ? otherFile : testFile;
		IFile removedFile= removeShownFile ? shownFile : hiddenFile;
		IFile remainingFile= removeShownFile ? hiddenFile : shownFile;
		result.removeMatches(result.getMatches(removedFile));
		consumeEvents();

		assertEquals(1, result.getElementsCount());
		assertEquals(List.of(remainingFile), shownFiles(), "only the file that still has matches must be shown");
		assertEquals(result.getLabel(), fPage.getLabel(),
				"the only file left fits into the element limit, nothing is hidden by the limit");
	}

	/**
	 * If the element limit hides files of a file name search, the label must report
	 * the number of shown files.
	 */
	@Test
	public void testLabelOfTruncatedFileNameSearchInTree() throws Exception {
		assertLabelOfTruncatedFileNameSearch(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	/**
	 * If the element limit hides files of a file name search, the label must report
	 * the number of shown files.
	 */
	@Test
	public void testLabelOfTruncatedFileNameSearchInTable() throws Exception {
		assertLabelOfTruncatedFileNameSearch(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
	}

	private void assertLabelOfTruncatedFileNameSearch(int layout) throws Exception {
		IFolder folder= fProject.getFolder("src");
		ResourceHelper.createFile(folder, "a.txt", "a");
		ResourceHelper.createFile(folder, "b.txt", "b");
		int elementLimit= 2; // hides one of the three files
		AbstractTextSearchResult result= runQuery(new FileSearchQuery("", false, true, createScope()), elementLimit,
				layout);

		assertEquals(3, result.getElementsCount());
		assertEquals(limitedFilesLabel(result, elementLimit), fPage.getLabel());
	}

	/**
	 * Files hidden by a match filter are not hidden by the element limit: as long as
	 * the element limit doesn't hide anything, the result must not be reported as
	 * limited.
	 */
	@Test
	public void testFilteredFilesAreNotReportedAsTruncatedInTree() throws Exception {
		assertFilteredFilesAreNotReportedAsTruncated(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	/**
	 * Files hidden by a match filter are not hidden by the element limit: as long as
	 * the element limit doesn't hide anything, the result must not be reported as
	 * limited.
	 */
	@Test
	public void testFilteredFilesAreNotReportedAsTruncatedInTable() throws Exception {
		assertFilteredFilesAreNotReportedAsTruncated(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
	}

	private void assertFilteredFilesAreNotReportedAsTruncated(int layout) throws Exception {
		IFile otherFile= ResourceHelper.createFile(fProject.getFolder("src"), "other.txt", "o");
		IFile testFile= fProject.getFolder("src").getFile("test.txt");
		// only one of the two files fits into the element limit
		AbstractTextSearchResult result= runQuery(new FileSearchQuery("", false, true, createScope()), 1, layout);
		assertEquals(2, result.getElementsCount());
		List<IFile> shownFiles= shownFiles();
		assertEquals(1, shownFiles.size(), "the element limit allows to show one file");

		// Filter the file that is shown, no matter which one the viewer picked: the
		// file hidden by the element limit passes the filter and fits into the limit.
		IFile filteredFile= shownFiles.get(0);
		IFile fileNotFilteredOut= filteredFile.equals(testFile) ? otherFile : testFile;
		result.setActiveMatchFilters(new MatchFilter[] { new FileNameFilter(filteredFile.getName()) });
		consumeEvents();
		assertEquals(List.of(fileNotFilteredOut), shownFiles(),
				"the file hidden by the element limit must take the place of the filtered file");
		assertEquals(filteredLabel(result.getLabel(), 1), fPage.getLabel(),
				"the only file shown fits into the element limit, nothing is hidden by the limit");
	}

	/**
	 * If a shown file gets filtered, a file hidden by the element limit must take its
	 * place in the table, and the table must not exceed the element limit when the
	 * filter is removed again.
	 */
	@Test
	public void testFilteringShownFileRevealsHiddenFileInTable() throws Exception {
		assertFilteringShownFileRevealsHiddenFile(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
	}

	/**
	 * If a shown file gets filtered, a file hidden by the element limit must take its
	 * place in the tree, and the tree must not exceed the element limit when the
	 * filter is removed again.
	 */
	@Test
	public void testFilteringShownFileRevealsHiddenFileInTree() throws Exception {
		assertFilteringShownFileRevealsHiddenFile(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	private void assertFilteringShownFileRevealsHiddenFile(int layout) throws Exception {
		IFolder folder= fProject.getFolder("src");
		IFile fileA= ResourceHelper.createFile(folder, "a.txt", "a");
		IFile fileB= ResourceHelper.createFile(folder, "b.txt", "b");
		IFile testFile= folder.getFile("test.txt");
		int elementLimit= 2; // hides one of the three files
		AbstractTextSearchResult result= runQuery(new FileSearchQuery("", false, true, createScope()), elementLimit,
				layout);
		assertEquals(3, result.getElementsCount());
		List<IFile> shownFiles= shownFiles();
		assertEquals(elementLimit, shownFiles.size());
		Set<IFile> hiddenFiles= new HashSet<>(List.of(fileA, fileB, testFile));
		hiddenFiles.removeAll(shownFiles);
		assertEquals(1, hiddenFiles.size());
		IFile hiddenFile= hiddenFiles.iterator().next();

		// filter one of the shown files, no matter which ones the viewer picked
		IFile filteredFile= shownFiles.get(0);
		result.setActiveMatchFilters(new MatchFilter[] { new FileNameFilter(filteredFile.getName()) });
		consumeEvents();
		Set<IFile> expected= new HashSet<>(shownFiles);
		expected.remove(filteredFile);
		expected.add(hiddenFile);
		assertEquals(expected, new HashSet<>(shownFiles()),
				"the file hidden by the element limit must take the place of the filtered file");
		assertEquals(filteredLabel(result.getLabel(), 1), fPage.getLabel(),
				"both files passing the filter fit into the element limit");

		result.setActiveMatchFilters(new MatchFilter[0]);
		consumeEvents();
		assertEquals(elementLimit, shownFiles().size(),
				"the file passing the filter again must not exceed the element limit");
		assertEquals(limitedFilesLabel(result, elementLimit), fPage.getLabel());
	}

	/**
	 * @return the files currently shown by the viewer of the page, in the order of
	 *         the viewer
	 */
	private List<IFile> shownFiles() {
		List<IFile> files= new ArrayList<>();
		StructuredViewer viewer= fPage.getViewer();
		if (viewer instanceof TreeViewer treeViewer) {
			treeViewer.expandAll();
			collectFiles(treeViewer.getTree().getItems(), files);
		} else if (viewer instanceof TableViewer tableViewer) {
			for (TableItem item : tableViewer.getTable().getItems()) {
				if (item.getData() instanceof IFile file) {
					files.add(file);
				}
			}
		}
		return files;
	}

	private static void collectFiles(TreeItem[] items, List<IFile> files) {
		for (TreeItem item : items) {
			if (item.getData() instanceof IFile file) {
				files.add(file);
			}
			collectFiles(item.getItems(), files);
		}
	}

	/**
	 * If a match filter and the element limit hide files, the "limited" qualifier
	 * must only report the files that pass the filter.
	 */
	@Test
	public void testLabelOfFilteredAndTruncatedFileNameSearchInTree() throws Exception {
		assertLabelOfFilteredAndTruncatedFileNameSearch(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	/**
	 * If a match filter and the element limit hide files, the "limited" qualifier
	 * must only report the files that pass the filter.
	 */
	@Test
	public void testLabelOfFilteredAndTruncatedFileNameSearchInTable() throws Exception {
		assertLabelOfFilteredAndTruncatedFileNameSearch(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
	}

	private void assertLabelOfFilteredAndTruncatedFileNameSearch(int layout) throws Exception {
		IFolder folder= fProject.getFolder("src");
		ResourceHelper.createFile(folder, "a.txt", "a");
		ResourceHelper.createFile(folder, "b.txt", "b");
		String filteredFileName= "filtered.txt";
		ResourceHelper.createFile(folder, filteredFileName, "f");
		int elementLimit= 2; // hides one of the three files that pass the filter
		AbstractTextSearchResult result= runQuery(new FileSearchQuery("", false, true, createScope()), elementLimit,
				layout);
		assertEquals(4, result.getElementsCount());

		result.setActiveMatchFilters(new MatchFilter[] { new FileNameFilter(filteredFileName) });
		consumeEvents();
		int filesNotFilteredOut= 3;
		String expected= filteredLabel(limitedFilesLabel(result, elementLimit, filesNotFilteredOut), 1);
		assertEquals(expected, fPage.getLabel(),
				"the filtered file must not be counted as hidden by the element limit");
		List<IFile> shownFiles= shownFiles();
		assertEquals(elementLimit, shownFiles.size(), "the viewer must show as many files as the label reports");
		for (IFile shownFile : shownFiles) {
			assertNotEquals(filteredFileName, shownFile.getName(), "the filtered file must not be shown");
		}
	}

	/** Filters all matches of files with the given name. */
	private static final class FileNameFilter extends MatchFilter {

		private final String fFileName;

		FileNameFilter(String fileName) {
			fFileName= fileName;
		}

		@Override
		public boolean filters(Match match) {
			return match.getElement() instanceof IResource resource && fFileName.equals(resource.getName());
		}

		@Override
		public String getName() {
			return "File name";
		}

		@Override
		public String getDescription() {
			return "Filters all matches of files named " + fFileName;
		}

		@Override
		public String getActionLabel() {
			return getName();
		}

		@Override
		public String getID() {
			return "org.eclipse.search.tests.fileNameFilter";
		}
	}

	private String limitedFilesLabel(AbstractTextSearchResult result, int shownFiles) {
		return limitedFilesLabel(result, shownFiles, result.getElementsCount());
	}

	/**
	 * @param totalFiles number of files shown without the element limit, files
	 *                   hidden by a match filter are not counted
	 */
	private String limitedFilesLabel(AbstractTextSearchResult result, int shownFiles, int totalFiles) {
		return Messages.format(SearchMessages.FileSearchPage_limited_format_files, new Object[] {
				result.getLabel(), Integer.valueOf(shownFiles), Integer.valueOf(totalFiles) });
	}

	private String limitedLabel(AbstractTextSearchResult result, int shownMatches) {
		return limitedLabel(result, shownMatches, result.getMatchCount());
	}

	/**
	 * @param totalMatches number of matches shown without the element limit,
	 *                     matches hidden by a match filter are not counted
	 */
	private String limitedLabel(AbstractTextSearchResult result, int shownMatches, int totalMatches) {
		return Messages.format(SearchMessages.FileSearchPage_limited_format_matches, new Object[] {
				result.getLabel(), Integer.valueOf(shownMatches), Integer.valueOf(totalMatches) });
	}

	private String filteredLabel(String label, int filteredOutMatchCount) {
		return Messages.format(SearchMessages.FileSearchPage_filteredWithCount_message,
				new Object[] { label, String.valueOf(filteredOutMatchCount) });
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
		return runQuery(query, elementLimit, AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	private AbstractTextSearchResult runQuery(FileSearchQuery query, int elementLimit, int layout) throws Exception {
		NewSearchUI.runQueryInForeground(null, query);
		ISearchResultViewPart view= NewSearchUI.getSearchResultView();
		fPage= (FileSearchPage) view.getActivePage();
		if (fPreviousElementLimit == null) { // don't overwrite the original state
			fPreviousElementLimit= fPage.getElementLimit();
			fPreviousLayout= fPage.getLayout();
		}
		fPage.setLayout(layout);
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
