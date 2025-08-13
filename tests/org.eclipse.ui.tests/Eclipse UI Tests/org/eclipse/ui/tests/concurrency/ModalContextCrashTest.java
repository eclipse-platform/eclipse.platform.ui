/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * Makes ModalContext thread crash and hang the IDE
 */
public class ModalContextCrashTest {

	@Test
	public void testCrash() throws Exception {
		IRunnableWithProgress operation = new CrashingRunnable();
		assertThrows(InvocationTargetException.class,
				() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, operation));
		if (Thread.interrupted()) {
			fail("Thread was interrupted at end of test");
		}
		assertFalse(Thread.interrupted());
	}

	private static final class CrashingRunnable implements IRunnableWithProgress, IThreadListener {

		@Override
		public void run(IProgressMonitor monitor) {
		}

		@Override
		public void threadChange(Thread thread) {
			// only throw the exception in the finally block
			// of ModalContextThread
			if (Display.findDisplay(thread) != null) {
				throw new RuntimeException("Simulated exception during threadChange");
			}
		}
	}

}
