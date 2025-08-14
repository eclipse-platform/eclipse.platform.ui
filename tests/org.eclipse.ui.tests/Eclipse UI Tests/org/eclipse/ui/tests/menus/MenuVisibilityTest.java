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

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.AbstractEnabledHandler;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;
import org.eclipse.ui.tests.commands.ActiveContextExpression;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class MenuVisibilityTest extends UITestCase {

	private static final String EXTENSION_ID = "org.eclipse.ui.tests.menusX1";
	private static final String LOCATION = "menu:foo";
	private static final String COMMAND_ID = "org.eclipse.ui.tests.commandEnabledVisibility";

	public MenuVisibilityTest() {
		super(MenuVisibilityTest.class.getSimpleName());
	}

	private IContextService contextService;
	private IMenuService menuService;
	private IWorkbenchWindow window;
	private IContextActivation activeContext;

	@Test
	public void testBasicContribution() throws Exception {

		IAction a = new Action() {
			@Override
			public void run() {
				System.out.println("Hello action");
			}
		};
		final MenuManager manager = new MenuManager();
		final ActionContributionItem item = new ActionContributionItem(a);
		final Expression activeContextExpr = new ActiveContextExpression(
				MenuContributionHarness.CONTEXT_TEST1_ID,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		AbstractContributionFactory factory = new AbstractContributionFactory(
				LOCATION, TestPlugin.PLUGIN_ID) {
			@Override
			public void createContributionItems(IServiceLocator menuService,
					IContributionRoot additions) {
				additions.addContributionItem(item, activeContextExpr);
			}
		};

		menuService.addContributionFactory(factory);
		menuService.populateContributionManager(manager, LOCATION);

		Shell shell = window.getShell();

		// Test the initial menu creation
		final Menu menuBar = manager.createContextMenu(shell);
		Event e = new Event();
		e.type = SWT.Show;
		e.widget = menuBar;
		menuBar.notifyListeners(SWT.Show, e);

		assertFalse("starting state", item.isVisible());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);
		menuBar.notifyListeners(SWT.Show, e);

		assertTrue("active context", item.isVisible());

		contextService.deactivateContext(activeContext);
		activeContext = null;
		menuBar.notifyListeners(SWT.Show, e);

		assertFalse("after deactivation", item.isVisible());

		menuService.releaseContributions(manager);
		menuService.removeContributionFactory(factory);
		manager.dispose();
	}

	@Test
	public void testExtensionContributionExpression() throws Exception {
		IAction a = new Action() {
			@Override
			public void run() {
				System.out.println("Hello action");
			}
		};
		final MenuManager manager = new MenuManager();
		final ActionContributionItem aci = new ActionContributionItem(a);

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint menusExtension = reg
				.getExtensionPoint("org.eclipse.ui.menus");
		IExtension extension = menusExtension.getExtension(EXTENSION_ID);

		IConfigurationElement[] mas = extension.getConfigurationElements();
		final Expression activeContextExpr[] = new Expression[1];
		for (IConfigurationElement ma : mas) {
			IConfigurationElement[] items = ma.getChildren();
			for (IConfigurationElement item : items) {
				String id = item.getAttribute("id");
				if (id != null
						&& id.equals("org.eclipse.ui.tests.menus.itemX1")) {
					IConfigurationElement visibleWhenElement = item
							.getChildren("visibleWhen")[0];
					activeContextExpr[0] = ExpressionConverter.getDefault()
							.perform(visibleWhenElement.getChildren()[0]);
				}
			}
		}
		assertNotNull("Failed to find expression", activeContextExpr[0]);
		AbstractContributionFactory factory = new AbstractContributionFactory(
				LOCATION, TestPlugin.PLUGIN_ID) {
			@Override
			public void createContributionItems(IServiceLocator menuService,
					IContributionRoot additions) {
				additions.addContributionItem(aci, activeContextExpr[0]);
			}
		};

		menuService.addContributionFactory(factory);
		menuService.populateContributionManager(manager, LOCATION);

		assertFalse("starting state", aci.isVisible());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);
		final Menu menu = manager.createContextMenu(window.getShell());
		menu.notifyListeners(SWT.Show, new Event());
		assertTrue("active context", aci.isVisible());
		menu.notifyListeners(SWT.Hide, new Event());


		contextService.deactivateContext(activeContext);
		activeContext = null;

		menu.notifyListeners(SWT.Show, new Event());
		assertFalse("after deactivation", aci.isVisible());
		menu.notifyListeners(SWT.Hide, new Event());

		menuService.releaseContributions(manager);
		menuService.removeContributionFactory(factory);
		manager.dispose();
	}

	private static class TestEnabled extends AbstractEnabledHandler {
		@Override
		public Object execute(ExecutionEvent event) {
			System.out.println("go");
			return null;
		}

		@Override
		public void setEnabled(boolean isEnabled) {
			super.setEnabled(isEnabled);
		}
	}

	@Test
	public void testVisibilityTracksEnablement() throws Exception {
		final MenuManager manager = new MenuManager();
		final CommandContributionItemParameter parm = new CommandContributionItemParameter(
				window, null, COMMAND_ID, Collections.EMPTY_MAP, null, null,
				null, null, null, null, CommandContributionItem.STYLE_PUSH,
				null, true);
		final CommandContributionItem item = new CommandContributionItem(parm);

		AbstractContributionFactory factory = new AbstractContributionFactory(
				LOCATION, TestPlugin.PLUGIN_ID) {
			@Override
			public void createContributionItems(IServiceLocator menuService,
					IContributionRoot additions) {
				additions.addContributionItem(item, null);
			}
		};

		menuService.addContributionFactory(factory);
		menuService.populateContributionManager(manager, LOCATION);

		assertFalse(item.isEnabled());
		assertFalse("starting state", item.isVisible());

		IHandlerService handlers = window
				.getService(IHandlerService.class);
		TestEnabled handler = new TestEnabled();
		IHandlerActivation activateHandler = handlers.activateHandler(
				COMMAND_ID, handler);

		assertTrue(handler.isEnabled());
		assertTrue(item.isEnabled());
		assertTrue("activated handler", item.isVisible());

		handler.setEnabled(false);

		assertFalse("set enabled == false", item.isVisible());

		handler.setEnabled(true);

		assertTrue("set enabled == true", item.isVisible());

		handlers.deactivateHandler(activateHandler);

		assertFalse("deactivate handler", item.isVisible());

		menuService.releaseContributions(manager);
		menuService.removeContributionFactory(factory);
		manager.dispose();
	}

	@Test
	public void testMenuManagerEnablement_Check_Bug_552659_Regression() {
		IContributionItem item = new ContributionItem() {
		};
		final MenuManager parentMenuManager = new MenuManager("parentMenu");
		final MenuManager subMenuManager = new MenuManager("subMenu");
		AbstractContributionFactory factory = new AbstractContributionFactory(LOCATION, TestPlugin.PLUGIN_ID) {
			@Override
			public void createContributionItems(IServiceLocator menuService, IContributionRoot additions) {
				additions.addContributionItem(item, null);
			}
		};
		parentMenuManager.add(subMenuManager);

		menuService.addContributionFactory(factory);
		menuService.populateContributionManager(subMenuManager, LOCATION);

		Shell shell = window.getShell();

		// Create the parent menu
		final Menu menuBar = parentMenuManager.createContextMenu(shell);
		Event e = new Event();
		e.type = SWT.Show;
		e.widget = menuBar;

		// Show parent menu
		menuBar.notifyListeners(SWT.Show, e);

		// update sub menu manager before actually showing the sub menu
		subMenuManager.update();

		assertEquals(1, parentMenuManager.getMenu().getItemCount());
		MenuItem subMenuItem = parentMenuManager.getMenu().getItem(0);
		assertEquals(subMenuManager.getMenu(), subMenuItem.getMenu());
		assertTrue(subMenuItem.isEnabled());

		menuService.releaseContributions(subMenuManager);
		menuService.removeContributionFactory(factory);
		subMenuManager.dispose();
		parentMenuManager.dispose();
	}

	@Test
	public void testMenuManagerVisibilityAndEnablement() {
		IContributionItem item = new ContributionItem() {
		};
		final MenuManager parentMenuManager = new MenuManager("parentMenu");
		final MenuManager subMenuManager = new MenuManager("subMenu");
		AbstractContributionFactory factory = new AbstractContributionFactory(LOCATION, TestPlugin.PLUGIN_ID) {
			@Override
			public void createContributionItems(IServiceLocator menuService, IContributionRoot additions) {
				additions.addContributionItem(item, null);
			}
		};
		parentMenuManager.add(subMenuManager);

		menuService.addContributionFactory(factory);
		menuService.populateContributionManager(subMenuManager, LOCATION);

		Shell shell = window.getShell();

		// Create the parent menu
		final Menu menuBar = parentMenuManager.createContextMenu(shell);
		Event e = new Event();
		e.type = SWT.Show;
		e.widget = menuBar;

		{
			// submenu contains visible item + submenu manager default visibility
			item.setVisible(true);
			subMenuManager.setVisible(true);
			subMenuManager.setRemoveAllWhenShown(false);

			parentMenuManager.updateAll(true);
			menuBar.notifyListeners(SWT.Show, e);

			assertTrue(subMenuManager.isVisible());
			assertTrue(subMenuManager.isEnabled());
			assertEquals(1, parentMenuManager.getMenu().getItemCount());
			MenuItem subMenuItem = parentMenuManager.getMenu().getItem(0);
			assertEquals(subMenuManager.getMenu(), subMenuItem.getMenu());
			assertTrue(subMenuItem.isEnabled());
		}

		{
			// submenu contains no visible item + submenu manager default visibility
			item.setVisible(false);
			subMenuManager.setVisible(true);
			subMenuManager.setRemoveAllWhenShown(false);

			parentMenuManager.updateAll(true);
			menuBar.notifyListeners(SWT.Show, e);

			assertFalse(subMenuManager.isVisible());
			assertTrue(subMenuManager.isEnabled()); // always true
			assertEquals(0, parentMenuManager.getMenu().getItemCount());
		}

		{
			// submenu contains visible item + submenu manager default visibility
			item.setVisible(true);
			subMenuManager.setVisible(true);
			subMenuManager.setRemoveAllWhenShown(false);

			parentMenuManager.updateAll(true);
			menuBar.notifyListeners(SWT.Show, e);

			assertTrue(subMenuManager.isVisible());
			assertTrue(subMenuManager.isEnabled());
			assertEquals(1, parentMenuManager.getMenu().getItemCount());
			MenuItem subMenuItem = parentMenuManager.getMenu().getItem(0);
			assertEquals(subMenuManager.getMenu(), subMenuItem.getMenu());
			assertTrue(subMenuItem.isEnabled());
		}

		{
			// submenu contains visible item but submenu forced invisible
			item.setVisible(true);
			subMenuManager.setVisible(false);
			subMenuManager.setRemoveAllWhenShown(false);

			parentMenuManager.updateAll(true);
			menuBar.notifyListeners(SWT.Show, e);

			assertFalse(subMenuManager.isVisible());
			assertTrue(subMenuManager.isEnabled()); // always true
			assertEquals(0, parentMenuManager.getMenu().getItemCount());
		}

		{
			// submenu contains no visible item but remove-all-when-shown is enabled
			item.setVisible(false);
			subMenuManager.setVisible(true);
			subMenuManager.setRemoveAllWhenShown(true);

			parentMenuManager.updateAll(true);
			menuBar.notifyListeners(SWT.Show, e);

			assertTrue(subMenuManager.isVisible());
			assertTrue(subMenuManager.isEnabled()); // always true
			assertEquals(1, parentMenuManager.getMenu().getItemCount());
			MenuItem subMenuItem = parentMenuManager.getMenu().getItem(0);
			assertEquals(subMenuManager.getMenu(), subMenuItem.getMenu());
			assertTrue(subMenuItem.isEnabled());
		}

		menuService.releaseContributions(subMenuManager);
		menuService.removeContributionFactory(factory);
		subMenuManager.dispose();
		parentMenuManager.dispose();
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		window = openTestWindow();
		menuService = window.getService(IMenuService.class);
		contextService = window
				.getService(IContextService.class);
		Context context1 = contextService
				.getContext(MenuContributionHarness.CONTEXT_TEST1_ID);
		if (!context1.isDefined()) {
			context1.define("Menu Test 1", "Menu test 1",
					IContextService.CONTEXT_ID_DIALOG_AND_WINDOW);
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		if (activeContext != null) {
			contextService.deactivateContext(activeContext);
			activeContext = null;
		}
		menuService = null;
		contextService = null;
		window = null;

		super.doTearDown();
	}
}
