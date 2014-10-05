/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.menus;

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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
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

/**
 * @since 3.3
 * 
 */
public class MenuVisibilityTest extends UITestCase {

	private static final String EXTENSION_ID = "org.eclipse.ui.tests.menusX1";
	private static final String LOCATION = "menu:foo";
	private static final String COMMAND_ID = "org.eclipse.ui.tests.commandEnabledVisibility";

	/**
	 * @param testName
	 */
	public MenuVisibilityTest(String testName) {
		super(testName);
	}

	private IContextService contextService;
	private IMenuService menuService;
	private IWorkbenchWindow window;
	private IContextActivation activeContext;

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
		for (int i = 0; i < mas.length; i++) {
			IConfigurationElement ma = mas[i];
			IConfigurationElement[] items = ma.getChildren();
			for (int j = 0; j < items.length; j++) {
				IConfigurationElement item = items[j];
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
