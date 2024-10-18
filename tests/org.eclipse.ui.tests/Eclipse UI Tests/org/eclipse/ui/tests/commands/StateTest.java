/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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

package org.eclipse.ui.tests.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.menus.TextState;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.handlers.RegistryRadioState;
import org.eclipse.ui.tests.TestPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests various aspects of command state.
 *
 * @since 3.2
 */
public class StateTest {

	private static final String TEXT_HELLO = "hello";

	private static final class ObjectStateHandler extends
			AbstractHandlerWithState {

		Object currentValue;
		String textValue;

		@Override
		public final Object execute(final ExecutionEvent event) {
			getState(OBJECT_STATE_ID).setValue(OBJECT_CHANGED);
			return OBJECT_CHANGED;
		}

		@Override
		public final void handleStateChange(final State state,
				final Object oldValue) {
			if (OBJECT_STATE_ID.equals(state.getId())) {
				currentValue = state.getValue();
			} else if (TEXT_STATE_ID.equals(state.getId())) {
				textValue = (String) state.getValue();
			}
		}
	}

	private static final class StateListener implements IStateListener {
		Object currentValue;
		String textValue;

		@Override
		public final void handleStateChange(final State state,
				final Object oldValue) {

			if (OBJECT_STATE_ID.equals(state.getId())) {
				currentValue = state.getValue();
			} else if (TEXT_STATE_ID.equals(state.getId())) {
				textValue = (String) state.getValue();
			}
		}
	}

	/**
	 * The identifier of the command with state that we wish to test.
	 */
	private static final String COMMAND_ID = "org.eclipse.ui.tests.commandWithState";

	/**
	 * The object to which the command is set as a test.
	 */
	private static final Object OBJECT_CHANGED = "CHANGED";

	/**
	 * The object to which the command is set before the test starts.
	 */
	private static final Object OBJECT_INITIAL = "INITIAL";

	/**
	 * The identifier of the state storing a simple object.
	 */
	private static final String OBJECT_STATE_ID = "OBJECT";

	/**
	 * The identifier of the state storing some text.
	 */
	private static final String TEXT_STATE_ID = "TEXT";

	/**
	 * The object state handler.
	 */
	private ObjectStateHandler handler;

	/**
	 * The handler activation for the object state handler.
	 */
	private IHandlerActivation handlerActivation;

	private final IWorkbench fWorkbench = PlatformUI.getWorkbench();

	@Before
	public final void doSetUp() {
		// Reset the object state to the initial object.
		final ICommandService commandService = fWorkbench
				.getService(ICommandService.class);
		final Command command = commandService.getCommand(COMMAND_ID);
		command.getState(OBJECT_STATE_ID).setValue(OBJECT_INITIAL);
		command.getState(TEXT_STATE_ID).setValue(null);

		// Register the object state handler.
		handler = new ObjectStateHandler();
		final IHandlerService handlerService = fWorkbench
				.getService(IHandlerService.class);
		handlerActivation = handlerService.activateHandler(COMMAND_ID, handler);
		// side effect: force handler association in
		// org.eclipse.e4.core.commands.internal.HandlerServiceHandler
		// This is done by org.eclipse.ui.menus.CommandContributionItem.isEnabled() and
		// org.eclipse.e4.ui.workbench.renderers.swt.ToolItemUpdater
		// during normal operation
		// See org.eclipse.ui.tests.commands.WorkbenchStateTest for an integration test
		// that does not rely on explicit polling
		assertTrue(command.isHandled());
	}

	@After
	public final void doTearDown() {
		// Unregister the object state handler.
		final IHandlerService handlerService = fWorkbench
				.getService(IHandlerService.class);
		handlerService.deactivateHandler(handlerActivation);
		handlerActivation = null;
		handler.dispose();
		handler = null;
	}

	/**
	 * Tests that if the handler changes the state, a listener to the state
	 * retrieved from the command is notified.
	 *
	 * @throws CommandException
	 *             Never.
	 */
	@Test
	public final void testCommandNotifiedOfStateChange()
			throws CommandException {
		// Attach a listener to the state on the command.
		final ICommandService commandService = fWorkbench
				.getService(ICommandService.class);
		final Command command = commandService.getCommand(COMMAND_ID);
		final State state = command.getState(OBJECT_STATE_ID);
		final StateListener listener = new StateListener();
		listener.currentValue = state.getValue();
		state.addListener(listener);

		// Check the initial state.
		assertSame("The initial state was not correct", OBJECT_INITIAL,
				listener.currentValue);

		// Run the handler.
		final IHandlerService handlerService = fWorkbench
				.getService(IHandlerService.class);
		handlerService.executeCommand(COMMAND_ID, null);

		// Check the state.
		assertSame(
				"The state on the command after the handler changed was not correct",
				OBJECT_CHANGED, listener.currentValue);

		// Remove the listener.
		state.removeListener(listener);
	}

	/**
	 * Tests that if the handler changes the state, the command reflects these
	 * changes.
	 *
	 * @throws CommandException
	 *             Never.
	 */
	@Test
	public final void testStateChangeReflectedInCommand()
			throws CommandException {
		// Get the command.
		final ICommandService commandService = fWorkbench
				.getService(ICommandService.class);
		final Command command = commandService.getCommand(COMMAND_ID);

		// Check the initial state.
		assertSame("The initial state was not correct", OBJECT_INITIAL, command
				.getState(OBJECT_STATE_ID).getValue());

		// Run the handler.
		final IHandlerService handlerService = fWorkbench
				.getService(IHandlerService.class);
		handlerService.executeCommand(COMMAND_ID, null);

		// Check the state.
		assertSame(
				"The state on the command after the handler changed was not correct",
				OBJECT_CHANGED, command.getState(OBJECT_STATE_ID).getValue());
	}

	/**
	 * Tests that if the command changes the state, the handler reflects these
	 * changes.
	 */
	@Test
	public final void testStateChangeReflectedInHandler() {
		// Check the initial state.
		assertSame("The initial state was not correct", OBJECT_INITIAL,
				handler.currentValue);

		// Change the state on the command.
		final ICommandService commandService = fWorkbench
				.getService(ICommandService.class);
		final Command command = commandService.getCommand(COMMAND_ID);
		command.getState(OBJECT_STATE_ID).setValue(OBJECT_CHANGED);

		// Check the state.
		assertSame(
				"The state on the command after the handler changed was not correct",
				OBJECT_CHANGED, handler.currentValue);
	}

	@Test
	public final void testTextState() {
		assertNull(handler.textValue);
		final ICommandService commandService = fWorkbench
				.getService(ICommandService.class);
		final Command command = commandService.getCommand(COMMAND_ID);
		command.getState(TEXT_STATE_ID).setValue(TEXT_HELLO);
		assertEquals(TEXT_HELLO, handler.textValue);
	}

	@Test
	public final void testTextStateListener() {
		assertNull(handler.textValue);
		final ICommandService commandService = fWorkbench
				.getService(ICommandService.class);
		final Command command = commandService.getCommand(COMMAND_ID);
		State state = command.getState(TEXT_STATE_ID);
		final StateListener listener = new StateListener();
		assertNull(state.getValue());
		assertNull(listener.textValue);
		assertNull(handler.textValue);

		state.addListener(listener);
		state.setValue(TEXT_HELLO);
		assertEquals(TEXT_HELLO, handler.textValue);
		assertEquals(TEXT_HELLO, listener.textValue);
	}

	@Test
	public final void testTextPreference() {
		final ICommandService commandService = fWorkbench
				.getService(ICommandService.class);
		final Command command = commandService.getCommand(COMMAND_ID);
		State state = command.getState(TEXT_STATE_ID);
		state.setValue(TEXT_HELLO);
		assertTrue(state instanceof PersistentState);
		PersistentState pstate = (PersistentState) state;
		IPreferenceStore preferenceStore = TestPlugin.getDefault().getPreferenceStore();
		pstate.save(preferenceStore, COMMAND_ID
				+ "." + TEXT_STATE_ID);
		TextState nstate = new TextState();
		assertNull(nstate.getValue());
		nstate.load(preferenceStore, COMMAND_ID
				+ "." + TEXT_STATE_ID);
		assertEquals(TEXT_HELLO, nstate.getValue());
	}

	@Test
	public final void testRadioState() {
		RegistryRadioState state1 = new RegistryRadioState();
		state1.setInitializationData(null, "class", COMMAND_ID);
		assertEquals(Boolean.FALSE, state1.getValue());
		RegistryRadioState state2 = new RegistryRadioState();
		state2.setInitializationData(null, "class", COMMAND_ID);
		assertEquals(Boolean.FALSE, state2.getValue());

		state1.setValue(Boolean.TRUE);
		assertEquals(Boolean.TRUE, state1.getValue());
		assertEquals(Boolean.FALSE, state2.getValue());

		state2.setValue(Boolean.TRUE);
		assertEquals(Boolean.FALSE, state1.getValue());
		assertEquals(Boolean.TRUE, state2.getValue());
	}
}
