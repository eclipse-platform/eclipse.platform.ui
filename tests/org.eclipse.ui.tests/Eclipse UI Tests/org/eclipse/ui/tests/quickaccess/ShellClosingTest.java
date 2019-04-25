/*******************************************************************************
 * Copyright (c) 2015, 2019 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.quickaccess;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * @since 3.5
 */
public class ShellClosingTest {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	/**
	 * Bug 433746: dispose SearchField shell
	 *
	 * Testing bot (with code like in test sample) disposes parent shell of text
	 * control and it causes the issue because SearchField keeps another
	 * invisible shell which is not get disposed, so later when that shell is
	 * disposed it tried to accesses controls which are being disposed already.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testClosingShellsBug433746() throws Throwable {
		final Throwable result[] = new Throwable[1];
		Display.getDefault().syncExec(() -> {
			try {
				Shell[] shells = Display.getDefault().getShells();
				Shell active = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				for (Shell shell : shells) {
					if (!(active == shell) && !shell.isDisposed()) {
						shell.close();
					}
				}
			} catch (Throwable e) {
				result[0] = e;
			}
		});
		if (result[0] != null) {
			throw result[0];
		}
	}
}
