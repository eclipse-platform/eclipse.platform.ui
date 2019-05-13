/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tests.harness.util.CallHistory;

public class MockActionDelegate implements IWorkbenchWindowActionDelegate {

	public CallHistory callHistory;

	public static final String ACTION_SET_ID = "org.eclipse.ui.tests.api.MockActionSet";

	public static MockActionDelegate lastDelegate;

	public MockActionDelegate() {
		callHistory = new CallHistory(this);
		lastDelegate = this;
	}

	/*
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		callHistory.add("run");
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		callHistory.add("selectionChanged");
	}

	/*
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

}

