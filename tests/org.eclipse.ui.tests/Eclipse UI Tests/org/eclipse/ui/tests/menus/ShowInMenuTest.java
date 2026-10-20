package org.eclipse.ui.tests.menus;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.handlers.ShowInSystemExplorerHandler;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CloseTestWindowsExtension.class)
public class ShowInMenuTest {

	private static final String TEST_ITEM_ID = "org.eclipse.ui.tests.menus.showInMenuTestItem";

	private IWorkbenchWindow window;
	private IWorkbenchPage page;
	private List<IProject> projects;

	@BeforeEach
	public final void setUp() throws Exception {
		window = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		page = window.getActivePage();
		projects = List.of(FileUtil.createProject("a"), FileUtil.createProject("b"));
	}

	@AfterEach
	public final void tearDown() throws Exception {
		for (IProject project : projects) {
			FileUtil.deleteProject(project);
		}
	}

	@Test
	void testVisibleWhenEvaluation() throws CoreException {
		IViewPart viewPart = page.showView(IPageLayout.ID_PROJECT_EXPLORER);
		ISelectionProvider selectionProvider = viewPart.getSite().getSelectionProvider();
		IWorkbenchWindow workbenchWindow = EditorTestHelper.getActiveWorkbenchWindow();

		// Test item should be visible when selecting a single project
		selectionProvider.setSelection(new StructuredSelection(projects.get(0)));
		Menu menu = computeShowInMenu(workbenchWindow);

		assertNotNull(getContributionItemById(menu, TEST_ITEM_ID));

		// Test item should be hidden when selecting multiple projects
		selectionProvider.setSelection(new StructuredSelection(projects));
		menu = computeShowInMenu(workbenchWindow);

		assertNull(getContributionItemById(menu, TEST_ITEM_ID));
	}

	@Test
	void testVisibleWhenEvaluationForShowInSystemExplorer() throws CoreException {
		IViewPart viewPart = page.showView(IPageLayout.ID_PROJECT_EXPLORER);
		ISelectionProvider selectionProvider = viewPart.getSite().getSelectionProvider();
		IWorkbenchWindow workbenchWindow = EditorTestHelper.getActiveWorkbenchWindow();

		// "Show in System Explorer" should be visible when selecting a single project
		selectionProvider.setSelection(new StructuredSelection(projects.get(0)));
		Menu menu = computeShowInMenu(workbenchWindow);

		assertNotNull(getContributionItemById(menu, ShowInSystemExplorerHandler.ID));

		// "Show in System Explorer" should be hidden when selecting multiple projects
		selectionProvider.setSelection(new StructuredSelection(projects));
		menu = computeShowInMenu(workbenchWindow);

		assertNull(getContributionItemById(menu, ShowInSystemExplorerHandler.ID));
	}

	private static Menu computeShowInMenu(IWorkbenchWindow workbenchWindow) {
		IContributionItem contributionItem = ContributionItemFactory.VIEWS_SHOW_IN.create(workbenchWindow);
		Menu menu = new Menu(workbenchWindow.getShell());
		contributionItem.fill(menu, 0);
		return menu;
	}

	private static ContributionItem getContributionItemById(Menu menu, String id) {
		for (MenuItem item : menu.getItems()) {
			if (item.getData() instanceof ContributionItem contributionItem && contributionItem.getId().equals(id)) {
				return contributionItem;
			}
		}
		return null;
	}
}
