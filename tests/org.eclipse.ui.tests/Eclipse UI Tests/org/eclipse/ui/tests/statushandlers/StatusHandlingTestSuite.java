/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the status handling facility
 * 
 * @since 3.3
 */
public class StatusHandlingTestSuite extends TestSuite {

	public StatusHandlingTestSuite() {
		addTest(new TestSuite(WizardsStatusHandlingTestCase.class));
		addTest(new TestSuite(StatusDialogManagerTest.class));
		addTest(new TestSuite(LabelProviderWrapperTest.class));
		addTest(new TestSuite(SupportTrayTest.class));
		addTest(new TestSuite(WorkbenchStatusDialogManagerImplTest.class));
	}

	public static Test suite() {
		return new StatusHandlingTestSuite();
	}
}
