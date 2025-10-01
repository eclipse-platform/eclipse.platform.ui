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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests the status handling facility
 *
 * @since 3.3
 */
@Suite
@SelectClasses({
	StatusDialogManagerTest.class,
	LabelProviderWrapperTest.class,
	SupportTrayTest.class,
	WorkbenchStatusDialogManagerImplTest.class,
	WizardsStatusHandlingTestCase.class,
})
public class StatusHandlingTestSuite {
	//
}
