/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import org.eclipse.ui.tests.compare.UIComparePreferencesAuto;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	UIDialogsAuto.class,
	DeprecatedUIDialogsAuto.class,
	UIWizardsAuto.class,
	DeprecatedUIWizardsAuto.class,
	UIPreferencesAuto.class,
	UIComparePreferencesAuto.class,
	DeprecatedUIPreferencesAuto.class,
	UIMessageDialogsAuto.class,
	UINewWorkingSetWizardAuto.class,
	UIEditWorkingSetWizardAuto.class,
	SearchPatternAuto.class,
	UIFilteredResourcesSelectionDialogAuto.class })
public class UIAutomatedSuite extends TestSuite {

}
