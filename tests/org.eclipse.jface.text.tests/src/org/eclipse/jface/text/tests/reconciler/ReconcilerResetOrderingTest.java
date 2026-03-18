/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.reconciler;

import org.eclipse.jface.text.reconciler.AbstractReconciler;

/**
 * Regression test for the race condition in
 * {@link AbstractReconciler#install BackgroundWorker.reset()} where
 * {@code reconcilerReset()} could be called after the background thread was
 * already notified, allowing {@code process()} to run before the reset hook.
 * <p>
 * This test widens the race window by introducing a 50ms delay before logging
 * {@code reconcilerReset}. Without the fix (reconcilerReset called before queue
 * notification), the background thread is already awake during the delay and
 * reaches {@code process()} first, causing {@code testReplacingDocumentWhenClean}
 * to deterministically fail.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-platform/eclipse.platform.ui/issues/2708">Issue 2708</a>
 */
public class ReconcilerResetOrderingTest extends FastAbstractReconcilerTest {

	@Override
	void beforeReconcilerResetLogged() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
