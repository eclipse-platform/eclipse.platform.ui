/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllFileSearchTests extends TestSuite {

	public static Test suite() {
		return new AllFileSearchTests();
	}

	public AllFileSearchTests() {
		
		TestSuite suite= new TestSuite();
		suite.addTest(AnnotationManagerTest.allTests());
		suite.addTest(FileSearchTests.allTests());
		suite.addTest(LineAnnotationManagerTest.allTests());
		suite.addTest(PositionTrackerTest.allTests());
		suite.addTest(ResultUpdaterTest.allTests());
		suite.addTest(SearchResultPageTest.allTests());
		suite.addTest(SortingTest.allTests());
		
		addTest(new JUnitSourceSetup(suite));
	}

}
