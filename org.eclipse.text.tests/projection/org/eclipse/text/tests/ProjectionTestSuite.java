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

package org.eclipse.text.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * 
 * @since 3.0
 */
public class ProjectionTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Suite for org.eclipse.text - projection"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(ProjectionDocumentTest.class);
		suite.addTestSuite(ProjectionMappingTest.class);
		//$JUnit-END$
		return suite;
	}
}
