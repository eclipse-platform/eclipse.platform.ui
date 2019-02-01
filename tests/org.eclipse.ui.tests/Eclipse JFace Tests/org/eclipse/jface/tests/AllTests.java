/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package org.eclipse.jface.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ org.eclipse.jface.tests.action.AllTests.class, org.eclipse.jface.tests.dialogs.AllTests.class,
		org.eclipse.jface.tests.images.AllTests.class, org.eclipse.jface.tests.viewers.AllTests.class,
		org.eclipse.jface.tests.layout.AllTests.class, org.eclipse.jface.tests.preferences.AllTests.class,
		org.eclipse.jface.tests.wizards.WizardTestSuite.class,
		org.eclipse.jface.tests.labelProviders.DecoratingLabelProviderTests.class,
		org.eclipse.jface.tests.fieldassist.FieldAssistTestSuite.class, org.eclipse.jface.tests.window.AllTests.class,
		org.eclipse.jface.tests.resources.AllTests.class })
public class AllTests {

	public static void main(String[] args) {
		JUnitCore.main(AllTests.class.getName());
	}
}
