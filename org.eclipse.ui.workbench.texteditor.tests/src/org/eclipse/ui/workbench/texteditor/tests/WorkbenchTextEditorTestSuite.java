/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ui.workbench.texteditor.quickdiff.tests.DiffTestSuite;

/**
 * Test Suite for org.eclipse.ui.workbench.texteditor.
 * 
 * @since 3.0
 */
public class WorkbenchTextEditorTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Suite for org.eclipse.ui.workbench.texteditor"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTest(DiffTestSuite.suite());
		suite.addTest(FindReplaceDialogTest.suite());
		//$JUnit-END$
		return suite;
	}
}
