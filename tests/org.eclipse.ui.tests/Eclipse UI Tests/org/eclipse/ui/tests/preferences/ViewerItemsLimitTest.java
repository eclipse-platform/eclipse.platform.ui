/*******************************************************************************
 * Copyright (c) 2023 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.internal.ExpandableNode;
import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.views.markers.MarkersTreeViewer;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ViewerItemsLimitTest extends UITestCase {

	private IPreferenceStore preferenceStore;

	private static final int DEFAULT_VIEW_LIMIT = 1000;

	private static final int VIEW_LIMIT_3 = 3;

	private static final int VIEW_LIMIT_DOUBLE = VIEW_LIMIT_3 + VIEW_LIMIT_3;

	private static String SEARCH_VIEW_ID = "org.eclipse.search.ui.views.SearchView";

	private IPerspectiveDescriptor defaultPerspective;

	private IWorkbenchWindow window;

	private IWorkbenchPage activePage;

	public ViewerItemsLimitTest() {
		super(ViewerItemsLimitTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		cleanUp();
		preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		int viewLimit = preferenceStore.getInt(IWorkbenchPreferenceConstants.LARGE_VIEW_LIMIT);
		assertEquals("Default viewer limit must be " + DEFAULT_VIEW_LIMIT, DEFAULT_VIEW_LIMIT, viewLimit);
		window = getActiveWindow();
		activePage = window.getActivePage();
		defaultPerspective = activePage.getPerspective();
		activePage.closeAllPerspectives(false, false);
		getWorkbench().showPerspective(EmptyPerspective.PERSP_ID, window);
	}

	@Override
	protected void doTearDown() throws Exception {
		cleanUp();
		preferenceStore.setValue(IWorkbenchPreferenceConstants.LARGE_VIEW_LIMIT, DEFAULT_VIEW_LIMIT);
		activePage.closeAllPerspectives(false, false);
		if (defaultPerspective != null) {
			getWorkbench().showPerspective(defaultPerspective.getId(), window);
		}
		super.doTearDown();
	}

	/**
	 * Create some projects and open the ProjectExplorer and check if limited
	 * projects are shown. Add one more project and check if newly added project
	 * added to it's position as per the sorting order. Add one more project and
	 * check it goes inside ExpandbaleNode. Delete the newly added project and check
	 * the projects order is as expected. Reset the viewer limit to default and
	 * check if all the project are visible.
	 */
	@Test
	public void testProjectExplorerLimitedProjects() throws CoreException {
		closeView(IPageLayout.ID_PROJECT_EXPLORER);

		setNewViewerLimit(VIEW_LIMIT_3);
		int numberOfProjects = 8;
		for (int i = 0; i < numberOfProjects; i++) {
			IProject javaProj = createProject("javap" + i);
			assertNotNull(javaProj);
		}
		// open project explorer
		IViewPart navigator = activePage.showView(IPageLayout.ID_PROJECT_EXPLORER);
		assertNotNull("failed to open project explorer", navigator);
		processEvents();

		CommonViewer commonViewer = navigator.getAdapter(CommonViewer.class);
		Tree tree = commonViewer.getTree();
		assertLimitedItems(VIEW_LIMIT_3, numberOfProjects, tree.getItems());

		// at second posting `javap1` must be present
		assertEquals("at second position javap1 must be present", "javap1", tree.getItems()[1].getText());

		// create a project which will be inserted at second position as per sorting
		IProject javap = createProject("javap01");
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		numberOfProjects++;

		processBackgroundUpdates(100);

		// still it must show limited items.
		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_3, 30_000);
		assertLimitedItems(VIEW_LIMIT_3, numberOfProjects, tree.getItems());
		// Now at second posting newly created project `javap01` must appear
		assertEquals("at second position javap01 must be present", "javap01", tree.getItems()[1].getText());

		// create a project which will be inserted inside expandable node
		javap = createProject("javap20");
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		numberOfProjects++;

		processBackgroundUpdates(100);
		// still it must show limited items.
		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_3, 30_000);
		assertLimitedItems(VIEW_LIMIT_3, numberOfProjects, tree.getItems());

		// change the viewer limit to 4
		setNewViewerLimit(VIEW_LIMIT_DOUBLE);

		processBackgroundUpdates(100);
		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_DOUBLE, 30_000);
		assertLimitedItems(VIEW_LIMIT_DOUBLE, numberOfProjects, tree.getItems());
		// Now at fourth posting previously added project `javap20` must appear
		assertEquals("at fourth position javap20 must be present", "javap20", tree.getItems()[4].getText());

		// delete the last added project
		javap.delete(true, null);
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		numberOfProjects--;

		processBackgroundUpdates(100);
		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_DOUBLE, 30_000);
		assertLimitedItems(VIEW_LIMIT_DOUBLE, numberOfProjects, tree.getItems());
		assertEquals("at fourth position javap3 must be present", "javap3", tree.getItems()[4].getText());

		setNewViewerLimit(DEFAULT_VIEW_LIMIT);

		processBackgroundUpdates(100);
		assertEquals("all the items must be visible with limit more than input", numberOfProjects,
				tree.getItems().length);
	}

	private IWorkbenchWindow getActiveWindow() {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		assertNotNull("Should get one window", window);
		return window;
	}

	private static void processBackgroundUpdates(int minWaitTime) {
		processEvents();
		waitForJobs(minWaitTime, 10_000);
		processEvents();
	}

	/**
	 * 1. close Intro part. 2. Delete any projects exist. 3. Close the view of our
	 * choice
	 */
	private void cleanUp() throws CoreException {
		deleteAllProjects();
		closeIntro();
		processBackgroundUpdates(100);
	}

	private void closeView(String viewId) {
		IViewPart myView = activePage.findView(viewId);
		activePage.hideView(myView);
	}

	private static void deleteAllProjects() throws CoreException {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject proj : workspaceRoot.getProjects()) {
			proj.delete(true, null);
		}
		workspaceRoot.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	private static IWorkbench closeIntro() {
		IWorkbench workbench = getWorkbench();
		IIntroPart introPart = workbench.getIntroManager().getIntro();
		if (introPart != null) {
			workbench.getIntroManager().closeIntro(introPart);
		}
		return workbench;
	}

	private void assertLimitedItems(int currentLimit, int realInputSize, Item[] items) {
		assertEquals("items length mist be limit plus one", currentLimit + 1, items.length);
		int nextBlock = currentLimit * 2;
		Item lastItem = items[items.length - 1];
		if (nextBlock > realInputSize) {
			nextBlock = realInputSize;
		}
		String expLabel = calculateExpandableLabel(lastItem.getData(), realInputSize);
		ExpandableNode node = (ExpandableNode) lastItem.getData();
		assertEquals(expLabel, node.getLabel());
	}

	private String calculateExpandableLabel(Object data, int realInputSize) {
		ExpandableNode node = (ExpandableNode) data;
		int remaining = realInputSize - node.getOffset();
		String expectedLabel;
		if (remaining > node.getLimit()) {
			expectedLabel = JFaceResources.format("ExpandableNode.defaultLabel", node.getLimit(), remaining); //$NON-NLS-1$
		} else {
			String suffix = remaining == 1 ? "" : "s"; //$NON-NLS-1$
			expectedLabel = JFaceResources.format("ExpandableNode.showRemaining", remaining, suffix); //$NON-NLS-1$
		}
		return expectedLabel;
	}

	private void setNewViewerLimit(int viewerLimit) {
		preferenceStore.setValue(IWorkbenchPreferenceConstants.LARGE_VIEW_LIMIT, viewerLimit);
		int readViewLimit = preferenceStore.getInt(IWorkbenchPreferenceConstants.LARGE_VIEW_LIMIT);
		assertEquals("Default viewer limit must be " + viewerLimit, viewerLimit, readViewLimit);
		processEvents();
	}

	/**
	 * Create some markers on the project and check if limited Markers are shown.
	 * Add one more marker which is at top of items. Check if it appears properly.
	 * Add one more marker check which will go inside expandable node. Delete the
	 * last added marker and check it refreshes properly. Set the viewer limit to
	 * default and check if all the markers are visible.
	 */
	@Test
	public void testMarkersViewLimitedMarkers() throws CoreException {
		closeView(IPageLayout.ID_PROBLEM_VIEW);
		setNewViewerLimit(VIEW_LIMIT_3);
		IProject project = createProject("jp" + 0);
		assertNotNull(project);
		int numberOfMarkers = 8;
		for (int i = 0; i < numberOfMarkers; i++) {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
			attributes.put(IMarker.MESSAGE, i + " project error has occurred");
			attributes.put(IMarker.LOCATION, project.getFullPath().toOSString());
			project.createMarker(IMarker.PROBLEM, attributes);
		}

		IViewPart probView = activePage.showView(IPageLayout.ID_PROBLEM_VIEW);
		// job may take some time to create marker.
		processBackgroundUpdates(1000);
		MarkersTreeViewer commonViewer = probView.getAdapter(MarkersTreeViewer.class);

		processEventsUntil(() -> getFirstItem(commonViewer) != null, 30_000);
		assertNotNull("There must be one problems root element", getFirstItem(commonViewer));
		commonViewer.expandAll();

		processEventsUntil(() -> getFirstItem(commonViewer).getItems().length > VIEW_LIMIT_3, 30_000);
		TreeItem firstItem = getFirstItem(commonViewer);
		assertLimitedItems(VIEW_LIMIT_3, numberOfMarkers, firstItem.getItems());
		assertEquals("0 project error has occurred", firstItem.getItems()[0].getText());

		// create one more marker which will appear at first location of error markers.
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
		attributes.put(IMarker.MESSAGE, "01 project error has occurred");
		attributes.put(IMarker.LOCATION, project.getFullPath().toOSString());
		IMarker errorMarker = project.createMarker(IMarker.PROBLEM, attributes);
		numberOfMarkers++;
		processBackgroundUpdates(1000);

		firstItem = getFirstItem(commonViewer);
		assertLimitedItems(VIEW_LIMIT_3, numberOfMarkers, getFirstItem(commonViewer).getItems());
		assertEquals("01 project error has occurred", getFirstItem(commonViewer).getItems()[0].getText());

		// create one more marker which will appear inside expandable node.
		attributes = new HashMap<>();
		attributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
		attributes.put(IMarker.MESSAGE, "30 project error has occurred");
		attributes.put(IMarker.LOCATION, project.getFullPath().toOSString());
		errorMarker = project.createMarker(IMarker.PROBLEM, attributes);
		numberOfMarkers++;
		processBackgroundUpdates(1000);

		firstItem = getFirstItem(commonViewer);
		assertLimitedItems(VIEW_LIMIT_3, numberOfMarkers, getFirstItem(commonViewer).getItems());

		// change the viewer limit
		setNewViewerLimit(VIEW_LIMIT_DOUBLE);
		processBackgroundUpdates(100);
		commonViewer.expandAll();
		processEventsUntil(() -> getFirstItem(commonViewer).getItems().length > VIEW_LIMIT_DOUBLE, 30_000);

		firstItem = getFirstItem(commonViewer);
		assertLimitedItems(VIEW_LIMIT_DOUBLE, numberOfMarkers, firstItem.getItems());
		assertEquals("30 project error has occurred", firstItem.getItems()[4].getText());

		errorMarker.delete();
		numberOfMarkers--;
		processBackgroundUpdates(1000);

		firstItem = getFirstItem(commonViewer);
		assertLimitedItems(VIEW_LIMIT_DOUBLE, numberOfMarkers, firstItem.getItems());
		assertEquals("3 project error has occurred", firstItem.getItems()[4].getText());

		setNewViewerLimit(DEFAULT_VIEW_LIMIT);

		// job may take some time to create marker.
		processBackgroundUpdates(100);
		commonViewer.expandAll();
		firstItem = getFirstItem(commonViewer);
		assertEquals("all the items must be visible with limit more than input", numberOfMarkers,
				firstItem.getItems().length);
	}

	private TreeItem getFirstItem(MarkersTreeViewer commonViewer) {
		TreeItem[] items = commonViewer.getTree().getItems();
		return items.length > 0 ? items[0] : null;
	}

	/**
	 * Create some projects and some search data. Create some query and execute the
	 * search. Check limited search results. Add new search data onto workspace and
	 * refresh search to see if search results updated properly.
	 */
	@Ignore
	@Test
	public void testLimitedSearchResult() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		setNewViewerLimit(VIEW_LIMIT_3);
		int numOfProj = 9;
		for (int i = 1; i <= numOfProj; i++) {
			createProjectWithFile(workspaceRoot, i);
		}
		processBackgroundUpdates(100);

		SearchView searchView = (SearchView) activePage.showView(SEARCH_VIEW_ID);
		FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(new String[] { "*" }, false);
		FileSearchQuery searchQuery = new FileSearchQuery("some", false, false, false, false, scope);
		NewSearchUI.runQueryInBackground(searchQuery);

		// job may take some time to create marker.
		processBackgroundUpdates(1000);

		FileSearchPage searchPage = (FileSearchPage) searchView.getActivePage();
		assertNotNull("Search page should be shown", searchPage);
		TreeViewer viewer = (TreeViewer) searchPage.getViewer();
		Tree tree = viewer.getTree();
		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_3, 30_000);

		TreeItem[] items = tree.getItems();
		assertLimitedItems(VIEW_LIMIT_3, numOfProj, items);
		assertFirstSearchResultExpanded(items, "jp1", VIEW_LIMIT_3);

		// this is equal to refresh of search results.
		createProjectWithFile(workspaceRoot, 0);
		processBackgroundUpdates(1000);

		numOfProj++;
		NewSearchUI.runQueryInBackground(searchQuery);
		processBackgroundUpdates(1000);

		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_3, 30_000);
		items = tree.getItems();
		assertLimitedItems(VIEW_LIMIT_3, numOfProj, items);
		assertFirstSearchResultExpanded(items, "jp0", VIEW_LIMIT_3);

		// this is equal to refresh of search results.
		createProjectWithFile(workspaceRoot, 50);
		processBackgroundUpdates(1000);

		numOfProj++;
		NewSearchUI.runQueryInBackground(searchQuery);
		processBackgroundUpdates(1000);

		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_3, 30_000);
		items = tree.getItems();
		assertLimitedItems(VIEW_LIMIT_3, numOfProj, items);
		assertFirstSearchResultExpanded(items, "jp0", VIEW_LIMIT_3);

		setNewViewerLimit(VIEW_LIMIT_DOUBLE);
		processBackgroundUpdates(100);

		processEventsUntil(() -> tree.getItems().length > VIEW_LIMIT_DOUBLE, 30_000);
		items = tree.getItems();
		assertLimitedItems(VIEW_LIMIT_DOUBLE, numOfProj, items);

		searchPage.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
		processBackgroundUpdates(100);

		Table table = ((TableViewer) searchPage.getViewer()).getTable();
		processEventsUntil(() -> table.getItems().length > VIEW_LIMIT_DOUBLE, 30_000);
		assertLimitedItems(VIEW_LIMIT_DOUBLE, numOfProj, table.getItems());

		setNewViewerLimit(DEFAULT_VIEW_LIMIT);
		processBackgroundUpdates(100);

		assertEquals("all the items must be visible with limit more than input", numOfProj, table.getItems().length);
	}

	private void assertFirstSearchResultExpanded(TreeItem[] items, String projName, int viewerLimit) {
		assertEquals(projName, items[0].getText());
		assertEquals(1, items[0].getItems().length);
		TreeItem rootItem = items[0].getItems()[0];
		assertEquals("test_file.text (6 matches)", rootItem.getText().trim());
		// here limited results must populate
		TreeItem[] matches = rootItem.getItems();
		assertLimitedItems(viewerLimit, 6, matches);
		assertEquals("1: some line 1", matches[0].getText().trim());
	}

	private void createProjectWithFile(IWorkspaceRoot workspaceRoot, int projNameSuf) throws CoreException {
		IProject testProject = createProject("jp" + projNameSuf);
		testProject.open(null);
		IPath path = IPath.fromOSString("/" + testProject.getName() + "/test_file" + "." + "text");
		IFile temporaryFile = workspaceRoot.getFile(path);
		String content = String.join(System.lineSeparator(), "some line 1", "some line 2", "some line 3", "some line 4",
				"some line 5", "some line 6");
		boolean force = true;
		temporaryFile.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), force, null);
	}

	private IProject createProject(String pname) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(pname);
		if (!project.exists()) {
			project.create(null);
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}
		return project;
	}
}
