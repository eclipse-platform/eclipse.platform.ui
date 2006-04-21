/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.timeeditor.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.2
 *
 */
public class CalendarControlTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.jface.examples.databinding.compositetable.timeeditor.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(DayModel_testGetColumnsForEvents.class);
		suite.addTestSuite(CalendarableModel_testInit.class);
		suite.addTestSuite(CalendarableModel_testRefreshResults.class);
		suite.addTestSuite(CalendarableModel_TestTimedFindMethods.class);
		//$JUnit-END$
		return suite;
	}

}
