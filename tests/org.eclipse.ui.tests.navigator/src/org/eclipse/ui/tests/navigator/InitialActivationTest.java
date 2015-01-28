/*******************************************************************************
 * Copyright (c) 2010,2015 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class InitialActivationTest extends NavigatorTestBase {

	public InitialActivationTest() {

		_navigatorInstanceId = TEST_VIEWER_INITIAL_ACTIVATION;
	}

	@Test
	public void testInitialActivationExpression() throws Exception {
		assertFalse(_contentService.isActive(TEST_CONTENT_INITIAL_ACTIVATION_FALSE));
		assertTrue(_contentService.isActive(TEST_CONTENT_INITIAL_ACTIVATION_TRUE));
	}


}
