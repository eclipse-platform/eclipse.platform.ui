/*******************************************************************************
 * Copyright (c) 2003, 2015, 2019 IBM Corporation and others.
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
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *     Stefan Winkler <stefan@winklerweb.net> - bug 178019 - CNF Tooltip support
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorActionService;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.navigator.extension.TestContentProvider;
import org.eclipse.ui.tests.navigator.extension.TestContentProviderPipelined;
import org.eclipse.ui.tests.navigator.extension.TestContentProviderResource;
import org.eclipse.ui.tests.navigator.extension.TestDragAssistant;
import org.eclipse.ui.tests.navigator.extension.TestEmptyContentProvider;
import org.eclipse.ui.tests.navigator.extension.TestLabelProvider;
import org.eclipse.ui.tests.navigator.extension.TestSorterData;
import org.eclipse.ui.tests.navigator.extension.TestSorterResource;
import org.eclipse.ui.tests.navigator.m12.model.ResourceWrapper;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;
import org.junit.After;
import org.junit.Before;

public class NavigatorTestBase {

	public static final String COMMON_NAVIGATOR_RESOURCE_EXT = "org.eclipse.ui.navigator.resourceContent";

	public static final String COMMON_NAVIGATOR_JAVA_EXT = "org.eclipse.jdt.java.ui.javaContent";

	public static final String TEST_VIEWER = "org.eclipse.ui.tests.navigator.TestView";
	public static final String TEST_VIEWER_PROGRAMMATIC = "org.eclipse.ui.tests.navigator.ProgrammaticTestView";
	public static final String TEST_VIEWER_PIPELINE = "org.eclipse.ui.tests.navigator.PipelineTestView";
	public static final String TEST_VIEWER_HIDE_EXTENSIONS = "org.eclipse.ui.tests.navigator.HideAvailableExtensionsTestView";
	public static final String TEST_VIEWER_INHERITED = "org.eclipse.ui.tests.navigator.InheritedTestView";
	public static final String TEST_VIEWER_NON_COMMONVIEWER = "org.eclipse.ui.tests.navigator.NonCommonViewerTestViewer";
	public static final String TEST_VIEWER_FILTER = "org.eclipse.ui.tests.navigator.FilterTestView";
	public static final String TEST_VIEWER_INITIAL_ACTIVATION = "org.eclipse.ui.tests.navigator.InitialActivationView";
	public static final String TEST_VIEWER_LINK_HELPER = "org.eclipse.ui.tests.navigator.TestLinkHelperView";
	public static final String TEST_VIEWER_SHOW_IN = "org.eclipse.ui.tests.navigator.TestShowInView";

	public static final String TEST_VIEW_NON_COMMONVIEWER = "org.eclipse.ui.tests.navigator.NonCommonViewerTestView";

	public static final String TEST_CONTENT = "org.eclipse.ui.tests.navigator.testContent";
	public static final String TEST_CONTENT2 = "org.eclipse.ui.tests.navigator.testContent2";
	public static final String TEST_CONTENT_OVERRIDDEN1 = "org.eclipse.ui.tests.navigator.testContentOverridden1";
	public static final String TEST_CONTENT_OVERRIDDEN2 = "org.eclipse.ui.tests.navigator.testContentOverridden2";
	public static final String TEST_CONTENT_OVERRIDE1 = "org.eclipse.ui.tests.navigator.testContentOverride1";
	public static final String TEST_CONTENT_OVERRIDE2 = "org.eclipse.ui.tests.navigator.testContentOverride2";
	public static final String TEST_CONTENT_OVERRIDE2_BLANK = "org.eclipse.ui.tests.navigator.testContentOverride2Blank";
	public static final String TEST_CONTENT_RESOURCE_OVERRIDE = "org.eclipse.ui.tests.navigator.testContentResourceOverride";
	public static final String TEST_CONTENT_PIPELINE = "org.eclipse.ui.tests.navigator.testPipeline";
	public static final String TEST_CONTENT_WITH = "org.eclipse.ui.tests.navigator.testContentWith";
	public static final String TEST_CONTENT_NO_CHILDREN = "org.eclipse.ui.tests.navigator.testContentNoChildren";
	public static final String TEST_CONTENT_EMPTY = "org.eclipse.ui.tests.navigator.testContentEmpty";
	public static final String TEST_CONTENT_TOOLTIPS = "org.eclipse.ui.tests.navigator.testContentTooltips";

	public static final String TEST_CONTENT_RESOURCE_UNSORTED = "org.eclipse.ui.tests.navigator.resourceContent.unsorted";

	public static final String TEST_CONTENT_INITIAL_ACTIVATION_FALSE = "org.eclipse.ui.tests.navigator.testInitialActivationFalse";
	public static final String TEST_CONTENT_INITIAL_ACTIVATION_TRUE = "org.eclipse.ui.tests.navigator.testInitialActivationTrue";

	public static final String TEST_SIMPLE_CHILDREN1 = "org.eclipse.ui.tests.navigator.testSimpleChildrenContent1";
	public static final String TEST_SIMPLE_CHILDREN2 = "org.eclipse.ui.tests.navigator.testSimpleChildrenContent2";
	public static final String TEST_SIMPLE_CHILDREN3 = "org.eclipse.ui.tests.navigator.testSimpleChildrenContent3";
	public static final String TEST_SIMPLE_CHILDREN_NOT_FOUND = "org.eclipse.ui.tests.navigator.testSimpleChildrenAppearsBeforeNotFound";

	public static final String TEST_CONTENT_M12_VIEW = "org.eclipse.ui.tests.navigator.M12View";
	public static final String TEST_CONTENT_M12_M1_CONTENT = "org.eclipse.ui.tests.navigator.m12.M1";
	public static final String TEST_CONTENT_M12_M1_CONTENT_FIRST_CLASS = "org.eclipse.ui.tests.navigator.m12.M1FirstClass";
	public static final String TEST_CONTENT_M12_M2_CONTENT = "org.eclipse.ui.tests.navigator.m12.M2";

	public static final String TEST_CONTENT_LABEL1 = "org.eclipse.ui.tests.navigator.testContentLabel1";
	public static final String TEST_CONTENT_LABEL2 = "org.eclipse.ui.tests.navigator.testContentLabel2";

	public static final String TEST_CONTENT_COMPARATOR_MODEL = "org.eclipse.ui.tests.navigator.testContentComparatorModel";
	public static final String TEST_CONTENT_SORTER_MODEL = "org.eclipse.ui.tests.navigator.testContentSorterModel";
	public static final String TEST_CONTENT_SORTER_MODEL_OVERRIDE = "org.eclipse.ui.tests.navigator.testContentSorterModel.override";
	public static final String TEST_CONTENT_SORTER_MODEL_OVERRIDE_NOSORT = "org.eclipse.ui.tests.navigator.testContentSorterModel.override.nosort";
	public static final String TEST_CONTENT_SORTER_RESOURCE = "org.eclipse.ui.tests.navigator.testContentSorterResource";
	public static final String TEST_CONTENT_SORTER_RESOURCE_SORTONLY = "org.eclipse.ui.tests.navigator.testContentSorterResource.sortOnly";
	public static final String TEST_CONTENT_SORTER_RESOURCE_SORTONLY_OVERRIDE = "org.eclipse.ui.tests.navigator.testContentSorterResource.sortOnly.override";
	public static final String TEST_CONTENT_SORTER_RESOURCE_OVERRIDE = "org.eclipse.ui.tests.navigator.testContentSorterResource.override";
	public static final String TEST_CONTENT_SORTER_RESOURCE_OVERRIDE_SORTER = "org.eclipse.ui.tests.navigator.testContentSorterResource.override.sorter";
	public static final String TEST_CONTENT_SORTER_BASIC_A = "org.eclipse.ui.tests.navigator.testContentBasic.a";
	public static final String TEST_CONTENT_SORTER_BASIC_B = "org.eclipse.ui.tests.navigator.testContentBasic.b";
	public static final String TEST_CONTENT_SORTER_BASIC_SORTONLY_SORTER = "org.eclipse.ui.tests.navigator.testContentBasic.sortOnlySorter";

	public static final String TEST_CONTENT_REDLABEL = "org.eclipse.ui.tests.navigator.testContentRedLabel";
	public static final String TEST_CONTENT_MISSINGLABEL = "org.eclipse.ui.tests.navigator.testContentMissingLabel";
	public static final String TEST_CONTENT_DROP_COPY = "org.eclipse.ui.tests.navigator.testContentDropCopy";
	public static final String TEST_CONTENT_HAS_CHILDREN = "org.eclipse.ui.tests.navigator.testContentHasChildren";
	public static final String TEST_CONTENT_ACTION_PROVIDER = "org.eclipse.ui.tests.navigator.testContentActionProvider";

	public static final String TEST_CONTENT_TRACKING_LABEL = "org.eclipse.ui.tests.navigator.testTrackingLabel";

	public static final String TEST_CONTENT_JST = "org.eclipse.ui.tests.navigator.jst.ContentProvider";

	protected static final String TEST_ACTIVITY = "org.eclipse.ui.tests.navigator.testActivity";
	protected static final String TEST_ACTIVITY_PROVIDER = "org.eclipse.ui.tests.navigator.testActivityProvider";

	public static final String TEST_ACTION_PROVIDER_PRIORITY = "org.eclipse.ui.tests.navigator.extension.TestActionProviderPriority";

	protected static final String ACTION_NESTED = "org.eclipse.ui.tests.navigator.NestedAction";

	public static final String TEST_VIEWER_HELP_CONTEXT = "org.eclipse.ui.tests.navigator.testHelpContext";

	public static final String TEST_C_CONTENT = "org.eclipse.ui.tests.navigator.cdt.content";

	public static final String TEST_FILTER_P1 = "org.eclipse.ui.tests.navigator.filters.p1";
	public static final String TEST_FILTER_P2 = "org.eclipse.ui.tests.navigator.filters.p2";

	protected String _navigatorInstanceId;

	protected Set<IResource> _expectedChildren = new HashSet<>();

	protected IProject _project;
	protected IProject _p1;
	protected IProject _p2;

	protected static final int _p1Ind = 0;
	protected static final int _p2Ind = 1;
	protected static final int _projectInd = 2;

	protected static int _projectCount;

	protected CommonViewer _viewer;

	protected CommonNavigator _commonNavigator;

	protected INavigatorContentService _contentService;
	protected NavigatorActionService _actionService;

	protected boolean _initTestData = true;

	protected static final boolean DEBUG = false;

	public NavigatorTestBase() {
		// placeholder
	}

	public NavigatorTestBase(String name) {
		// Nothing
	}

	@Before
	public void setUp() throws CoreException {

		if (_navigatorInstanceId == null) {
			throw new RuntimeException("Set the _navigatorInstanceId in the constructor");
		}

		// Easier if this is not around when not needed
		if (!_navigatorInstanceId.equals(ProjectExplorer.VIEW_ID)) {
			EditorTestHelper.showView(ProjectExplorer.VIEW_ID, false);
		}

		TestContentProviderPipelined.resetTest();
		TestContentProviderResource.resetTest();
		TestSorterData.resetTest();
		TestSorterResource.resetTest();
		TestLabelProvider.resetTest();
		TestDragAssistant.resetTest();
		TestEmptyContentProvider.resetTest();

		if (_initTestData) {
			clearAll();
			TestWorkspace.init();

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			_project = root.getProject("Test"); //$NON-NLS-1$

			_expectedChildren.add(_project.getFolder("src")); //$NON-NLS-1$
			_expectedChildren.add(_project.getFolder("bin")); //$NON-NLS-1$
			_expectedChildren.add(_project.getFile(".project")); //$NON-NLS-1$
			_expectedChildren.add(_project.getFile(".classpath")); //$NON-NLS-1$
			_expectedChildren.add(_project.getFile("model.properties")); //$NON-NLS-1$

			_p1 = ResourcesPlugin.getWorkspace().getRoot().getProject("p1");
			_p1.open(null);
			_p2 = ResourcesPlugin.getWorkspace().getRoot().getProject("p2");
			_p2.open(null);

			_projectCount = 3;
		}

		//lookAt();

		showNavigator();
		refreshViewer();

		_contentService = _viewer.getNavigatorContentService();
		_actionService = _commonNavigator.getNavigatorActionService();

		((NavigatorFilterService) _contentService.getFilterService()).resetFilterActivationState();

	}

	protected void lookAt() {
		DisplayHelper.sleep(1000000);
	}

	protected void waitForModelObjects() throws Exception {
		_project.findMember(TestContentProvider.MODEL_FILE_PATH).touch(null);
		// Let build run to load the model objects
		DisplayHelper.sleep(50);
	}

	/**
	 * Wait for up to 1 minute for a condition to be met.
	 *
	 * @param description A description of the condition (for debugging purposes).
	 * @param condition   The condition to be met.
	 */
	protected void waitForCondition(String description, BooleanSupplier condition) {
		for (int i = 1; i <= 1200; i++) {

			if (condition.getAsBoolean()) {
				System.out.println("The condition '" + description + "' was met in " + (i * 50) + " ms or less");
				return;
			}
			DisplayHelper.sleep(50);
		}

		fail("The condition '" + description + "' was never met");
	}

	protected void showNavigator() throws PartInitException {
		EditorTestHelper.showView(_navigatorInstanceId, true);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();

		_commonNavigator = (CommonNavigator) activePage.findView(_navigatorInstanceId);
		_commonNavigator.setFocus();
		_viewer = _commonNavigator.getAdapter(CommonViewer.class);
	}

	@After
	public void tearDown() throws CoreException {
		clearAll();
		// Hide it, we want a new one each time
		EditorTestHelper.showView(_navigatorInstanceId, false);
	}

	protected void clearAll() throws CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			FileUtil.delete(project);
		}
	}

	/**
	 * Use this to not have to wait for the label provider to refresh after changing
	 * the activation.  Otherwise we would have to have a small delay in each test
	 * case which we don't want.
	 */
	protected void refreshViewer() {
		try {
			// Setting the text in the tree to be empty forces the
			// DecoratingStyledCellLabelProvider
			// to refresh immediately and not wait for one that is scheduled to
			// run.
			TreeItem[] rootItems = _viewer.getTree().getItems();
			for (TreeItem rootItem : rootItems) {
				rootItem.setText("");
			}
		} catch (Exception ex) {
			// Ignore
		}
		_viewer.refresh();
	}

	protected Object verifyMenu(IStructuredSelection sel, String item) {
		MenuManager mm = new MenuManager();
		_actionService.setContext(new ActionContext(sel));
		_actionService.fillContextMenu(mm);

		IContributionItem[] items = mm.getItems();
		for (IContributionItem item1 : items) {
			if (item1 instanceof MenuManager) {
				MenuManager childMm = (MenuManager) item1;
				if (DEBUG) {
					System.out.println("menu text: " + childMm.getMenuText());
				}
				if (childMm.getMenuText().contains(item))
					return childMm;
			} else if (item1 instanceof ActionContributionItem) {
				ActionContributionItem aci = (ActionContributionItem) item1;
				if (DEBUG) {
					System.out.println("action text: " + aci.getAction().getText());
				}
				if (aci.getAction().getText().contains(item))
					return aci;
			}
		}

		return null;
	}

	protected boolean verifyMenu(IStructuredSelection sel, String item, boolean useNewMenu) {
		MenuManager mm = new MenuManager();
		_actionService.setContext(new ActionContext(sel));
		_actionService.fillContextMenu(mm);

		IContributionItem[] items = mm.getItems();

		if (useNewMenu) {
			MenuManager newMm = (MenuManager) items[1];
			items = newMm.getItems();
		}

		for (IContributionItem i : items) {
			if (i instanceof ActionContributionItem) {
				ActionContributionItem aci = (ActionContributionItem) i;
				if (aci.getAction().getText().startsWith(item))
					return true;
				if (DEBUG)
					System.out.println("action text: " + aci.getAction().getText());
			}
		}

		return false;
	}

	protected static final boolean ALL = true;
	protected static final boolean TEXT = true;

	protected void checkItems(TreeItem[] rootItems, TestLabelProvider tlp) {
		checkItems(rootItems, tlp, ALL, TEXT);
	}

	protected void checkItemsText(TreeItem[] rootItems, TestLabelProvider tlp, boolean all) {
		checkItems(rootItems, tlp, all, TEXT);
	}

	protected void checkItems(TreeItem[] rootItems, TestLabelProvider tlp, boolean all, boolean text) {
		for (TreeItem rootItem : rootItems) {
			// Skip the dummy items (for the + placeholder)
			if (rootItem.getText() == null || rootItem.getText().equals("")) {
				continue;
			}
			if (text && !rootItem.getText().startsWith(tlp.getColorName())) {
				fail("Wrong text: " + rootItem.getText());
			}
			assertEquals(tlp.backgroundColor, rootItem.getBackground(0));
			assertEquals(TestLabelProvider.toForegroundColor(tlp.backgroundColor), rootItem.getForeground(0));
			assertEquals(TestLabelProvider.font, rootItem.getFont(0));
			assertEquals(tlp.image, rootItem.getImage(0));
			if (all) {
				checkItems(rootItem.getItems(), tlp, all, text);
			}
		}
	}

	/**
	 * Returns the TreeItem whose data is a ResourceWrapper with the specified
	 * name.
	 */
	protected TreeItem _findChild(String name, TreeItem[] items) {
		for (TreeItem item : items) {
			assertTrue("Child " + item + " should be an M1 or M2 resource", item.getData() instanceof ResourceWrapper);
			ResourceWrapper rw = (ResourceWrapper) item.getData();
			if (name.equals(rw.getResource().getName())) {
				return item;
			}
		}
		return null;
	}

	protected void _expand(TreeItem[] items) {
		for (TreeItem item : items) {
			_viewer.setExpandedState(item.getData(), true);
		}
	}

	/**
	 * Wait up to 60 seconds for a test label provider to be initialized.
	 *
	 * @param tlp the label provider.
	 */
	protected void waitForInitialization(TestLabelProvider tlp) {

		for (int i = 0; i < 1200; i++) {
			TreeItem[] rootItems = _viewer.getTree().getItems();

			if (tlp != null && rootItems[0].getBackground(0).equals(tlp.backgroundColor)) {
				System.out.println("The label provider '" + tlp.getClass().getCanonicalName()
						+ "' was initialized after "
						+ (i * 50) + " ms");
				return;
			}

			DisplayHelper.sleep(50);
		}

		fail("The label provider was not initialized.");
	}

}
