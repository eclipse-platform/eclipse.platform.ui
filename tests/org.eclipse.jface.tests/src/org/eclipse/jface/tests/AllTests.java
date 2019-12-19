/*******************************************************************************
 * Copyright (c) 2018, 2019 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.jface.tests;

import org.eclipse.jface.tests.action.AllActionTests;
import org.eclipse.jface.tests.dialogs.AllDialogTests;
import org.eclipse.jface.tests.fieldassist.FieldAssistTestSuite;
import org.eclipse.jface.tests.images.AllImagesTests;
import org.eclipse.jface.tests.labelProviders.AllLabelProviderTests;
import org.eclipse.jface.tests.labelProviders.DecoratingLabelProviderTests;
import org.eclipse.jface.tests.layout.AllLayoutTests;
import org.eclipse.jface.tests.preferences.AllPrefsTests;
import org.eclipse.jface.tests.resources.AllResourcesTests;
import org.eclipse.jface.tests.viewers.AllViewersTests;
import org.eclipse.jface.tests.widgets.AllWidgetTests;
import org.eclipse.jface.tests.window.AllWindowTests;
import org.eclipse.jface.tests.wizards.WizardTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AllActionTests.class, AllDialogTests.class, AllImagesTests.class, AllLabelProviderTests.class,
		AllLayoutTests.class, AllPrefsTests.class, AllResourcesTests.class, AllViewersTests.class, AllWidgetTests.class,
		AllWindowTests.class, DecoratingLabelProviderTests.class, FieldAssistTestSuite.class, WizardTestSuite.class })
public class AllTests {

}
