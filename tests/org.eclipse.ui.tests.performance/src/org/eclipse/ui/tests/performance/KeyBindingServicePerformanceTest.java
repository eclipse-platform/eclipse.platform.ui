/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.reportTimings;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Measures how long it takes a part to register its actions with the key
 * binding service, which is what every editor does while it is being created.
 * <p>
 * The action count is a parameter so that the growth of the registration cost
 * with the number of actions is visible, rather than only its value at one
 * arbitrary size.
 */
@SuppressWarnings("deprecation") // IKeyBindingService is the API under measurement
@RunWith(Parameterized.class)
public class KeyBindingServicePerformanceTest {

	private static final String CATEGORY_ID = "org.eclipse.ui.tests.performance.keyBindingCategory";

	private static final String COMMAND_ID_PREFIX = "org.eclipse.ui.tests.performance.keyBindingCommand";

	/** AbstractTextEditor registers on the order of the larger counts here. */
	private static final int MAX_ACTION_COUNT = 200;

	private static final int WARMUP_ROUNDS = 5;

	private static final int MIN_ROUNDS = 20;

	private static final int MAX_ROUNDS = 200;

	private static final int MAX_MEASURE_TIME_MS = 5000;

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private final int actionCount;

	private IKeyBindingService keyBindingService;

	private List<IAction> actions;

	@Parameters(name = "{index}: {0} actions")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 25 }, { 50 }, { 100 }, { MAX_ACTION_COUNT } });
	}

	public KeyBindingServicePerformanceTest(int actionCount) {
		this.actionCount = actionCount;
	}

	@Before
	public void setUp() throws Exception {
		IWorkbenchWindow window = openTestWindow(UIPerformanceTestRule.PERSPECTIVE1);
		ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		Category category = commandService.getCategory(CATEGORY_ID);
		if (!category.isDefined()) {
			category.define("Key Binding Service Performance", null);
		}
		for (int i = 0; i < actionCount; i++) {
			Command command = commandService.getCommand(COMMAND_ID_PREFIX + i);
			if (!command.isDefined()) {
				command.define("Key Binding Performance Command " + i, null, category);
			}
		}

		IViewPart view = window.getActivePage().showView(IPageLayout.ID_OUTLINE);
		keyBindingService = view.getSite().getKeyBindingService();
		assertNotNull("No key binding service for the test part", keyBindingService);

		actions = new ArrayList<>(actionCount);
		for (int i = 0; i < actionCount; i++) {
			actions.add(new CommandAction(COMMAND_ID_PREFIX + i));
		}
	}

	@Test
	public void test() throws Exception {
		for (int i = 0; i < WARMUP_ROUNDS; i++) {
			registerAll();
			unregisterAll();
		}

		List<Long> registerTimes = new ArrayList<>();
		List<Long> unregisterTimes = new ArrayList<>();

		exercise(() -> {
			registerTimes.add(registerAll());
			unregisterTimes.add(unregisterAll());
		}, MIN_ROUNDS, MAX_ROUNDS, MAX_MEASURE_TIME_MS);

		reportTimings("KeyBindingService register " + actionCount + " actions", registerTimes);
		reportTimings("KeyBindingService unregister " + actionCount + " actions", unregisterTimes);
	}

	private long registerAll() {
		long before = System.nanoTime();
		for (IAction action : actions) {
			keyBindingService.registerAction(action);
		}
		return System.nanoTime() - before;
	}

	private long unregisterAll() {
		long before = System.nanoTime();
		for (IAction action : actions) {
			keyBindingService.unregisterAction(action);
		}
		return System.nanoTime() - before;
	}

	/** Distinct command id per action, as a part's actions have. */
	private static final class CommandAction extends Action {
		CommandAction(String commandId) {
			setId(commandId);
			setActionDefinitionId(commandId);
		}
	}
}
