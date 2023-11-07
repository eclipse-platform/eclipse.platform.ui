/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
package org.eclipse.ui.tests.session;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A test to verify the persistence of handler state between sessions.
 *
 * @since 3.2
 */
public class HandlerStateTest extends TestCase {

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.HandlerStateTest");
		ts.addTest(new HandlerStateTest("testInitialHandlerState"));
		ts.addTest(new HandlerStateTest("testModifiedHandlerState"));
		return ts;
	}

	/**
	 * The identifier of the command with state that we wish to test.
	 */
	private static final String COMMAND_ID = "org.eclipse.ui.tests.commandWithState";

	/**
	 * The identifier of the state with an initial value of <code>false</code>.
	 */
	private static final String FALSE_STATE_ID = "FALSE";

	/**
	 * The text after the handler state has been modified.
	 */
	private static final String MODIFIED_TEXT = "Rain rain go away come back again in april or may";

	/**
	 * The identifier of the text state with an initial value of
	 * <code>null</code>.
	 */
	private static final String TEXT_STATE_ID = "TEXT";

	/**
	 * The identifier of the state with an initial value of <code>true</code>.
	 */
	private static final String TRUE_STATE_ID = "TRUE";

	private final IWorkbench workbench = PlatformUI.getWorkbench();
	private final ICommandService service = workbench.getService(ICommandService.class);
	private final Command command = service.getCommand(COMMAND_ID);


	/**
	 * Constructs a new instance of <code>HandlerStateTest</code>.
	 *
	 * @param testName
	 *            The name of the test; may be <code>null</code>.
	 */
	public HandlerStateTest(final String testName) {
		super(testName);
	}

	/**
	 * Verifies that the initial handler state is correct. After this, the state
	 * is changed.
	 */
	public final void testInitialHandlerState() {
		State state;
		boolean actual;

		// Check the state that defaults to true.
		state = command.getState(TRUE_STATE_ID);
		actual = ((Boolean) state.getValue()).booleanValue();
		assertTrue("The initial value should be true", actual);
		state.setValue(Boolean.FALSE);

		// Check the state that defaults to false.
		state = command.getState(FALSE_STATE_ID);
		actual = ((Boolean) state.getValue()).booleanValue();
		assertTrue("The initial value should be false", !actual);
		state.setValue(Boolean.TRUE);

		// Check the text state.
		state = command.getState(TEXT_STATE_ID);
		final String text = (String) state.getValue();
		assertNull("The initial value should be null", text);
		((PersistentState) state).setShouldPersist(true);
		state.setValue(MODIFIED_TEXT);
	}

	/**
	 * Verifies that the handler state is persisted between sessions.
	 */
	public final void testModifiedHandlerState() {
		State state;
		boolean actual;

		// Test the state that defaults to true is now false.
		state = command.getState(TRUE_STATE_ID);
		actual = ((Boolean) state.getValue()).booleanValue();
		assertTrue("The value should now be different", !actual);

		// Test the state that defaults to false is now true.
		state = command.getState(FALSE_STATE_ID);
		actual = ((Boolean) state.getValue()).booleanValue();
		assertTrue("The value should now be different", actual);

		// Test that the text state is now MODIFIED_TEXT.
		state = command.getState(TEXT_STATE_ID);
		((PersistentState) state).setShouldPersist(true);
		final String text = (String) state.getValue();
		assertEquals("The modified value was not persisted", MODIFIED_TEXT,
				text);
	}
}
