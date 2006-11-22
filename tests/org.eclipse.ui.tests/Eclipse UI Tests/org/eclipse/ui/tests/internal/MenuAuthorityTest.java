/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

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
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.menus.IMenuService;
import org.eclipse.ui.internal.menus.MenuActivation;
import org.eclipse.ui.internal.menus.MenuAuthority;
import org.eclipse.ui.internal.services.ISourceProviderService;
import org.eclipse.ui.tests.commands.ActiveContextExpression;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class MenuAuthorityTest extends UITestCase {

	/**
	 * 
	 */
	private static final String EXTENSION_ID = "org.eclipse.ui.tests.menusX1";

	/**
	 * @param testName
	 */
	public MenuAuthorityTest(String testName) {
		super(testName);
	}

	private static final String CONTEXT_ID1 = "org.eclipse.ui.menus.contexts.test1";
	private IContextService contextService;
	private IMenuService menuService;
	private MenuAuthority menuAuth;
	private IWorkbenchWindow window;
	private IContextActivation activeContext;

	public void testBasicContribution() throws Exception {

		IAction a = new Action() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				System.out.println("Hello action");
			}
		};
		ActionContributionItem item = new ActionContributionItem(a);
		Expression activeContextExpr = new ActiveContextExpression(CONTEXT_ID1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });

		MenuActivation activation = new MenuActivation(item, activeContextExpr,
				menuService);
		menuAuth.addContribution(activation);
		assertFalse("starting state", activation.evaluate(menuAuth
				.getCurrentState()));

		activeContext = contextService.activateContext(CONTEXT_ID1);

		assertTrue("active context", activation.evaluate(menuAuth
				.getCurrentState()));

		contextService.deactivateContext(activeContext);
		activeContext = null;

		assertFalse("after deactivation", activation.evaluate(menuAuth
				.getCurrentState()));

		menuAuth.removeContribution(activation);
	}

	public void testExtensionContributionExpression() throws Exception {
		IAction a = new Action() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				System.out.println("Hello action");
			}
		};
		ActionContributionItem aci = new ActionContributionItem(a);

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint menus2 = reg.getExtensionPoint("org.eclipse.ui.menus2");
		IExtension extension = menus2.getExtension(EXTENSION_ID);

		IConfigurationElement[] mas = extension.getConfigurationElements();
		Expression activeContextExpr = null;
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
					activeContextExpr = ExpressionConverter.getDefault()
							.perform(visibleWhenElement.getChildren()[0]);
				}
			}
		}
		assertNotNull("Failed to find expression", activeContextExpr);

		MenuActivation activation = new MenuActivation(aci, activeContextExpr,
				menuService);
		menuAuth.addContribution(activation);
		assertFalse("starting state", activation.evaluate(menuAuth
				.getCurrentState()));

		activeContext = contextService.activateContext(CONTEXT_ID1);
		assertTrue("active context", activation.evaluate(menuAuth
				.getCurrentState()));

		contextService.deactivateContext(activeContext);
		activeContext = null;

		assertFalse("after deactivation", activation.evaluate(menuAuth
				.getCurrentState()));

		menuAuth.removeContribution(activation);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		
		window = openTestWindow();
		menuService = (IMenuService) window.getService(IMenuService.class);
		contextService = (IContextService) window
				.getService(IContextService.class);
		Context context1 = contextService.getContext(CONTEXT_ID1);
		if (!context1.isDefined()) {
			context1.define("Menu Test 1", "Menu test 1",
					IContextService.CONTEXT_ID_DIALOG_AND_WINDOW);
		}

		menuAuth = new MenuAuthority((Window) window);
		ISourceProviderService sp = (ISourceProviderService) window
				.getService(ISourceProviderService.class);
		ISourceProvider[] sourceProviders = sp.getSourceProviders();
		for (int i = 0; i < sourceProviders.length; i++) {
			ISourceProvider sourceProvider = sourceProviders[i];
			menuAuth.addSourceProvider(sourceProvider);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		if (activeContext != null) {
			contextService.deactivateContext(activeContext);
			activeContext = null;
		}
		ISourceProviderService sp = (ISourceProviderService) window
				.getService(ISourceProviderService.class);
		ISourceProvider[] sourceProviders = sp.getSourceProviders();
		for (int i = 0; i < sourceProviders.length; i++) {
			ISourceProvider sourceProvider = sourceProviders[i];
			menuAuth.removeSourceProvider(sourceProvider);
		}
		menuAuth = null;
		menuService = null;
		contextService = null;
		window = null;
		
		super.doTearDown();
	}
}
