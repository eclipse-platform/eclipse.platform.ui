/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.menus;

import java.lang.reflect.Field;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;

/**
 * @since 3.3
 * 
 */
public class MenuPopulationTest extends MenuTestCase {
	private static final String ICONS_ANYTHING_GIF = "/anything.gif)";
	private static final String ICONS_BINARY_GIF = "/binary_co.gif)";
	private static final String ICONS_MOCK_GIF = "/mockeditorpart1.gif)";
	private static final String ICONS_VIEW_GIF = "/view.gif)";

	private static final String FIELD_ICON = "icon";
	public static final String ID_DEFAULT = "org.eclipse.ui.tests.menus.iconsDefault";
	public static final String ID_ALL = "org.eclipse.ui.tests.menus.iconsAll";
	public static final String ID_TOOLBAR = "org.eclipse.ui.tests.menus.iconsToolbarOnly";
	private AbstractContributionFactory afterOne;
	private AbstractContributionFactory beforeOne;
	private AbstractContributionFactory endofOne;
	private CommandContributionItem usefulContribution;

	/**
	 * @param testName
	 */
	public MenuPopulationTest(String testName) {
		super(testName);
	}

	public void testViewPopulation() throws Exception {
		MenuManager manager = new MenuManager(null, TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);
		IContributionItem[] items = manager.getItems();
		IContributionItem itemX1 = null;
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if ("MenuTest.ItemX1".equals(item.getId())) {
				itemX1 = item;
			}
		}

		assertFalse(itemX1.isVisible());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);

		assertTrue(itemX1.isVisible());

		contextService.deactivateContext(activeContext);

		assertFalse(itemX1.isVisible());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);

		assertTrue(itemX1.isVisible());

		menuService.releaseContributions(manager);
		manager.dispose();
	}

	public void testMenuIcons() throws Exception {
		Field iconField = CommandContributionItem.class
				.getDeclaredField(FIELD_ICON);
		iconField.setAccessible(true);

		MenuManager manager = new MenuManager(null, TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);

		IContributionItem ici = manager.find(ID_DEFAULT);
		assertTrue(ici instanceof CommandContributionItem);
		CommandContributionItem cmd = (CommandContributionItem) ici;

		ImageDescriptor icon = (ImageDescriptor) iconField.get(cmd);
		assertNotNull(icon);
		String iconString = icon.toString();
		assertEquals(ICONS_ANYTHING_GIF, iconString.substring(iconString
				.lastIndexOf('/')));

		ici = manager.find(ID_ALL);
		assertTrue(ici instanceof CommandContributionItem);
		cmd = (CommandContributionItem) ici;
		icon = (ImageDescriptor) iconField.get(cmd);
		assertNotNull(icon);
		iconString = icon.toString();
		assertEquals(ICONS_BINARY_GIF, iconString.substring(iconString
				.lastIndexOf('/')));

		ici = manager.find(ID_TOOLBAR);
		assertTrue(ici instanceof CommandContributionItem);
		cmd = (CommandContributionItem) ici;
		icon = (ImageDescriptor) iconField.get(cmd);
		assertNull(icon);

		manager.dispose();
	}

	public void testToolBarItems() throws Exception {
		Field iconField = CommandContributionItem.class
				.getDeclaredField(FIELD_ICON);
		iconField.setAccessible(true);

		ToolBarManager manager = new ToolBarManager();
		menuService.populateContributionManager(manager, "toolbar:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);

		IContributionItem ici = manager.find(ID_DEFAULT);
		assertTrue(ici instanceof CommandContributionItem);
		CommandContributionItem cmd = (CommandContributionItem) ici;

		ImageDescriptor icon = (ImageDescriptor) iconField.get(cmd);
		assertNotNull(icon);
		String iconString = icon.toString();
		assertEquals(ICONS_ANYTHING_GIF, iconString.substring(iconString
				.lastIndexOf('/')));

		ici = manager.find(ID_ALL);
		assertTrue(ici instanceof CommandContributionItem);
		cmd = (CommandContributionItem) ici;
		icon = (ImageDescriptor) iconField.get(cmd);
		assertNotNull(icon);
		iconString = icon.toString();
		assertEquals(ICONS_MOCK_GIF, iconString.substring(iconString
				.lastIndexOf('/')));

		ici = manager.find(ID_TOOLBAR);
		assertTrue(ici instanceof CommandContributionItem);
		cmd = (CommandContributionItem) ici;
		icon = (ImageDescriptor) iconField.get(cmd);
		assertNotNull(icon);
		iconString = icon.toString();
		assertEquals(ICONS_VIEW_GIF, iconString.substring(iconString
				.lastIndexOf('/')));

		manager.dispose();
	}

	public void testFactoryScopePopulation() throws Exception {
		AbstractContributionFactory factory = new AbstractContributionFactory(
				"menu:the.population.menu?after=additions",
				"org.eclipse.ui.tests") {

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
		IMenuService service = (IMenuService) view.getSite().getService(
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

	public void testAfterQueryInvalid() throws Exception {
		MenuManager manager = new MenuManager();
		menuService.populateContributionManager(manager, "menu:after.menu");
		assertEquals(0, manager.getSize());
	}

	public void testAfterQueryOneGroup() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("after.one"));
		assertEquals(1, manager.getSize());
		menuService.populateContributionManager(manager, "menu:after.menu");
		assertEquals(2, manager.getSize());
		assertEquals("after.insert", manager.getItems()[1].getId());
	}

	public void testAfterQueryTwoGroups() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("after.one"));
		manager.add(new GroupMarker("after.two"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:after.menu");
		assertEquals(3, manager.getSize());
		assertEquals("after.insert", manager.getItems()[1].getId());
	}

	public void testBeforeQueryInvalid() throws Exception {
		MenuManager manager = new MenuManager();
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(0, manager.getSize());
	}

	public void testBeforeQueryOneGroup() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("before.one"));
		assertEquals(1, manager.getSize());
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(2, manager.getSize());
		assertEquals("before.insert", manager.getItems()[0].getId());
	}

	public void testBeforeQueryTwoGroups() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("before.one"));
		manager.add(new GroupMarker("before.two"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(3, manager.getSize());
		assertEquals("before.insert", manager.getItems()[0].getId());
	}

	public void testBeforeQueryTwoGroups2() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("before.two"));
		manager.add(new GroupMarker("before.one"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:before.menu");
		assertEquals(3, manager.getSize());
		assertEquals("before.insert", manager.getItems()[1].getId());
	}

	public void testEndofQueryInvalid() throws Exception {
		MenuManager manager = new MenuManager();
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(0, manager.getSize());
	}

	public void testEndofQueryOneGroup() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.one"));
		assertEquals(1, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(2, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[1].getId());
	}

	public void testEndofQueryTwoGroups() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.one"));
		manager.add(new GroupMarker("endof.two"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(3, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[1].getId());
	}

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

	public void testEndofQueryTwoGroups3() throws Exception {
		MenuManager manager = new MenuManager();
		manager.add(new GroupMarker("endof.two"));
		manager.add(new GroupMarker("endof.one"));
		assertEquals(2, manager.getSize());
		menuService.populateContributionManager(manager, "menu:endof.menu");
		assertEquals(3, manager.getSize());
		assertEquals("endof.insert", manager.getItems()[2].getId());
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.menus.MenuTestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		afterOne = new AbstractContributionFactory(
				"menu:after.menu?after=after.one", "org.eclipse.ui.tests") {
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				additions.addContributionItem(new GroupMarker("after.insert"),
						null);
			}
		};
		menuService.addContributionFactory(afterOne);

		beforeOne = new AbstractContributionFactory(
				"menu:before.menu?before=before.one", "org.eclipse.ui.tests") {
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				additions.addContributionItem(new GroupMarker("before.insert"),
						null);
			}
		};
		menuService.addContributionFactory(beforeOne);

		endofOne = new AbstractContributionFactory(
				"menu:endof.menu?endof=endof.one", "org.eclipse.ui.tests") {
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.menus.MenuTestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		menuService.removeContributionFactory(afterOne);
		menuService.removeContributionFactory(beforeOne);
		menuService.removeContributionFactory(endofOne);
		usefulContribution.dispose();
		usefulContribution = null;
		super.doTearDown();
	}
}
