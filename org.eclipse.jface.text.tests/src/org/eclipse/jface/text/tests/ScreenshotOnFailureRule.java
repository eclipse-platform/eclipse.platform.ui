/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import org.junit.rules.TestWatcher;

import org.eclipse.test.Screenshots;

public final class ScreenshotOnFailureRule extends TestWatcher {
	@Override
	protected void failed(Throwable e, org.junit.runner.Description description) {
		Screenshots.takeScreenshot(description.getTestClass(), description.getMethodName());
	}
}