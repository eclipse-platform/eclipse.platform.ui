/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.commands.ActionHandler;
import org.junit.Test;

/**
 * This test whether an ActionHandler will update its internal cache of
 * properties when a RetargetAction changes only its handler.
 *
 * @since 3.0.1
 */
public class Bug70503Test {

	private class PubliclyRetargettableAction extends RetargetAction {
		/**
		 * Constructs a new instance of <code>PubliclyRetargettableAction</code>.
		 *
		 * @param actionID
		 *            The action identifier to pass to the
		 *            <code>RetargetAction</code>; should not be
		 *            <code>null</code>.
		 * @param text
		 *            The text to be displayed on the action; may be
		 *            <code>null</code> if there should be no text.
		 */
		public PubliclyRetargettableAction(String actionID, String text) {
			super(actionID, text);
		}

		/**
		 * A public version of the <code>setActionHandler</code> method.
		 *
		 * @param handler
		 *            The new action handler; may be <code>null</code> if
		 *            there is no handler currently.
		 */
		private final void changeHandler(final IAction handler) {
			super.setActionHandler(handler);
		}
	}

	/**
	 * Tests whether changing only the handler will update an action handler.
	 * The set up is a <code>RetargetAction</code> wrapped in an
	 * <code>ActionHandler</code>. The test verifies a switch back and forth
	 * to make sure that the updates are happening.
	 */
	@Test
	public final void testHandlerChangeCausesUpdate() {
		final PubliclyRetargettableAction retargetAction = new PubliclyRetargettableAction(
				"actionID", "text");
		final ActionHandler actionHandler = new ActionHandler(retargetAction);
		assertFalse("The retarget action handler should start 'unhandled'",
				((Boolean) actionHandler.getAttributeValuesByName().get(
						"handled")).booleanValue());
		retargetAction.changeHandler(new PubliclyRetargettableAction(
				"actionID", "text"));
		assertTrue(
				"The retarget action handler should recognize the new handler.",
				((Boolean) actionHandler.getAttributeValuesByName().get(
						"handled")).booleanValue());
		retargetAction.changeHandler(null);
		assertFalse(
				"The retarget action handler should recognize that the handler is now gone.",
				((Boolean) actionHandler.getAttributeValuesByName().get(
						"handled")).booleanValue());
	}
}
