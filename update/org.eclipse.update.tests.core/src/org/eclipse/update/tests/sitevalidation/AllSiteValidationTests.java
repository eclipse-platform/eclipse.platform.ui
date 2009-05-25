/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.sitevalidation;

import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllSiteValidationTests extends UpdateManagerTestCase {
	public AllSiteValidationTests(String name) {
		super(name);
	}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.setName("Site Validation Tests");

		// the following will take all teh test methods in teh class that start with 'test'

		suite.addTest(new TestSuite(TestSiteValidation.class));

		return suite;
	}
}
