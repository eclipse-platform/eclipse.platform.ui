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
package org.eclipse.ui.tests.keys;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests how {@link IKeyBindingService} binds actions to commands, in particular
 * that registering an action displaces any action previously registered for the
 * same command id.
 */
@ExtendWith(CloseTestWindowsExtension.class)
public class KeyBindingServiceTest {

	private static final String CATEGORY_ID = "org.eclipse.ui.tests.keys.keyBindingServiceCategory";

	private static final String CMD_ID = "org.eclipse.ui.tests.keys.keyBindingServiceCommand";

	private static final String OTHER_CMD_ID = "org.eclipse.ui.tests.keys.keyBindingServiceOtherCommand";

	/** Records how often it was run, so we can tell which action is bound. */
	private static final class CountingAction extends Action {
		private int runCount;

		CountingAction(String actionDefinitionId) {
			setId(actionDefinitionId);
			setActionDefinitionId(actionDefinitionId);
		}

		@Override
		public void run() {
			runCount++;
		}
	}

	private IKeyBindingService keyBindingService;

	private IHandlerService handlerService;

	@BeforeEach
	public void setUp() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		defineCommand(commandService, CMD_ID, "Key Binding Service Test Command");
		defineCommand(commandService, OTHER_CMD_ID, "Key Binding Service Other Test Command");

		IViewPart view = window.getActivePage().showView(IPageLayout.ID_OUTLINE);
		keyBindingService = view.getSite().getKeyBindingService();
		handlerService = view.getSite().getService(IHandlerService.class);
	}

	private static void defineCommand(ICommandService commandService, String commandId, String name)
			throws Exception {
		Command command = commandService.getCommand(commandId);
		if (!command.isDefined()) {
			Category category = commandService.getCategory(CATEGORY_ID);
			if (!category.isDefined()) {
				category.define("Key Binding Service Tests", null);
			}
			command.define(name, null, category);
			command.setHandler(HandlerServiceImpl.getHandler(commandId));
		}
	}

	@Test
	public void registeringAnActionBindsItToItsCommand() throws Exception {
		CountingAction action = new CountingAction(CMD_ID);
		keyBindingService.registerAction(action);

		handlerService.executeCommand(CMD_ID, null);

		assertEquals(1, action.runCount, "Registered action should have run");
	}

	@Test
	public void registeringASecondActionForTheSameCommandReplacesTheFirst() throws Exception {
		CountingAction first = new CountingAction(CMD_ID);
		CountingAction second = new CountingAction(CMD_ID);
		keyBindingService.registerAction(first);
		keyBindingService.registerAction(second);

		handlerService.executeCommand(CMD_ID, null);

		assertEquals(0, first.runCount, "Replaced action should no longer be bound");
		assertEquals(1, second.runCount, "Most recently registered action should be bound");
	}

	/**
	 * The action displaced by a second registration must be deactivated, not merely
	 * shadowed, otherwise it would resurface once the second one is unregistered.
	 */
	@Test
	public void unregisteringTheReplacementLeavesTheCommandUnhandled() throws Exception {
		CountingAction first = new CountingAction(CMD_ID);
		CountingAction second = new CountingAction(CMD_ID);
		keyBindingService.registerAction(first);
		keyBindingService.registerAction(second);
		keyBindingService.unregisterAction(second);

		assertThrows(NotHandledException.class, () -> handlerService.executeCommand(CMD_ID, null));
		assertEquals(0, first.runCount, "Replaced action should not resurface");
	}

	@Test
	public void unregisteringTheOnlyActionLeavesTheCommandUnhandled() throws Exception {
		CountingAction action = new CountingAction(CMD_ID);
		keyBindingService.registerAction(action);
		keyBindingService.unregisterAction(action);

		assertThrows(NotHandledException.class, () -> handlerService.executeCommand(CMD_ID, null));
	}

	@Test
	public void registeringTheSameActionTwiceKeepsItBound() throws Exception {
		CountingAction action = new CountingAction(CMD_ID);
		keyBindingService.registerAction(action);
		keyBindingService.registerAction(action);

		handlerService.executeCommand(CMD_ID, null);

		assertEquals(1, action.runCount, "Re-registered action should still be bound exactly once");
	}

	@Test
	public void actionsForDifferentCommandsDoNotDisplaceEachOther() throws Exception {
		CountingAction action = new CountingAction(CMD_ID);
		CountingAction otherAction = new CountingAction(OTHER_CMD_ID);
		keyBindingService.registerAction(action);
		keyBindingService.registerAction(otherAction);

		handlerService.executeCommand(CMD_ID, null);
		handlerService.executeCommand(OTHER_CMD_ID, null);

		assertEquals(1, action.runCount, "Action for the first command should still be bound");
		assertEquals(1, otherAction.runCount, "Action for the second command should be bound");
	}

	/**
	 * An action stays bound to the command it was registered under, even if its
	 * definition id changes afterwards, so that is the id a later registration
	 * displaces it by.
	 */
	@Test
	public void actionWithChangedDefinitionIdIsDisplacedByItsRegisteredCommand() throws Exception {
		CountingAction action = new CountingAction(CMD_ID);
		keyBindingService.registerAction(action);
		action.setActionDefinitionId(OTHER_CMD_ID);

		CountingAction replacement = new CountingAction(CMD_ID);
		keyBindingService.registerAction(replacement);

		handlerService.executeCommand(CMD_ID, null);

		assertEquals(0, action.runCount, "Action registered under the command should have been displaced");
		assertEquals(1, replacement.runCount, "Replacement should be bound");
	}

	@Test
	public void actionWithChangedDefinitionIdIsNotDisplacedByItsCurrentDefinitionId() throws Exception {
		CountingAction action = new CountingAction(CMD_ID);
		keyBindingService.registerAction(action);
		action.setActionDefinitionId(OTHER_CMD_ID);

		CountingAction otherAction = new CountingAction(OTHER_CMD_ID);
		keyBindingService.registerAction(otherAction);

		handlerService.executeCommand(CMD_ID, null);
		handlerService.executeCommand(OTHER_CMD_ID, null);

		assertEquals(1, action.runCount, "Action should still be bound to the command it was registered under");
		assertEquals(1, otherAction.runCount, "Action for the other command should be bound");
	}
}
