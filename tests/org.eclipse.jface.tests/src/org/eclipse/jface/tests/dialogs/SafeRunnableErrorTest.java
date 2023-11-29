/*******************************************************************************
 * Copyright (c) 2008, 2017 Oakland Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Oakland Software (francisu@ieee.org) - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.util.SafeRunnable;
import org.junit.Test;

/**
 * NOTE - these tests are not really very good, in order to really test this you
 * need to actually see what happens in the dialog, and therefore test it by
 * hand.
 *
 * @since 3.4
 */
public class SafeRunnableErrorTest {

	int count;

	protected Thread runner() {
		return new Thread(() -> {
			ISafeRunnable runnable = new SafeRunnable() {
				@Override
				public void run() throws Exception {
					throw new RuntimeException("test exception " + ++count);
				}
			};
			SafeRunnable.run(runnable);

		});
	}

	@Test
	public void testSafeRunnableHandler() {
		// Just make sure that nothing bad happens when we throw here
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				throw new RuntimeException("test exception");
			}
		});
	}

	@Test
	public void testSafeRunnableHandlerOtherThread() throws Exception {
		Thread t = runner();
		t.run();
		t.join();
	}

	@Test
	public void testSafeRunnableHandlerMulti() {
		ISafeRunnable runnable = new SafeRunnable() {
			@Override
			public void run() throws Exception {
				throw new RuntimeException("test exception " + ++count);
			}
		};

		// Make sure these don't block
		int expectedRuns = 3;
		for (int run = 0; run < expectedRuns; run++) {
			SafeRunnable.run(runnable);
		}
		assertEquals(expectedRuns, count);
	}

}
