/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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

package org.eclipse.ui.tests.statushandlers;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests the status handling facility
 *
 * @since 3.3
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	StatusDialogManagerTest.class,
	LabelProviderWrapperTest.class,
	SupportTrayTest.class,
	WorkbenchStatusDialogManagerImplTest.class
		// TODO test fails in Gerrit, but passes locally
		// I think it needs a "org.eclipse.sdk.ide" product to pass
		// WizardsStatusHandlingTestCase.class,
})
public class StatusHandlingTestSuite {
	//
}
