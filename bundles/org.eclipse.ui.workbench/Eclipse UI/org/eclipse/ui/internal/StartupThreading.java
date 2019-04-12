/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.misc.StatusUtil;

/**
 * @since 3.3
 *
 */
public final class StartupThreading {

	private static Display display;

	public abstract static class StartupRunnable implements Runnable {
		private Throwable throwable;

		@Override
		public final void run() {
			try {
				runWithException();
			} catch (Throwable t) {
				this.throwable = t;
			}
		}

		public abstract void runWithException() throws Throwable;

		public Throwable getThrowable() {
			return throwable;
		}
	}

	static void setDisplay(Display display) {
		StartupThreading.display = display;
	}

	public static void runWithWorkbenchExceptions(StartupRunnable r) throws WorkbenchException {
		display.syncExec(r);
		Throwable throwable = r.getThrowable();
		if (throwable != null) {
			if (throwable instanceof Error) {
				throw (Error) throwable;
			} else if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			} else if (throwable instanceof WorkbenchException) {
				throw (WorkbenchException) throwable;
			} else {
				throw new WorkbenchException(StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, throwable));
			}
		}
	}

	public static void runWithPartInitExceptions(StartupRunnable r) throws PartInitException {
		display.syncExec(r);
		Throwable throwable = r.getThrowable();
		if (throwable != null) {
			if (throwable instanceof Error) {
				throw (Error) throwable;
			} else if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			} else if (throwable instanceof WorkbenchException) {
				throw (PartInitException) throwable;
			} else {
				throw new PartInitException(StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, throwable));
			}
		}
	}

	public static void runWithThrowable(StartupRunnable r) throws Throwable {
		display.syncExec(r);
		Throwable throwable = r.getThrowable();
		if (throwable != null) {
			throw throwable;
		}
	}

	public static void runWithoutExceptions(StartupRunnable r) throws RuntimeException {
		display.syncExec(r);
		Throwable throwable = r.getThrowable();
		if (throwable != null) {
			if (throwable instanceof Error) {
				throw (Error) throwable;
			} else if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			} else {
				throw new RuntimeException(throwable);
			}
		}
	}

}
