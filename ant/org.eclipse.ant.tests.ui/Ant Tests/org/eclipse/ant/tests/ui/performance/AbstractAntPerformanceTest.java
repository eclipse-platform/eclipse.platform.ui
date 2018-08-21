/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.performance;

import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * Abstract class for ant performance tests, ensures the test project is created and ready in the test workspace.
 * 
 * @since 3.5
 */
public abstract class AbstractAntPerformanceTest extends PerformanceTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.test.performance.PerformanceTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		AbstractAntUITest.assertProject();
	}
}
