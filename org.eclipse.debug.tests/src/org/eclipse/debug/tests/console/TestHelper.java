/*******************************************************************************
 * Copyright (c) 2017 Andreas Loth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Loth - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.tests.console;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;


public final class TestHelper {

	private TestHelper() {
		throw new AssertionError("No instances of this utility class!"); //$NON-NLS-1$
	}

	public static void processUIEvents(final long millis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < millis) {
			while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {
				// loop untile the queue is empty
			}
		}
	}

	public static void waitForJobs() throws InterruptedException {
		if (Display.getCurrent() == null) {
			Thread.sleep(200);
		} else {
			processUIEvents(200);
		}
		while (!Job.getJobManager().isIdle()) {
			if (Display.getCurrent() == null) {
				Thread.sleep(200);
			} else {
				processUIEvents(200);
			}
		}
	}

}
