/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.api;


import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.junit.Assert;

public class StartupClass implements IStartup {

	/**
	 * This boolean should only be true if the earlyStartup() method
	 * has been called.
	 */
	private static boolean earlyStartupCalled = false;

	/**
	 * This boolean should only be true if the earlyStartup() method
	 * has completed.
	 */
	private static boolean earlyStartupCompleted = false;

	@Override
	public void earlyStartup() {
		earlyStartupCalled = true;
		Assert.assertNull("IStartup should run in non-UI thread", Display.getCurrent());
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// ignore
		}
		earlyStartupCompleted = true;
	}

	public static boolean getEarlyStartupCalled() {
		return earlyStartupCalled;
	}

	public static boolean getEarlyStartupCompleted() {
		return earlyStartupCompleted;
	}

	/**
	 * Reset the flags.
	 */
	public static void reset() {
		earlyStartupCalled = false;
		earlyStartupCompleted = false;
	}
}
