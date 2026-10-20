/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.UIPlugin;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Logs tests start and end.
 */
public class LogTestListener implements TestExecutionListener {

	@Override
	public void executionStarted(TestIdentifier id) {
		logInfo(id.getDisplayName() + " STARTING");
	}

	@Override
	public void executionFinished(TestIdentifier id, TestExecutionResult result) {
		logInfo(id.getDisplayName() + " DONE: " + result.getStatus());
	}

	private static void logInfo(String message) {
		UIPlugin.getDefault().getLog().log(Status.info(message));
	}
}
