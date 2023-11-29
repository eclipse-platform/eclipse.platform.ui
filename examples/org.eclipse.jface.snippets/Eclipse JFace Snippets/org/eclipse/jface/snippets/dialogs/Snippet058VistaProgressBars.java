/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

public class Snippet058VistaProgressBars {

	/**
	 * Open a progress monitor dialog and switch the blocking.
	 */
	public static void main(String[] args) {

		Display display = new Display();

		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
		IRunnableWithProgress runnable = createRunnableFor(dialog);
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

		display.dispose();
	}

	private static IRunnableWithProgress createRunnableFor(
			final ProgressMonitorDialog dialog) {

		return monitor -> {

			IProgressMonitor blocking = monitor;

			blocking.beginTask("Vista Coolness", 100);
			for (int i = 0; i < 10; i++) {
				blocking.setBlocked(new Status(IStatus.WARNING, "Blocked", "This is blocked on Vista"));
				blocking.worked(5);
				spin(dialog.getShell().getDisplay());
				blocking.clearBlocked();
				blocking.worked(5);
				spin(dialog.getShell().getDisplay());
				if (monitor.isCanceled())
					return;
			}
			blocking.done();
		};
	}

	private static void spin(final Display display) {
		display.syncExec(() -> {
			long endTime = System.currentTimeMillis() + 1000;

			while (System.currentTimeMillis() < endTime)
				display.readAndDispatch();

		});

	}
}
