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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.tests.images.ImageRegistryTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	public static Test suite() {
		TestSuite suite= new TestSuite();
		suite.addTest(new TestSuite(TreeViewerTest.class));
		suite.addTest(new TestSuite(TableViewerTest.class));
		suite.addTest(new TestSuite(TableTreeViewerTest.class));
		suite.addTest(new TestSuite(ListViewerTest.class));
		suite.addTest(new TestSuite(CheckboxTableViewerTest.class));
		suite.addTest(new TestSuite(CheckboxTreeViewerTest.class));
		suite.addTest(new TestSuite(ComboViewerTest.class));
		suite.addTest(new TestSuite(ImageRegistryTest.class));
		return suite;
	}
}
