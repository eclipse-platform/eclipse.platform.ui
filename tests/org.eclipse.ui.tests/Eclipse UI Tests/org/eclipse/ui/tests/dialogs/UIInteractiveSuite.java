/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.ui.tests.dialogs;

import org.eclipse.ui.tests.compare.UIComparePreferences;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all areas of the UI.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	UIPreferences.class,
	UIComparePreferences.class,
	DeprecatedUIPreferences.class,
	UIWizards.class,
	DeprecatedUIWizards.class,
	UIDialogs.class,
	DeprecatedUIDialogs.class,
	UIMessageDialogs.class,
	UIErrorDialogs.class,
	UIFilteredResourcesSelectionDialog.class,
		ResourcePathCopyTest.class,
})
public class UIInteractiveSuite {

}
