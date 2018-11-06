/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jface.tests.viewers;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TreeSelectionTest.class, MultipleEqualElementsTreeViewerTest.class,
		LazySortedCollectionTest.class, TreeViewerTest.class, VirtualTreeViewerTest.class, SimpleTreeViewerTest.class,
		SimpleTableViewerTest.class, SimpleVirtualLazyTreeViewerTest.class, VirtualLazyTreeViewerTest.class,
		TableViewerTest.class, TreeViewerColumnTest.class, VirtualTableViewerTest.class,
		VirtualLazyTableViewerTest.class, TableColorProviderTest.class,
		TableFontProviderTest.class, ListViewerTest.class, CheckboxTableViewerTest.class,
		CheckboxTableViewerTest.DeprecatedConstructor.class, CheckboxTableViewerTest.FactoryMethod.class,
		CheckboxTreeViewerTest.class, ComboViewerTest.class, CComboViewerTest.class, TreeViewerComparatorTest.class,
		ListViewerComparatorTest.class, TableViewerComparatorTest.class, Bug138608Test.class,
		ComboViewerComparerTest.class, ListViewerRefreshTest.class, Bug200558Test.class, Bug201002TableViewerTest.class,
		Bug201002TreeViewerTest.class, Bug200337TableViewerTest.class, Bug203657TreeViewerTest.class,
		Bug203657TableViewerTest.class, Bug205700TreeViewerTest.class, Bug180504TableViewerTest.class,
		Bug180504TreeViewerTest.class, Bug256889TableViewerTest.class, Bug287765Test.class, Bug242231Test.class,
		StyledStringBuilderTest.class, TreeManagerTest.class })
public class AllTests {

	public static void main(String[] args) {
		JUnitCore.main(AllTests.class.getName());
	}

}
