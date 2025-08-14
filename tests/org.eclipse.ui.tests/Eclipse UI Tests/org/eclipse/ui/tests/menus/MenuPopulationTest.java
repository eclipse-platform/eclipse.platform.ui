/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
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

package org.eclipse.ui.tests.menus;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.api.workbenchpart.EmptyView;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class MenuPopulationTest extends MenuTestCase {
	private static final String ICONS_ANYTHING_GIF = "/anything.gif";
	private static final String ICONS_BINARY_GIF = "/binary_co.gif";
	private static final String ICONS_MOCK_GIF = "/mockeditorpart1.gif";
	private static final String ICONS_VIEW_GIF = "/view.gif";

	private static final String FIELD_ICON = "icon";
	public static final String ID_DEFAULT = "org.eclipse.ui.tests.menus.iconsDefault";
	public static final String ID_ALL = "org.eclipse.ui.tests.menus.iconsAll";
	public static final String ID_TOOLBAR = "org.eclipse.ui.tests.menus.iconsToolbarOnly";
	private AbstractContributionFactory afterOne;
	private AbstractContributionFactory beforeOne;
	private AbstractContributionFactory endofOne;
	private CommandContributionItem usefulContribution;

	private static final String ITEM_ID = "my.id";
	private static final String MENU_LOCATION = "menu:local.menu.test";
	private Field iconField;

	public MenuPopulationTest() {
		super(MenuPopulationTest.class.getSimpleName());
	}

	@Test
	public void testMenuServicePopupContribution() throws Exception {

		PopupMenuExtender popupMenuExtender = null;
		try {

			window.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW);

			processEventsUntil(() -> window.getActivePage().getActivePart() != null, 10000);


			IWorkbenchPart problemsView = window.getActivePage().getActivePart();
			assertNotNull(problemsView);

			final boolean[] errorLogged = addLogger();

			MenuManager manager = new MenuManager();
			manager.setRemoveAllWhenShown(true);
			Menu contextMenu = manager.createContextMenu(problemsView.getSite().getShell());

			popupMenuExtender = new PopupMenuExtender(
					IPageLayout.ID_PROBLEM_VIEW, manager, problemsView
							.getSite().getSelectionProvider(), problemsView,
					((PartSite) problemsView.getSite()).getContext(), false);
			popupMenuExtender.addMenuId(MarkerSupportRegistry.MARKERS_ID);

			contextMenu.notifyListeners(SWT.Show, new Event());
			contextMenu.notifyListeners(SWT.Hide, new Event());

			contextMenu.notifyListeners(SWT.Show, new Event());
			contextMenu.notifyListeners(SWT.Hide, new Event());

			assertFalse(errorLogged[0]);

		}finally {
			if(popupMenuExtender != null) {
				popupMenuExtender.dispose();
			}
		}
	}

	@Test
	@Ignore("See Bugs 411765 and 452203")
	public void XXXtestMenuServiceContribution() {
		IMenuService ms = PlatformUI.getWorkbench().getService(IMenuService.class);
		AbstractContributionFactory factory = new AbstractContributionFactory("menu:org.eclipse.ui.main.menu?after=file", "205747") {
			@Override
			public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
				MenuManager manager = new MenuManager("&LoFile", "lofile");
				CommandContributionItem cci = new CommandContributionItem(new CommandContributionItemParameter(serviceLocator, "my.about",
						IWorkbenchCommandConstants.HELP_ABOUT, CommandContributionItem.STYLE_PUSH));
				manager.add(cci);
				additions.addContributionItem(manager, null);
			}
		};

		final boolean[] errorLogged = addLogger();

		assertContributions(false);
		ms.addContributionFactory(factory);
		assertFalse(errorLogged[0]);

		assertContributions(true);
		ms.removeContributionFactory(factory);
		assertContributions(false);

	}

	private boolean[] addLogger() {
		final boolean []errorLogged = new boolean[] {false};
		Platform.addLogListener((status, plugin) -> {
			if("org.eclipse.ui.workbench".equals(status.getPlugin())
					&& status.getSeverity() == IStatus.ERROR
					&& status.getException() instanceof IndexOutOfBoundsException) {
				errorLogged[0] = true;
			}
		});
		return errorLogged;
	}


	private void assertContributions(boolean added) {

		MenuManager menuManager = ((WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow()).getMenuManager();
		IContributionItem[] items = menuManager.getItems();
		boolean found = false;
		for (IContributionItem item : items) {
			if (item instanceof MenuManager aManager) {
				if(aManager.getId().equals("lofile")) {
					found = true;
					break;
				}
			}
		}
		assertEquals(found, added);
	}


	@Test
	public void test_1_1_RelationshipInMenuManagerRenderer_Bug552361() throws Exception {

		PopupMenuExtender popupMenuExtender1 = null;
		PopupMenuExtender popupMenuExtender2 = null;
		try {

			window.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW);

			processEventsUntil(() -> window.getActivePage().getActivePart() != null, 10000);

			IWorkbenchPart problemsView = window.getActivePage().getActivePart();
			assertNotNull(problemsView);

			String testId = "org.eclipse.ui.tests.menus.bug552361";

			MPart modelPart = problemsView.getSite().getService(MPart.class);
			IRendererFactory factory = modelPart.getContext().get(IRendererFactory.class);
			MenuManagerRenderer renderer = (MenuManagerRenderer) factory
					.getRenderer(MenuFactoryImpl.eINSTANCE.createPopupMenu(), null);

			MenuManager manager1 = new MenuManager();
			MenuManager manager2 = new MenuManager();

			MMenu menuModel1 = renderer.getMenuModel(manager1);
			MMenu menuModel2 = renderer.getMenuModel(manager2);

			assertNull(menuModel1);
			assertNull(menuModel2);

			popupMenuExtender1 = new PopupMenuExtender(testId, manager1, null, problemsView, null, false);
			popupMenuExtender2 = new PopupMenuExtender(testId, manager2, null, problemsView, null, false);

			menuModel1 = renderer.getMenuModel(manager1);
			menuModel2 = renderer.getMenuModel(manager2);

			assertNotNull(menuModel1);
			assertNotNull(menuModel2);
			assertSame(manager1, renderer.getManager(menuModel1));
			assertSame(manager2, renderer.getManager(menuModel2));

		} finally {
			if (popupMenuExtender1 != null) {
				popupMenuExtender1.dispose();
			}
			if (popupMenuExtender2 != null) {
				popupMenuExtender2.dispose();
			}
		}
	}

	@Test
	public void testViewPopulation() throws Exception {
		MenuManager manager = new MenuManager(null, TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);
		IContributionItem[] items = manager.getItems();
		IContributionItem itemX1 = null;
		for (IContributionItem item : items) {
			if ("MenuTest.ItemX1".equals(item.getId())) {
				itemX1 = item;
			}
		}

		assertFalse(itemX1.isVisible());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);

		final Menu menu = manager.createContextMenu(window.getShell());
		menu.notifyListeners(SWT.Show, new Event());
		assertTrue(itemX1.isVisible());
		menu.notifyListeners(SWT.Hide, new Event());

		contextService.deactivateContext(activeContext);
		menu.notifyListeners(SWT.Show, new Event());

		assertFalse(itemX1.isVisible());
		menu.notifyListeners(SWT.Hide, new Event());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);
		menu.notifyListeners(SWT.Show, new Event());

		assertTrue(itemX1.isVisible());
		menu.notifyListeners(SWT.Hide, new Event());

		menuService.releaseContributions(manager);
		manager.dispose();
	}

	@Test
	public void testMenuIcons() throws Exception {

		MenuManager manager = new MenuManager(null, TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);

		IContributionItem ici = manager.find(ID_DEFAULT);
		assertTrue(ici instanceof CommandContributionItem || ici instanceof HandledContributionItem);
		if (ici instanceof CommandContributionItem cmd) {
			assertIcon(cmd, ICONS_ANYTHING_GIF);
		} else if (ici instanceof HandledContributionItem) {
			assertIcon((HandledContributionItem)ici, ICONS_ANYTHING_GIF);
		}

		ici = manager.find(ID_ALL);
		assertTrue(ici instanceof CommandContributionItem || ici instanceof HandledContributionItem);
		if (ici instanceof CommandContributionItem cci) {
			assertIcon(cci, ICONS_BINARY_GIF);
		} else if (ici instanceof HandledContributionItem hci) {
			assertIcon(hci, ICONS_BINARY_GIF);
		}


		ici = manager.find(ID_TOOLBAR);
		assertTrue(ici instanceof CommandContributionItem || ici instanceof HandledContributionItem);
		if (ici instanceof CommandContributionItem cmd) {
			ImageDescriptor icon = (ImageDescriptor) iconField.get(cmd);
			assertNull(icon);
		} else if (ici instanceof HandledContributionItem hci) {
			final MHandledItem model = hci.getModel();
			String iconString = model.getIconURI();
			assertTrue(iconString, iconString == null || iconString.isEmpty());
		}

		manager.dispose();
	}

	@Test
	public void testToolBarItems() throws Exception {
		ToolBarManager manager = new ToolBarManager();
		menuService.populateContributionManager(manager, "toolbar:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);

		IContributionItem ici = manager.find(ID_DEFAULT);
		assertTrue(ici instanceof CommandContributionItem || ici instanceof HandledContributionItem);
		if (ici instanceof CommandContributionItem cmd) {
			assertIcon(cmd, ICONS_ANYTHING_GIF);
		} else if (ici instanceof HandledContributionItem hci) {
			assertIcon(hci, ICONS_ANYTHING_GIF);
		}

		ici = manager.find(ID_ALL);
		assertTrue(ici instanceof CommandContributionItem || ici instanceof HandledContributionItem);
		if (ici instanceof CommandContributionItem cci) {
			assertIcon(cci, ICONS_MOCK_GIF);
		} else if (ici instanceof HandledContributionItem hci) {
			assertIcon(hci, ICONS_MOCK_GIF);
		}

		ici = manager.find(ID_TOOLBAR);
		assertTrue(ici instanceof CommandContributionItem || ici instanceof HandledContributionItem);
		if (ici instanceof CommandContributionItem cci) {
			assertIcon(cci, ICONS_VIEW_GIF);
		} else if (ici instanceof HandledContributionItem hci) {
			assertIcon(hci, ICONS_VIEW_GIF);
		}

		manager.dispose();
	}

	static class MyFactory extends AbstractContributionFactory {
		public ContributionItem localContribution;

		public MyFactory() {
			super(MENU_LOCATION + "?after=additions", "org.eclipse.ui.tests");
			localContribution = new ContributionItem(ITEM_ID) {
			};
		}

		@Override
		public void createContributionItems(IServiceLocator serviceLocator,
				IContributionRoot additions) {
			additions.addContributionItem(localContribution, null);
		}
	}

	@Test
	public void testFactoryAddition() throws Exception {
		MyFactory factory = new MyFactory();
		MenuManager manager = new MenuManager(null);
		try {
			menuService.addContributionFactory(factory);

			menuService.populateContributionManager(manager, MENU_LOCATION);
			IContributionItem item = manager.find(ITEM_ID);
			assertNotNull(item);
			assertEquals(factory.localContribution, item);

		} finally {
			menuService.releaseContributions(manager);
			manager.dispose();
			menuService.removeContributionFactory(factory);
		}
	}

	@Test
	public void testFactoryRemove() throws Exception {
		MyFactory factory = new MyFactory();
		MenuManager manager = new MenuManager(null);
		try {
			menuService.addContributionFactory(factory);

			menuService.removeContributionFactory(factory);

			menuService.populateContributionManager(manager, MENU_LOCATION);
			IContributionItem item = manager.find(ITEM_ID);
			assertNull(item);
		} finally {
			menuService.releaseContributions(manager);
			manager.dispose();
			menuService.removeContributionFactory(factory);
		}
	}

	@Test
	@Ignore("See Bugs 411765 and 452203")
	public void XXXtestDynamicFactoryAddition() throws Exception {
		MyFactory factory = new MyFactory();

		MenuManager manager = new MenuManager(null);
		menuService.populateContributionManager(manager, MENU_LOCATION);
		IContributionItem item = manager.find(ITEM_ID);
		assertNull(item);

		try {
			menuService.addContributionFactory(factory);
			item = manager.find(ITEM_ID);
			assertNotNull(item);
			assertEquals(factory.localContribution, item);

		} finally {
			menuService.releaseContributions(manager);
			manager.dispose();
			menuService.removeContributionFactory(factory);
		}
	}

	@Test
	@Ignore("See Bugs 411765 and 452203")
	public void XXXtestDynamicFactoryRemove() throws Exception {
		MyFactory factory = new MyFactory();
		MenuManager manager = new MenuManager(null);
		try {
			menuService.addContributionFactory(factory);

			menuService.populateContributionManager(manager, MENU_LOCATION);
			IContributionItem item = manager.find(ITEM_ID);
			assertNotNull(item);
			assertEquals(factory.localContribution, item);

			menuService.removeContributionFactory(factory);

			item = manager.find(ITEM_ID);
			assertNull(item);
		} finally {
			menuService.releaseContributions(manager);
			manager.dispose();
			menuService.removeContributionFactory(factory);
		}
	}

	@Test
	@Ignore("See Bugs 411765 and 452203")
	public void XXXtestFactoryScopePopulation() throws Exception {
		AbstractContributionFactory factory = new AbstractContributionFactory(
				"menu:the.population.menu?after=additions",
				"org.eclipse.ui.tests") {

			@Override
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				final MenuManager manager = new MenuManager("menu.id");
				manager.add(new Action("action.id") {
				});
				additions.addContributionItem(manager, null);
			}

		};
		MenuManager testManager = new MenuManager();
		IViewPart view = window.getActivePage()
				.showView(IPageLayout.ID_OUTLINE);
		assertNotNull(view);
		IMenuService service = view.getSite().getService(
				IMenuService.class);
		service.populateContributionManager(testManager,
				"menu:the.population.menu");
		assertEquals(0, testManager.getSize());
		service.addContributionFactory(factory);
		assertEquals(1, testManager.getSize());
		window.getActivePage().hideView(view);
		processEvents();
		assertEquals(0, testManager.getSize());
	}

	@Test
	public void testAfterQueryInvalid() throws Exception {
		MenuManager manager = new MenuManager();
		menuService.populateContributionManager(manager, "menu:after.menu");
		assertEquals(0, manager.getSize());
	}

	@Test
	public void testAfterQueryOneGroup() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("after.one"));
		assertEquals(1, manager.getSize());
		menuService.populateContributionManager(manager, "menu:after.menu");
		assertEquals(2, manager.getSize());
		assertEquals("after.insert", manager.getItems()[1].getId());
	}

	@Test
	public void testAfterQueryTwoGroups() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("after.one"));
		manager.add(new GroupMarker("after.two"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:after.menu");
		assertEquals(3, manager.getSize());
		assertEquals("after.insert", manager.getItems()[1].getId());
	}

	@Test
	public void testBeforeQueryInvalid() throws Exception {
		MenuManager manager = new MenuManager();
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(0, manager.getSize());
	}

	@Test
	public void testBeforeQueryOneGroup() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("before.one"));
		assertEquals(1, manager.getSize());
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(2, manager.getSize());
		assertEquals("before.insert", manager.getItems()[0].getId());
	}

	@Test
	public void testBeforeQueryTwoGroups() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("before.one"));
		manager.add(new GroupMarker("before.two"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(3, manager.getSize());
		assertEquals("before.insert", manager.getItems()[0].getId());
	}

	@Test
	public void testBeforeQueryTwoGroups2() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("before.two"));
		manager.add(new GroupMarker("before.one"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(3, manager.getSize());
		assertEquals("before.insert", manager.getItems()[1].getId());
	}

	@Test
	public void testEndofQueryInvalid() throws Exception {
		MenuManager manager = new MenuManager();
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(0, manager.getSize());
	}

	@Test
	public void testEndofQueryOneGroup() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.one"));
		assertEquals(1, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(2, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[1].getId());
	}

	@Test
	public void testEndofQueryTwoGroups() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.one"));
		manager.add(new GroupMarker("endof.two"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(3, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[1].getId());
	}

	@Test
	public void testEndofQueryTwoGroups2() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.one"));
		manager.add(usefulContribution);
		manager.add(new GroupMarker("endof.two"));
		assertEquals(3, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(4, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[2].getId());
	}

	@Test
	public void testEndofQueryTwoGroups3() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.two"));
		manager.add(new GroupMarker("endof.one"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(3, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[2].getId());
	}

	@Test
	public void testEndofQueryTwoGroups4() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.two"));
		manager.add(new GroupMarker("endof.one"));
		manager.add(usefulContribution);
		assertEquals(3, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(4, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[3].getId());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		afterOne = new AbstractContributionFactory(
				"menu:after.menu?after=after.one", "org.eclipse.ui.tests") {
			@Override
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				additions.addContributionItem(new GroupMarker("after.insert"),
						null);
			}
		};
		menuService.addContributionFactory(afterOne);

		beforeOne = new AbstractContributionFactory(
				"menu:before.menu?before=before.one", "org.eclipse.ui.tests") {
			@Override
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				additions.addContributionItem(new GroupMarker("before.insert"),
						null);
			}
		};
		menuService.addContributionFactory(beforeOne);

		endofOne = new AbstractContributionFactory(
				"menu:endof.menu?endof=endof.one", "org.eclipse.ui.tests") {
			@Override
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				additions.addContributionItem(new GroupMarker("endof.insert"),
						null);
			}
		};
		menuService.addContributionFactory(endofOne);
		usefulContribution = new CommandContributionItem(
				new CommandContributionItemParameter(window, null,
						IWorkbenchCommandConstants.HELP_ABOUT, 0));
		iconField = CommandContributionItem.class
				.getDeclaredField(FIELD_ICON);
		iconField.setAccessible(true);
	}

	@Override
	protected void doTearDown() throws Exception {
		menuService.removeContributionFactory(afterOne);
		menuService.removeContributionFactory(beforeOne);
		menuService.removeContributionFactory(endofOne);
		usefulContribution.dispose();
		usefulContribution = null;
		super.doTearDown();
	}

	@Test
	@Ignore("See Bug 544515")
	public void XXXtestPrivatePopup() throws Exception {

			PopupMenuExtender popupMenuExtender = null;
			MenuManager manager = null;
			Menu contextMenu = null;
			try {

			window.getActivePage().showView(EmptyView.ID);

				processEventsUntil(() -> window.getActivePage().getActivePart() != null, 10000);

				IWorkbenchPart activePart = window.getActivePage().getActivePart();
				assertNotNull(activePart);

				manager = new MenuManager();
				manager.setRemoveAllWhenShown(true);
			popupMenuExtender = new PopupMenuExtender(activePart.getSite()
					.getId(), manager, null, activePart,
					((PartSite) activePart.getSite()).getContext());

				Shell windowShell = window.getShell();
				contextMenu = manager.createContextMenu(windowShell);

				Event showEvent = new Event();
				showEvent.widget = contextMenu;
				showEvent.type = SWT.Show;

				contextMenu.notifyListeners(SWT.Show, showEvent);

				Event hideEvent = new Event();
				hideEvent.widget = contextMenu;
				hideEvent.type = SWT.Hide;

				contextMenu.notifyListeners(SWT.Hide, hideEvent);

				assertPrivatePopups(manager);

				manager.removeAll();
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

				contextMenu.notifyListeners(SWT.Show, showEvent);
				contextMenu.notifyListeners(SWT.Hide, hideEvent);

				assertPrivatePopups(manager);

			}finally {
				if(popupMenuExtender != null) {
					popupMenuExtender.dispose();
				}
				if (contextMenu!=null) {
					contextMenu.dispose();
				}
				if(manager != null) {
					manager.dispose();
				}
			}
		}

		private void assertPrivatePopups(final MenuManager manager) {
			boolean cmd1Found = false;
			boolean cmd2Found = false;
			boolean cmd3Found = false;
			IContributionItem[] items = manager.getItems();
			for (IContributionItem item : items) {
				if("org.eclipse.ui.tests.anypopup.command1".equals(item.getId())) {
					cmd1Found = true;
				}else if("org.eclipse.ui.tests.anypopup.command2".equals(item.getId())) {
					cmd2Found = true;
				}else if("org.eclipse.ui.tests.anypopup.command3".equals(item.getId())) {
					cmd3Found = true;
				}
			}

			boolean hasAdditions = manager.indexOf(IWorkbenchActionConstants.MB_ADDITIONS) != -1;
			assertTrue("no allPopups attribute for cmd1. Should show always", cmd1Found);
			assertTrue("allPopups = true for cmd2. Should always show", cmd2Found);
			assertTrue("allPopups = false for cmd3. Should show only if additions present", hasAdditions == cmd3Found); // allPopups = false. Should show only if additions is available
		}



	private void assertIcon(CommandContributionItem cmd, String targetIcon) throws IllegalArgumentException, IllegalAccessException {
		ImageDescriptor icon = (ImageDescriptor) iconField.get(cmd);
		assertNotNull(icon);
		String iconString = icon.toString();
		assertEquals(targetIcon+')', iconString.substring(iconString
				.lastIndexOf('/')));
	}
	private void assertIcon(HandledContributionItem item, String targetIcon) {
		final MHandledItem model = item.getModel();
		String iconString = model.getIconURI();
		assertEquals(targetIcon, iconString.substring(iconString
				.lastIndexOf('/')));
	}
}
