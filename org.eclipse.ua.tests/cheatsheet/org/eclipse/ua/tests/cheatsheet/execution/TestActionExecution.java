/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.execution;

/**
 * Tests which exercise the ActionRunner class
 */

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ui.internal.cheatsheets.ActionRunner;
import org.eclipse.ui.internal.cheatsheets.data.Action;

public class TestActionExecution extends TestCase {
	
	private static final String ACTION_PACKAGE = 
		"org.eclipse.ua.tests.cheatsheet.execution"; //$NON-NLS-1$
	private static final String SIMPLE_ACTION_CLASS = 
		ACTION_PACKAGE + ".SimpleAction"; //$NON-NLS-1$
	private static final String FAILING_ACTION_CLASS =
		ACTION_PACKAGE + ".FailingAction"; //$NON-NLS-1$
	private static final String ACTION_WITH_PARAMETERS_CLASS =
		ACTION_PACKAGE + ".ActionWithParameters"; //$NON-NLS-1$
	
	protected void setUp() throws Exception {
		ActionEnvironment.reset();
	}
	
	public void testSimpleAction() {
		Action action = new Action(); 
		action.setClass(SIMPLE_ACTION_CLASS);
		action.setParams(new String[0]);
		action.setPluginID(getPluginId());
		IStatus status = new ActionRunner().runAction(action, null);
		assertTrue(status.isOK());
		assertEquals(1, ActionEnvironment.getTimesCompleted());
	}
	
	public void testInvalidAction() {
		Action action = new Action(); 
		action.setClass(SIMPLE_ACTION_CLASS + "invalid"); //$NON-NLS-1$
		action.setParams(new String[0]);
		action.setPluginID(getPluginId());
		IStatus status = new ActionRunner().runAction(action, null);
		assertEquals(IStatus.ERROR, status.getSeverity());
		assertEquals(0, ActionEnvironment.getTimesCompleted());
	}
	
	public void testSimpleActionWithException() {
		Action action = new Action(); 
		action.setClass(SIMPLE_ACTION_CLASS);
		action.setParams(new String[0]);
		action.setPluginID(getPluginId());
		ActionEnvironment.setThrowException(true);
		IStatus status = new ActionRunner().runAction(action, null);
		assertEquals(IStatus.ERROR, status.getSeverity());
		assertEquals(RuntimeException.class, status.getException().getClass());
	}
	
	public void testFailingAction() {
		Action action = new Action(); 
		action.setClass(FAILING_ACTION_CLASS);
		action.setParams(new String[0]);
		action.setPluginID(getPluginId());
		IStatus status = new ActionRunner().runAction(action, null);
		assertEquals(IStatus.WARNING, status.getSeverity());
	}
	
	public void testActionWithParameters() {
		Action action = new Action(); 
		action.setClass(ACTION_WITH_PARAMETERS_CLASS);
		String value0 = "abc"; //$NON-NLS-1$
		String value1 = "defg"; //$NON-NLS-1$
		final String[] params = {value0, value1};
		action.setParams(params);
		action.setPluginID(getPluginId());
		IStatus status = new ActionRunner().runAction(action, null);
		assertTrue(status.isOK());
		assertEquals(1, ActionEnvironment.getTimesCompleted());
		String[] actuals = ActionEnvironment.getParams();
		assertEquals(2, actuals.length);
		assertEquals(value0, actuals[0]);
		assertEquals(value1, actuals[1]);
	}

	private String getPluginId() {
		return UserAssistanceTestPlugin.getDefault().getPluginId();
	}

}
