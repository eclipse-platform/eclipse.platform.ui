/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Raghunandana Murthappa - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.views.markers.MarkersTreeViewer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the filtered tree used by all markers views, i.e. for the search
 * box narrowing down the shown markers and for the view menu entry
 * showing/hiding that search box.
 * <p>
 * Every aspect is verified for the Problems, the Tasks and the Bookmarks view,
 * since all of them share the same implementation but show different markers
 * with different columns.
 * </p>
 * <p>
 * The views are driven the way a user drives them: the search box is shown and
 * hidden through the check box in the view menu and the markers are filtered by
 * typing into the search box.
 * </p>
 */
public class MarkersFilteredTreeTest {

	/** No {@link IPageLayout} constant exists for the "All Markers" view. */
	private static final String ID_ALL_MARKERS_VIEW = "org.eclipse.ui.views.AllMarkersView";

	/** All views the shared filtered tree implementation is used by. */
	private static final String[] MARKER_VIEW_IDS = { IPageLayout.ID_PROBLEM_VIEW, IPageLayout.ID_TASK_LIST,
			IPageLayout.ID_BOOKMARKS, ID_ALL_MARKERS_VIEW };

	private static final String NOT_MATCHING_PATTERN = "ZzqNoSuchMarkerAtAll";

	private static final String PROJECT_NAME = "MarkersFilteredTreeTestProject";

	private static final long TIMEOUT = 20000;

	/**
	 * Matches the <code>{0}</code>, <code>{1}</code>, ... placeholders of the
	 * message templates the shown counts are verified against.
	 */
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\d+\\}");

	private IWorkbenchPage page;

	private IProject project;

	/**
	 * Marker messages are made unique per test, so that markers of a previous
	 * test - which are removed asynchronously - can never be mistaken for the
	 * ones a test is waiting for.
	 */
	private String matchingMessage;

	private String otherMessage;

	/**
	 * The unique part shared by {@link #matchingMessage} and
	 * {@link #otherMessage}, i.e. a pattern matching exactly the markers created
	 * by the running test and nothing else in the workspace.
	 */
	private String uniqueToken;

	/** Makes the secondary ids of the views opened by a test unique. */
	private int viewCount;

	/** All views opened by a test, so that they can be reset and closed again. */
	private final Set<IViewPart> openedViews = new LinkedHashSet<>();

	@Before
	public void setUp() {
		page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		uniqueToken = "Zzq" + System.nanoTime();
		matchingMessage = "AlphaMarker" + uniqueToken;
		otherMessage = "BetaMarker" + uniqueToken;
		getPreferenceStore().setToDefault(IWorkbenchPreferenceConstants.INITIALLY_SHOW_FILTER_TEXT_IN_MARKER_VIEWS);
	}

	@After
	public void tearDown() throws Exception {
		getPreferenceStore().setToDefault(IWorkbenchPreferenceConstants.INITIALLY_SHOW_FILTER_TEXT_IN_MARKER_VIEWS);
		for (IViewPart view : openedViews) {
			setFilterText(view, "");
			// restore the default, which is also remembered for views opened by
			// subsequent tests
			setSearchBoxVisible(view, true);
			page.hideView(view);
		}
		openedViews.clear();
		if (project != null) {
			FileUtil.deleteProject(project);
			project = null;
			// process all async tasks which we might have triggered
			DisplayHelper.runEventLoop(PlatformUI.getWorkbench().getDisplay(), 100);
		}
	}

	// --- Problems view ---------------------------------------------------

	@Test
	public void testProblemsViewUsesFilteredTree() throws Exception {
		verifyFilteredTreeIsUsed(IPageLayout.ID_PROBLEM_VIEW);
	}

	@Test
	public void testProblemsViewSearchBoxCanBeHiddenAndShown() throws Exception {
		verifySearchBoxCanBeHiddenAndShown(IPageLayout.ID_PROBLEM_VIEW);
	}

	@Test
	public void testProblemsViewFiltersMarkers() throws Exception {
		verifyMarkersAreFiltered(IPageLayout.ID_PROBLEM_VIEW, IMarker.PROBLEM);
	}

	@Test
	public void testProblemsViewHidingSearchBoxResetsFilter() throws Exception {
		verifyHidingSearchBoxResetsFilter(IPageLayout.ID_PROBLEM_VIEW, IMarker.PROBLEM);
	}

	@Test
	public void testProblemsViewDescriptionCountsShownMarkersOnly() throws Exception {
		verifyDescriptionCountsShownMarkersOnly(IPageLayout.ID_PROBLEM_VIEW, IMarker.PROBLEM);
	}

	@Test
	public void testProblemsViewCategoryLabelCountsShownMarkersOnly() throws Exception {
		// the Problems view groups by severity by default
		verifyCategoryLabelCountsShownMarkersOnly(IPageLayout.ID_PROBLEM_VIEW, IMarker.PROBLEM);
	}

	// --- Tasks view ------------------------------------------------------

	@Test
	public void testTasksViewUsesFilteredTree() throws Exception {
		verifyFilteredTreeIsUsed(IPageLayout.ID_TASK_LIST);
	}

	@Test
	public void testTasksViewSearchBoxCanBeHiddenAndShown() throws Exception {
		verifySearchBoxCanBeHiddenAndShown(IPageLayout.ID_TASK_LIST);
	}

	@Test
	public void testTasksViewFiltersMarkers() throws Exception {
		verifyMarkersAreFiltered(IPageLayout.ID_TASK_LIST, IMarker.TASK);
	}

	@Test
	public void testTasksViewHidingSearchBoxResetsFilter() throws Exception {
		verifyHidingSearchBoxResetsFilter(IPageLayout.ID_TASK_LIST, IMarker.TASK);
	}

	@Test
	public void testTasksViewDescriptionCountsShownMarkersOnly() throws Exception {
		verifyDescriptionCountsShownMarkersOnly(IPageLayout.ID_TASK_LIST, IMarker.TASK);
	}

	// --- Bookmarks view --------------------------------------------------

	@Test
	public void testBookmarksViewUsesFilteredTree() throws Exception {
		verifyFilteredTreeIsUsed(IPageLayout.ID_BOOKMARKS);
	}

	@Test
	public void testBookmarksViewSearchBoxCanBeHiddenAndShown() throws Exception {
		verifySearchBoxCanBeHiddenAndShown(IPageLayout.ID_BOOKMARKS);
	}

	@Test
	public void testBookmarksViewFiltersMarkers() throws Exception {
		verifyMarkersAreFiltered(IPageLayout.ID_BOOKMARKS, IMarker.BOOKMARK);
	}

	@Test
	public void testBookmarksViewHidingSearchBoxResetsFilter() throws Exception {
		verifyHidingSearchBoxResetsFilter(IPageLayout.ID_BOOKMARKS, IMarker.BOOKMARK);
	}

	@Test
	public void testBookmarksViewDescriptionCountsShownMarkersOnly() throws Exception {
		verifyDescriptionCountsShownMarkersOnly(IPageLayout.ID_BOOKMARKS, IMarker.BOOKMARK);
	}

	// --- All Markers view -------------------------------------------------

	@Test
	public void testAllMarkersViewUsesFilteredTree() throws Exception {
		verifyFilteredTreeIsUsed(ID_ALL_MARKERS_VIEW);
	}

	@Test
	public void testAllMarkersViewSearchBoxCanBeHiddenAndShown() throws Exception {
		verifySearchBoxCanBeHiddenAndShown(ID_ALL_MARKERS_VIEW);
	}

	@Test
	public void testAllMarkersViewFiltersMarkers() throws Exception {
		verifyMarkersAreFiltered(ID_ALL_MARKERS_VIEW, IMarker.PROBLEM);
	}

	@Test
	public void testAllMarkersViewHidingSearchBoxResetsFilter() throws Exception {
		verifyHidingSearchBoxResetsFilter(ID_ALL_MARKERS_VIEW, IMarker.PROBLEM);
	}

	@Test
	public void testAllMarkersViewDescriptionCountsShownMarkersOnly() throws Exception {
		verifyDescriptionCountsShownMarkersOnly(ID_ALL_MARKERS_VIEW, IMarker.PROBLEM);
	}

	@Test
	public void testAllMarkersViewCategoryLabelCountsShownMarkersOnly() throws Exception {
		// the All Markers view groups by marker type by default
		verifyCategoryLabelCountsShownMarkersOnly(ID_ALL_MARKERS_VIEW, IMarker.PROBLEM);
	}

	// --- all marker views ------------------------------------------------

	@Test
	public void testViewMenuProvidesSearchBoxToggle() throws Exception {
		for (String viewId : MARKER_VIEW_IDS) {
			IViewPart view = openViewInDefaultState(viewId);
			IAction toggle = getShowFilterTextAction(view);

			assertTrue(viewId + ": the toggle is expected to be checked while the search box is shown",
					toggle.isChecked());

			// the framework updates the checked state before running the action
			toggle.setChecked(false);
			toggle.run();
			assertSearchBoxVisibility(viewId + ": running the unchecked toggle must hide the search box", view, false);

			toggle.setChecked(true);
			toggle.run();
			assertSearchBoxVisibility(viewId + ": running the checked toggle must show the search box again", view,
					true);
		}
	}

	@Test
	public void testVisibilityIsRestoredForNewViewInstances() throws Exception {
		for (String viewId : MARKER_VIEW_IDS) {
			IViewPart view = openViewInDefaultState(viewId);
			setSearchBoxVisible(view, false);
			closeView(view);

			assertSearchBoxVisibility(viewId + ": a newly opened view is expected to restore the hidden search box",
					openView(viewId), false);

			IViewPart reopened = openView(viewId);
			setSearchBoxVisible(reopened, true);
			closeView(reopened);

			assertSearchBoxVisibility(viewId + ": a newly opened view is expected to restore the shown search box",
					openView(viewId), true);
		}
	}

	// --- the "initially show text filter" preference ----------------------

	@Test
	public void testPreferenceControlsInitialSearchBoxVisibility() throws Exception {
		for (String viewId : MARKER_VIEW_IDS) {
			setInitiallyShowFilterTextPreference(false);
			assertSearchBoxVisibility(
					viewId + ": a view opened while the preference is disabled must hide the search box",
					openFreshView(viewId), false);

			setInitiallyShowFilterTextPreference(true);
			assertSearchBoxVisibility(
					viewId + ": a view opened while the preference is enabled must show the search box",
					openFreshView(viewId), true);
		}
	}

	@Test
	public void testPreferenceChangeIsAppliedToOpenViews() throws Exception {
		for (String viewId : MARKER_VIEW_IDS) {
			IViewPart view = openFreshView(viewId);
			assertSearchBoxVisibility(viewId + " is expected to show the search box by default", view, true);

			setInitiallyShowFilterTextPreference(false);
			assertSearchBoxVisibility(viewId + ": disabling the preference must hide the search box of an open view",
					view, false);

			setInitiallyShowFilterTextPreference(true);
			assertSearchBoxVisibility(viewId + ": enabling the preference must show the search box of an open view",
					view, true);
		}
	}

	@Test
	public void testExplicitToggleWinsOverPreference() throws Exception {
		for (String viewId : MARKER_VIEW_IDS) {
			IViewPart view = openFreshView(viewId);
			// an explicit decision of the user for this view instance
			setSearchBoxVisible(view, false);

			setInitiallyShowFilterTextPreference(false);
			setInitiallyShowFilterTextPreference(true);

			assertSearchBoxVisibility(
					viewId + ": the preference must not override the search box visibility set by the user", view,
					false);
		}
	}

	// --- verifications shared by all marker views ------------------------

	private void verifyFilteredTreeIsUsed(String viewId) throws Exception {
		IViewPart view = openViewInDefaultState(viewId);
		FilteredTree filteredTree = getFilteredTree(view);

		assertTrue(viewId + " filtered tree is expected to host the markers viewer",
				filteredTree.getViewer() instanceof MarkersTreeViewer);

		Text filterControl = getFilterControl(view);
		assertEquals(viewId + " is expected to show the markers specific hint",
				MarkerMessages.MarkerView_searchFilterInitialText, filterControl.getMessage());
		assertEquals("the hint must not be shown as the field's value", "", filterControl.getText());
	}

	private void verifySearchBoxCanBeHiddenAndShown(String viewId) throws Exception {
		IViewPart view = openViewInDefaultState(viewId);
		assertSearchBoxVisibility(viewId + " is expected to show the search box by default", view, true);

		setSearchBoxVisible(view, false);
		assertSearchBoxVisibility(viewId + ": the search box was expected to be hidden", view, false);

		setSearchBoxVisible(view, true);
		assertSearchBoxVisibility(viewId + ": the search box was expected to be shown again", view, true);
	}

	private void verifyMarkersAreFiltered(String viewId, String markerType) throws Exception {
		IViewPart view = openViewWithTestMarkers(viewId, markerType);

		filterOutAllMarkers(viewId, view);

		setFilterText(view, matchingMessage);
		waitUntil(viewId + ": only the matching marker was expected to stay visible", view,
				showsMatchingMarkerOnly());

		// markers must also match by the values of the other visible columns,
		// the path column shows the project the markers were created in
		setFilterText(view, PROJECT_NAME);
		waitUntil(viewId + ": markers were expected to match by their path, too", view, showsBothMarkers());

		setFilterText(view, "");
		waitUntil(viewId + ": all markers were expected to be shown again", view, showsBothMarkers());
	}

	private void verifyHidingSearchBoxResetsFilter(String viewId, String markerType) throws Exception {
		IViewPart view = openViewWithTestMarkers(viewId, markerType);

		filterOutAllMarkers(viewId, view);

		setSearchBoxVisible(view, false);

		assertEquals("hiding the search box must reset the filter text", "", getFilterControl(view).getText());
		waitUntil(viewId + ": markers stayed filtered out although the search box was hidden", view,
				showsBothMarkers());
	}

	/**
	 * Verifies that the counts reported in the view's content description
	 * reflect the markers actually shown, i.e. that they honor the search box
	 * filter and not only the marker limit.
	 * <p>
	 * Only states in which strictly less markers are shown than the view holds
	 * in total are verified, since only those report a shown count at all - the
	 * two markers created by the test guarantee that. The absolute total is
	 * intentionally not asserted, as the workspace may hold markers of other
	 * tests; it is only verified to stay stable while filtering, since a search
	 * box filter must never change it.
	 * </p>
	 */
	private void verifyDescriptionCountsShownMarkersOnly(String viewId, String markerType) throws Exception {
		IViewPart view = openFreshViewWithTestMarkers(viewId, markerType);

		setFilterText(view, matchingMessage);
		int[] oneShown = waitForDescriptionCounts(viewId + ": one of the two markers matches the search box", view, 1);

		setFilterText(view, NOT_MATCHING_PATTERN);
		int[] noneShown = waitForDescriptionCounts(viewId + ": no marker matches the search box", view, 0);

		assertTrue(viewId + ": the view is expected to hold at least the two markers created by this test, but the"
				+ " description reported a total of " + oneShown[1], oneShown[1] >= 2);
		assertEquals(viewId + ": the search box must not change the total number of markers reported", oneShown[1],
				noneShown[1]);
	}

	/**
	 * Verifies that the item counts in the labels of the categories a view
	 * groups its markers into reflect the markers actually shown below them,
	 * i.e. that they honor the search box filter.
	 * <p>
	 * The counts are summed up over all categories left visible, so that the
	 * verification does not depend on how the markers created by the test are
	 * distributed over the categories. Markers of other tests cannot contribute
	 * to the sum, since the used patterns only match the markers of this test.
	 * </p>
	 */
	private void verifyCategoryLabelCountsShownMarkersOnly(String viewId, String markerType) throws Exception {
		IViewPart view = openFreshViewWithTestMarkers(viewId, markerType);
		waitUntil(viewId + " is expected to group its markers into categories",
				() -> getShownCategoryItemCount(view) >= 0);

		setFilterText(view, uniqueToken);
		waitForShownCategoryItemCount(viewId + ": both markers of this test match the search box", view, 2);

		setFilterText(view, matchingMessage);
		waitForShownCategoryItemCount(viewId + ": one of the two markers matches the search box", view, 1);

		setFilterText(view, NOT_MATCHING_PATTERN);
		waitForShownCategoryItemCount(viewId + ": no marker matches the search box", view, 0);
	}

	/**
	 * Opens the given view and waits until the markers created for the test are
	 * shown by it.
	 */
	private IViewPart openViewWithTestMarkers(String viewId, String markerType) throws Exception {
		IViewPart view = openViewInDefaultState(viewId);
		createTestMarkers(markerType);
		waitUntil(viewId + ": markers were not shown", view, showsBothMarkers());
		return view;
	}

	/**
	 * Opens a view instance that never persisted any state and waits until the
	 * markers created for the test are shown by it, so that grouping, filters
	 * and search box visibility are the documented defaults.
	 */
	private IViewPart openFreshViewWithTestMarkers(String viewId, String markerType) throws Exception {
		IViewPart view = openFreshView(viewId);
		createTestMarkers(markerType);
		waitUntil(viewId + ": markers were not shown", view, showsBothMarkers());
		return view;
	}

	/**
	 * Filters with a pattern no marker can match and waits until the view is
	 * empty.
	 */
	private void filterOutAllMarkers(String viewId, IViewPart view) {
		setFilterText(view, NOT_MATCHING_PATTERN);
		waitUntil(viewId + ": markers were not filtered out", () -> getTree(view).getItemCount() == 0);
	}

	private static void assertSearchBoxVisibility(String message, IViewPart view, boolean expectedVisible) {
		assertEquals(message, expectedVisible, getShowFilterTextAction(view).isChecked());
		assertEquals(message, expectedVisible, getFilteredTree(view).isShowFilterControls());
		assertEquals(message, expectedVisible, getFilterComposite(view).getVisible());
	}

	// --- helpers ---------------------------------------------------------

	/**
	 * Opens the view and resets it to the state all tests start from, i.e. with
	 * a shown and empty search box.
	 */
	private IViewPart openViewInDefaultState(String viewId) throws Exception {
		IViewPart view = openView(viewId);
		setSearchBoxVisible(view, true);
		setFilterText(view, "");
		return view;
	}

	private IViewPart openView(String viewId) throws Exception {
		IViewPart view = page.showView(viewId);
		assertNotNull(viewId + " is expected to be a markers view", view.getAdapter(MarkersTreeViewer.class));
		openedViews.add(view);
		return view;
	}

	/**
	 * Opens an additional instance of the given view under a secondary id no
	 * view ever used before. Such an instance starts without any persisted
	 * state, so that it uses the default grouping and derives the visibility of
	 * its search box from the preference instead of from a decision the user -
	 * or a previous test - made for the primary view instance.
	 */
	private IViewPart openFreshView(String viewId) throws Exception {
		String secondaryId = uniqueToken + "_" + viewCount++;
		IViewPart view = page.showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		assertNotNull(viewId + " is expected to be a markers view", view.getAdapter(MarkersTreeViewer.class));
		openedViews.add(view);
		return view;
	}

	private void closeView(IViewPart view) {
		openedViews.remove(view);
		page.hideView(view);
	}

	/**
	 * Shows or hides the search box the way the user does it, i.e. through the
	 * check box contributed to the view menu.
	 */
	private static void setSearchBoxVisible(IViewPart view, boolean visible) {
		IAction toggle = getShowFilterTextAction(view);
		toggle.setChecked(visible);
		toggle.run();
	}

	private static IAction getShowFilterTextAction(IViewPart view) {
		for (IContributionItem item : view.getViewSite().getActionBars().getMenuManager().getItems()) {
			if (item instanceof ActionContributionItem contribution
					&& MarkerMessages.MarkerView_showFilterText.equals(contribution.getAction().getText())) {
				return contribution.getAction();
			}
		}
		throw new AssertionError(
				view.getViewSite().getId() + " is expected to contribute a search box toggle to its view menu");
	}

	private static IPreferenceStore getPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Sets the preference deciding whether marker views initially show their
	 * search box. Views honoring it are updated synchronously, so that callers
	 * can assert on them right away.
	 */
	private static void setInitiallyShowFilterTextPreference(boolean initiallyShown) {
		getPreferenceStore().setValue(IWorkbenchPreferenceConstants.INITIALLY_SHOW_FILTER_TEXT_IN_MARKER_VIEWS,
				initiallyShown);
	}

	/**
	 * Returns the filtered tree hosting the markers of the given view, found by
	 * walking up from the markers viewer the view adapts to.
	 */
	private static FilteredTree getFilteredTree(IViewPart view) {
		MarkersTreeViewer viewer = view.getAdapter(MarkersTreeViewer.class);
		assertNotNull("the markers view is expected to adapt to its viewer", viewer);
		Composite parent = viewer.getTree().getParent();
		while (parent != null && !(parent instanceof FilteredTree)) {
			parent = parent.getParent();
		}
		assertTrue("the markers viewer is expected to be hosted in a filtered tree", parent instanceof FilteredTree);
		return (FilteredTree) parent;
	}

	private void createTestMarkers(String markerType) throws Exception {
		if (project == null) {
			project = FileUtil.createProject(PROJECT_NAME);
		}
		IFile file = FileUtil.createFile("markers.txt", project);
		createMarker(file, markerType, matchingMessage);
		createMarker(file, markerType, otherMessage);
	}

	private static void createMarker(IFile file, String markerType, String message) throws Exception {
		IMarker marker = file.createMarker(markerType);
		marker.setAttribute(IMarker.MESSAGE, message);
		if (IMarker.PROBLEM.equals(markerType)) {
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}
	}

	private static void setFilterText(IViewPart view, String text) {
		getFilterControl(view).setText(text);
	}

	private static Text getFilterControl(IViewPart view) {
		Text filterControl = getFilteredTree(view).getFilterControl();
		assertNotNull("the filtered tree is expected to provide a search box", filterControl);
		return filterControl;
	}

	private static Composite getFilterComposite(IViewPart view) {
		return getFilterControl(view).getParent();
	}

	private static Tree getTree(IViewPart view) {
		return getFilteredTree(view).getViewer().getTree();
	}

	/** Condition matching while both markers created for the test are shown. */
	private Predicate<List<String>> showsBothMarkers() {
		return shows(matchingMessage).and(shows(otherMessage));
	}

	/**
	 * Condition matching while only the marker the tests filter for is shown.
	 */
	private Predicate<List<String>> showsMatchingMarkerOnly() {
		return shows(matchingMessage).and(shows(otherMessage).negate());
	}

	/**
	 * Returns a condition on all values currently shown in a view, matching when
	 * any of them contains the given text.
	 */
	private static Predicate<List<String>> shows(String text) {
		return values -> values.stream().anyMatch(value -> value.contains(text));
	}

	/**
	 * Collects all values currently shown in the given view. Categories are
	 * expanded first, since markers are shown as their children when the view is
	 * grouped.
	 */
	private static List<String> getShownValues(IViewPart view) {
		TreeViewer viewer = getFilteredTree(view).getViewer();
		viewer.expandAll();
		List<String> values = new ArrayList<>();
		collectValues(getTree(view).getItems(), values);
		return values;
	}

	private static void collectValues(TreeItem[] items, List<String> values) {
		for (TreeItem item : items) {
			int columnCount = Math.max(1, item.getParent().getColumnCount());
			for (int column = 0; column < columnCount; column++) {
				String value = item.getText(column);
				if (value != null) {
					values.add(value);
				}
			}
			collectValues(item.getItems(), values);
		}
	}

	private static void waitUntil(String message, IViewPart view, Predicate<List<String>> condition) {
		waitUntil(message, () -> condition.test(getShownValues(view)));
	}

	private static void waitUntil(String message, BooleanSupplier condition) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		assertTrue(message, DisplayHelper.waitForCondition(display, TIMEOUT, condition));
	}

	// --- the counts reported by a view -----------------------------------

	/**
	 * Waits until the view's content description reports the given number of
	 * shown markers, since it is updated asynchronously once the search box
	 * filter has been applied to the tree.
	 *
	 * @return the reported number of shown markers and the reported total
	 */
	private static int[] waitForDescriptionCounts(String context, IViewPart view, int expectedShown) {
		int[][] reported = new int[1][];
		Display display = PlatformUI.getWorkbench().getDisplay();
		boolean reachedExpectation = DisplayHelper.waitForCondition(display, TIMEOUT, () -> {
			reported[0] = parseDescriptionCounts(getContentDescription(view));
			return reported[0] != null && reported[0][0] == expectedShown;
		});
		assertTrue(context + ": the view was expected to report " + expectedShown
				+ " shown markers, but its description was '" + getContentDescription(view) + "'", reachedExpectation);
		return reported[0];
	}

	private static String getContentDescription(IViewPart view) {
		return ((IWorkbenchPart2) view).getContentDescription();
	}

	/**
	 * Extracts the number of shown markers and the total number of markers from
	 * a view's content description.
	 *
	 * @return both counts, or <code>null</code> if the description does not
	 *         report them, which is the case while all markers are shown
	 */
	private static int[] parseDescriptionCounts(String description) {
		// the first message wins that both matches and yields numbers, so the
		// most specific one has to be tried first
		int[] counts = parseCounts(MarkerMessages.problem_filter_matchedMessage, description, 1, 2);
		if (counts == null) {
			counts = parseCounts(MarkerMessages.filter_matchedMessage, description, 0, 1);
		}
		if (counts == null) {
			// all markers are shown, so shown and total are the same
			counts = parseCounts(MarkerMessages.filter_itemsMessage, description, 0, 0);
		}
		return counts;
	}

	/**
	 * Waits until the categories left visible in the given view report the
	 * given number of shown markers in total.
	 */
	private static void waitForShownCategoryItemCount(String context, IViewPart view, int expectedCount) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		boolean reachedExpectation = DisplayHelper.waitForCondition(display, TIMEOUT,
				() -> getShownCategoryItemCount(view) == expectedCount);
		assertTrue(context + ": the categories were expected to report " + expectedCount
				+ " shown markers in total, but the root items were " + getRootItemLabels(view), reachedExpectation);
	}

	/**
	 * Sums up the item counts reported by the labels of all categories
	 * currently visible in the given view.
	 *
	 * @return the total number of markers the categories report as shown, or
	 *         <code>-1</code> if the view does not group its markers into
	 *         categories
	 */
	private static int getShownCategoryItemCount(IViewPart view) {
		int total = 0;
		for (TreeItem rootItem : getTree(view).getItems()) {
			Integer count = parseCategoryItemCount(rootItem.getText(0));
			if (count == null) {
				return -1;
			}
			total += count.intValue();
		}
		return total;
	}

	/**
	 * Extracts the number of shown markers from the label of a category.
	 *
	 * @return the reported number, or <code>null</code> if the given label is
	 *         not the label of a category
	 */
	private static Integer parseCategoryItemCount(String label) {
		// the first message wins that both matches and yields a number, so the
		// most specific one has to be tried first
		int[] counts = parseCounts(MarkerMessages.Category_Limit_Label, label, 1, 2);
		if (counts == null) {
			counts = parseCounts(MarkerMessages.Category_Label, label, 1, 1);
		}
		if (counts != null) {
			return Integer.valueOf(counts[0]);
		}
		return parseArguments(MarkerMessages.Category_One_Item_Label, label) == null ? null : Integer.valueOf(1);
	}

	private static List<String> getRootItemLabels(IViewPart view) {
		List<String> labels = new ArrayList<>();
		for (TreeItem rootItem : getTree(view).getItems()) {
			labels.add(rootItem.getText(0));
		}
		return labels;
	}

	/**
	 * Extracts two numbers from a message that was created by binding the given
	 * NLS template.
	 *
	 * @param pattern    the NLS template the text was created from
	 * @param text       the text to extract the numbers from
	 * @param firstIndex the index of the placeholder holding the first number
	 * @param lastIndex  the index of the placeholder holding the second number
	 * @return both numbers, or <code>null</code> if the text was not created
	 *         from the given template or does not hold numbers
	 */
	private static int[] parseCounts(String pattern, String text, int firstIndex, int lastIndex) {
		List<String> arguments = parseArguments(pattern, text);
		if (arguments == null || arguments.size() <= Math.max(firstIndex, lastIndex)) {
			return null;
		}
		try {
			return new int[] { Integer.parseInt(arguments.get(firstIndex).trim()),
					Integer.parseInt(arguments.get(lastIndex).trim()) };
		} catch (NumberFormatException e) {
			// the text matches the template by chance only
			return null;
		}
	}

	/**
	 * Extracts the values that were bound to the placeholders of an NLS
	 * template. All templates used here hold their placeholders in ascending
	 * order, so that the n-th returned value is the value of
	 * <code>{n}</code>.
	 *
	 * @return the bound values, or <code>null</code> if the given text was not
	 *         created from the given template
	 */
	private static List<String> parseArguments(String pattern, String text) {
		if (text == null) {
			return null;
		}
		StringBuilder regex = new StringBuilder();
		Matcher placeholders = PLACEHOLDER.matcher(pattern);
		int literalStart = 0;
		while (placeholders.find()) {
			regex.append(Pattern.quote(pattern.substring(literalStart, placeholders.start())));
			regex.append("(.*)");
			literalStart = placeholders.end();
		}
		regex.append(Pattern.quote(pattern.substring(literalStart)));

		Matcher matcher = Pattern.compile(regex.toString(), Pattern.DOTALL).matcher(text);
		if (!matcher.matches()) {
			return null;
		}
		List<String> arguments = new ArrayList<>(matcher.groupCount());
		for (int group = 1; group <= matcher.groupCount(); group++) {
			arguments.add(matcher.group(group));
		}
		return arguments;
	}
}
