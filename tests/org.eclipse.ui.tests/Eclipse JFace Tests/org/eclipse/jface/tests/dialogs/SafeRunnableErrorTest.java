/*******************************************************************************
 * Copyright (c) 2008 Oakland Software and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Oakland Software (francisu@ieee.org) - initial API and
 * implementation
 ******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import junit.framework.TestCase;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.util.SafeRunnable;

/**
 * NOTE - these tests are not really very good, in order to really test this you
 * need to actually see what happens in the dialog, and therefore test it by
 * hand.
 * 
 * @since 3.4
 * 
 */
public class SafeRunnableErrorTest extends TestCase {

	int count;

	protected Thread runner() {
		return new Thread(new Runnable() {

			public void run() {
				ISafeRunnable runnable = new SafeRunnable() {
					public void run() throws Exception {
						throw new RuntimeException("test exception " + ++count);
					}
				};
				SafeRunnable.run(runnable);

			}
		});
	}

	public void testSafeRunnableHandler() {
		// Just make sure that nothing bad happens when we throw here
		SafeRunnable.run(new SafeRunnable() {
			public void run() throws Exception {
				throw new RuntimeException("test exception");
			}
		});
	}

	public void testSafeRunnableHandlerOtherThread() throws Exception {
		Thread t = runner();
		t.run();
		t.join();
	}

	public void testSafeRunnableHandlerMulti() {
		ISafeRunnable runnable = new SafeRunnable() {
			public void run() throws Exception {
				throw new RuntimeException("test exception " + ++count);
			}
		};

		// Make sure these don't block
		SafeRunnable.run(runnable);
		SafeRunnable.run(runnable);
		SafeRunnable.run(runnable);
		assertEquals(3, count);
	}

}
