/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.tests.leaks.LeakTests;
import org.junit.jupiter.api.Test;

/**
 * Tests that the {@link StatusManager} singleton does not retain the statuses it
 * is told about.
 */
public class StatusManagerTest {

	/**
	 * The entry is only dropped again once the very same instance comes back
	 * through the log listener, which does not happen for every caller.
	 */
	@Test
	public void testStatusNeverReachingTheLogListenerIsNotRetained() throws Exception {
		ReferenceQueue<IStatus> queue = new ReferenceQueue<>();
		IStatus status = new Status(IStatus.ERROR, "org.eclipse.ui.tests", "never logged"); //$NON-NLS-1$ //$NON-NLS-2$
		WeakReference<IStatus> ref = new WeakReference<>(status, queue);

		StatusManager.getManager().addLoggedStatus(status);
		status = null; // drop the only strong reference

		LeakTests.checkRef(queue, ref);
	}
}
