package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.preferences.DeprecatedFontPreferenceTestCase;
import org.eclipse.jface.tests.preferences.FontPreferenceTestCase;

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
		suite.addTest(new TestSuite(FontPreferenceTestCase.class));
		suite.addTest(new TestSuite(DeprecatedFontPreferenceTestCase.class));
		return suite;
	}
}
