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

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.tests.harness.util.CallHistory;

public class MockPartListener implements IPartListener {
	private final CallHistory callTrace;

	public MockPartListener() {
		callTrace = new CallHistory(this);
	}

	public CallHistory getCallHistory() {
		return callTrace;
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		callTrace.add("partActivated");
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		callTrace.add("partBroughtToTop");
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		callTrace.add("partClosed");
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		callTrace.add("partDeactivated");
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
		callTrace.add("partOpened");
	}
}
