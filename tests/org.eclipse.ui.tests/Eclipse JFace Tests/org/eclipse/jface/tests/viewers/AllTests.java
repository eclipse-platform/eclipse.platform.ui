/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		return new AllTests();
	}

	public AllTests() {
		addTestSuite(TreeSelectionTest.class);
		addTestSuite(MultipleEqualElementsTreeViewerTest.class);
		addTestSuite(LazySortedCollectionTest.class);
		addTestSuite(TreeViewerTest.class);
		addTestSuite(VirtualTreeViewerTest.class);
		addTestSuite(SimpleTreeViewerTest.class);
		addTestSuite(SimpleTableViewerTest.class);
		addTestSuite(SimpleVirtualLazyTreeViewerTest.class);
		addTestSuite(VirtualLazyTreeViewerTest.class);
		addTestSuite(TableViewerTest.class);
		addTestSuite(TreeViewerColumnTest.class);
		addTestSuite(VirtualTableViewerTest.class);
		addTestSuite(VirtualLazyTableViewerTest.class);
		addTestSuite(TableTreeViewerTest.class);
		addTestSuite(TableColorProviderTest.class);
		addTestSuite(TableFontProviderTest.class);
		addTestSuite(ListViewerTest.class);
		addTestSuite(CheckboxTableViewerTest.class);
		addTestSuite(CheckboxTableViewerTest.DeprecatedConstructor.class);
		addTestSuite(CheckboxTableViewerTest.FactoryMethod.class);
		addTestSuite(CheckboxTreeViewerTest.class);
		addTestSuite(ComboViewerTest.class);
		addTestSuite(CComboViewerTest.class);
		addTestSuite(TreeViewerComparatorTest.class);
		addTestSuite(ListViewerComparatorTest.class);
		addTestSuite(TableViewerComparatorTest.class);
		addTestSuite(Bug138608Test.class);
		addTestSuite(ComboViewerComparerTest.class);
		addTestSuite(ListViewerRefreshTest.class);
		addTestSuite(Bug200558Test.class);
		addTestSuite(Bug201002TableViewerTest.class);
		addTestSuite(Bug201002TreeViewerTest.class);
		addTestSuite(Bug200337TableViewerTest.class);
		addTestSuite(Bug203657TreeViewerTest.class);
		addTestSuite(Bug203657TableViewerTest.class);
		addTestSuite(Bug205700TreeViewerTest.class);
		addTestSuite(Bug180504TableViewerTest.class);
		addTestSuite(Bug180504TreeViewerTest.class);
		addTestSuite(Bug256889TableViewerTest.class);
		addTestSuite(Bug287765Test.class);
		addTestSuite(Bug242231Test.class);
		addTestSuite(StyledStringBuilderTest.class);
		addTestSuite(TreeManagerTest.class);
	}
}
