/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
package org.eclipse.jface.tests.dialogs;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ DialogTest.class, StatusDialogTest.class, DialogSettingsTest.class, InputDialogTest.class,
		TitleAreaDialogTest.class, SafeRunnableErrorTest.class, ProgressIndicatorStyleTest.class,
		ProgressMonitorDialogTest.class, PlainMessageDialogTest.class })
public class AllDialogTests {

	public static void main(String[] args) {
		JUnitCore.main(AllDialogTests.class.getName());
	}
}
