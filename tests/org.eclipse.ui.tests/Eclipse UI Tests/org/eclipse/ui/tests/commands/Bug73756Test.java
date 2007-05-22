/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.statushandlers.TestStatusHandler;

/**
 * A tests whether is active will log an exception if the command is not
 * defined.
 * 
 * @since 3.1
 */
public final class Bug73756Test extends UITestCase {

	private static String CMD_ID = "a command that is not defined";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(ExternalActionManager.class.getName());

	private static int SEVERITY = IStatus.ERROR;

	private static String MESSAGE = MessageFormat.format(Util.translateString(
			RESOURCE_BUNDLE, "undefinedCommand.WarningMessage", null), //$NON-NLS-1$
			new String[] { CMD_ID });

	private static String PLUGIN_ID = "org.eclipse.jface";

	/**
	 * Constructs a new instance of <code>Bug73756Test</code>.
	 * 
	 * @param name
	 *            The name of the test
	 */
	public Bug73756Test(final String name) {
		super(name);
	}

	/**
	 * Tests that calling <code>isActive()</code> on an undefined command
	 * causes a log message to be written. This simple calls
	 * <code>isActive()</code> for a bogus command identifier. A log listener
	 * flips a boolean flag if a log message is written.
	 */
	public final void testUndefinedCommandIsActiveLogged() {
		// Check if a bogus command is active.
		ExternalActionManager.getInstance().getCallback().isActive(CMD_ID);

		// Check if a correct status is logged
		assertEquals(TestStatusHandler.getLastHandledStyle(), StatusManager.LOG);
		assertStatusAdapter(TestStatusHandler.getLastHandledStatusAdapter());
	}

	/**
	 * Checks whether the last handled status is correct
	 */
	private void assertStatusAdapter(StatusAdapter statusAdapter) {
		assertNotNull("A warning should have been logged.", statusAdapter);
		IStatus status = statusAdapter.getStatus();
		assertEquals(status.getSeverity(), SEVERITY);
		assertEquals(status.getPlugin(), PLUGIN_ID);
		assertEquals(status.getMessage(), MESSAGE);
	}
}
