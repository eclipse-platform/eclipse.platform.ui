/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.intro;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.0
 */
public class IntroTestSuite extends TestSuite {

	public static Test suite() {
		return new IntroTestSuite();
	}

	/**
	 *
	 */
	public IntroTestSuite() {
		addTest(new TestSuite(IntroPartTest.class));
		addTest(new TestSuite(NoIntroPartTest.class));
		addTest(new TestSuite(IntroTest.class));
	}
}
