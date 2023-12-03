/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.activities;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class MenusTest extends UITestCase {

	private TestFactory factory;
	private IWorkbenchWindow window;
	private IMenuService service;
	private Set<String> enabledActivities;

	/**
	 * @since 3.3
	 */
	private static final class TestFactory extends AbstractContributionFactory {
		private CommandContributionItem fooItemWithNoVisibilityClause;
		private CommandContributionItem barItemWithNoVisibilityClause;

		private TestFactory(String location, String namespace) {
			super(location, namespace);
		}

		@Override
		public void createContributionItems(IServiceLocator serviceLocator,
				IContributionRoot additions) {
			fooItemWithNoVisibilityClause = new CommandContributionItem(
					serviceLocator, "foo", "foo", null, null, null, null,
					"Hi there", null, null, CommandContributionItem.STYLE_PUSH);
			barItemWithNoVisibilityClause = new CommandContributionItem(
					serviceLocator, "bar", "bar", null, null, null, null,
					"Muppet", null, null, CommandContributionItem.STYLE_PUSH);

			additions.addContributionItem(fooItemWithNoVisibilityClause, null);
			additions.addContributionItem(barItemWithNoVisibilityClause, null);
		}

		/**
		 * @return Returns the fooItemWithNoVisibilityClause.
		 */
		public CommandContributionItem getFooItemWithNoVisibilityClause() {
			return fooItemWithNoVisibilityClause;
		}

		/**
		 * @return Returns the barItemWithNoVisibilityClause.
		 */
		public CommandContributionItem getBarItemWithNoVisibilityClause() {
			return barItemWithNoVisibilityClause;
		}
	}

	public MenusTest() {
		super(MenusTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = openTestWindow();
		enabledActivities = window.getWorkbench().getActivitySupport()
				.getActivityManager().getEnabledActivityIds();
		service = window.getService(IMenuService.class);
		assertNotNull(service);
	}

	@Override
	protected void doTearDown() throws Exception {
		window.getWorkbench().getActivitySupport().setEnabledActivityIds(
				enabledActivities);
		assertEquals(enabledActivities, window.getWorkbench()
				.getActivitySupport().getActivityManager()
				.getEnabledActivityIds());
		if (factory != null) {
			service.removeContributionFactory(factory);
		}
		super.doTearDown();
	}

	@Test
	public void testNoNamespaceFactory() {
		window.getWorkbench().getActivitySupport().setEnabledActivityIds(
				Collections.singleton("menuTest1")); // enable the foo
														// activity

		factory = new TestFactory("menu:tests", null);
		service.addContributionFactory(factory);
		MenuManager manager = new MenuManager();
		service.populateContributionManager(manager, "menu:tests");
		assertTrue(manager.getSize() > 0);

		assertTrue(factory.getFooItemWithNoVisibilityClause().isVisible());
		assertTrue(factory.getBarItemWithNoVisibilityClause().isVisible());

		window.getWorkbench().getActivitySupport().setEnabledActivityIds(
				Collections.EMPTY_SET);
		assertTrue(factory.getFooItemWithNoVisibilityClause().isVisible());
		assertTrue(factory.getBarItemWithNoVisibilityClause().isVisible());

	}

	@Test
	@Ignore
	public void XXXtestMenuVisibilityWithCustomFactory() {
		window.getWorkbench().getActivitySupport().setEnabledActivityIds(
				Collections.singleton("menuTest1")); // enable the foo
														// activity
		factory = new TestFactory("menu:tests",
				"org.eclipse.ui.tests");
		service.addContributionFactory(factory);
		MenuManager manager = new MenuManager();
		service.populateContributionManager(manager, "menu:tests");
		assertTrue(manager.getSize() > 0);

		assertTrue(factory.getFooItemWithNoVisibilityClause().isVisible());
		assertFalse(factory.getBarItemWithNoVisibilityClause().isVisible());

		window.getWorkbench().getActivitySupport().setEnabledActivityIds(
				Collections.EMPTY_SET);
		assertFalse(factory.getFooItemWithNoVisibilityClause().isVisible());
		assertFalse(factory.getBarItemWithNoVisibilityClause().isVisible());

		window.getWorkbench().getActivitySupport().setEnabledActivityIds(
				Collections.singleton("menuTest2")); // enable the all
														// activity
		assertFalse(factory.getFooItemWithNoVisibilityClause().isVisible());
		assertTrue(factory.getBarItemWithNoVisibilityClause().isVisible());

	}
}
