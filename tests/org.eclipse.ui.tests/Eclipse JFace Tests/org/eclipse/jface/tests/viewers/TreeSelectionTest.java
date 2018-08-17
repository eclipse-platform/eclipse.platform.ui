/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
 *     Mikael Barbero (Eclipse Foundation) - Bug 254570
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.tests.viewers.StructuredSelectionTest.EqualsHashCodeContractTestHelper;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;

import junit.framework.TestCase;

/**
 * @since 3.2
 *
 */
public class TreeSelectionTest extends TestCase {

	public TreeSelectionTest(String name) {
		super(name);
	}

	public void testNewWithEmptyTreePath() {
		assertNotNull(new TreeSelection(new TreePath(new Object[0])));
	}

	public void testBug1384558() {
		Object one = new Object();
		Object two = new Object();
		Object three = new Object();
		TreePath[] treePaths1 = new TreePath[3];
		treePaths1[0] = new TreePath(new Object[]{one, two});
		treePaths1[1] = new TreePath(new Object[]{one, three});
		treePaths1[2] = new TreePath(new Object[]{two, two});
		TreeSelection treeSelection1 = new TreeSelection(treePaths1);
		TreePath[] treePaths2 = new TreePath[2];
		treePaths2[0] = new TreePath(new Object[]{one, two});
		treePaths2[1] = new TreePath(new Object[]{one, three});
		TreeSelection treeSelection2 = new TreeSelection(treePaths2);
		// before fixing the bug, this threw an AIOOBE:
		assertFalse(treeSelection1.equals(treeSelection2));
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(treeSelection1, treeSelection2);
	}

	public void testEquals1() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection(), new TreeSelection());
	}

	public void testEquals2() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection((TreePath) null),
				new TreeSelection((TreePath) null));
	}

	public void testEquals3() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection((TreePath[]) null),
				new TreeSelection((TreePath[]) null));
	}

	public void testEquals4() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection(new TreePath[0]),
				new TreeSelection(new TreePath[0]));
	}

	public void testEquals5() {
		Object one = new Object();
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection(newTreePath(one)),
				new TreeSelection(newTreePath(one)));
	}

	public void testEquals6() {
		Object one = new Object();
		Object two = new Object();
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(new TreeSelection(newTreePath(one)),
				new TreeSelection(newTreePath(two)));
	}

	public void testEquals7() {
		Object one = new Object();
		Object two = new Object();
		Object three = new Object();
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection(newTreePath(one, two, three)),
				new TreeSelection(newTreePath(one, two, three)));
	}

	public void testEquals8() {
		Object one = new Object();
		Object two = new Object();
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(new TreeSelection(newTreePath(one, two)),
				new TreeSelection(newTreePath(two, one)));
	}

	public void testEquals9() {
		Object one = new Object();
		Object two = new Object();
		Object three = new Object();
		Object four = new Object();
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(
				new TreeSelection(newTreePaths(newTreePath(one, two), newTreePath(three, four))),
				new TreeSelection(newTreePaths(newTreePath(one, two), newTreePath(three, four))));
	}

	public void testEquals10() {
		Object one = new Object();
		Object two = new Object();
		Object three = new Object();
		Object four = new Object();
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(
				new TreeSelection(newTreePaths(newTreePath(one, two), newTreePath(three, four))),
				new TreeSelection(newTreePaths(newTreePath(three, four), newTreePath(one, two))));
	}

	public void testEquals11() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection(), new StructuredSelection());
	}

	public void testEquals12() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection((TreePath) null),
				new StructuredSelection());
	}

	public void testEquals13() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(new TreeSelection(newTreePath("element")),
				new StructuredSelection("element"));
	}

	public void testEquals14() {
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(new TreeSelection(newTreePath("element 1")),
				new StructuredSelection("element 2"));
	}

	public void testEquals15() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(
				new TreeSelection(newTreePath("element 1", "element 2")),
				new StructuredSelection(Arrays.asList("element 2")));
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(
				new TreeSelection(newTreePath("element 1", "element 2")),
				new StructuredSelection(new Object[] { "element 2", }));
	}

	public void testEquals16() {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(
				new TreeSelection(
						newTreePaths(newTreePath("element 1", "element 2"), newTreePath("element 3", "element 4"))),
				new StructuredSelection(Arrays.asList("element 2", "element 4")));
	}

	public void testEquals17() {
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(
				new TreeSelection(
						newTreePaths(newTreePath("element 1", "element 2"), newTreePath("element 3", "element 4"))),
				new StructuredSelection(Arrays.asList("element 4", "element 2")));
	}

	public void testEquals18() {
		doTestEquals18(StructuredSelectionTest.JAVA_LANG_OBJECT_COMPARER);
		doTestEquals18(StructuredSelectionTest.IDENTITY_COMPARER);
	}

	private void doTestEquals18(IElementComparer comparer) {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(
				new TreeSelection((TreePath) null, comparer), new StructuredSelection(new ArrayList<>(), comparer));
	}

	public void testEquals19() {
		doTestEquals19(StructuredSelectionTest.JAVA_LANG_OBJECT_COMPARER);
		doTestEquals19(StructuredSelectionTest.IDENTITY_COMPARER);
	}

	private void doTestEquals19(IElementComparer comparer) {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(
				new TreeSelection(newTreePath("element 1", "element 2"), comparer),
				new StructuredSelection(Arrays.asList("element 2"), comparer));
	}

	public void testEquals20() {
		doTestEquals20(StructuredSelectionTest.JAVA_LANG_OBJECT_COMPARER);
		doTestEquals20(StructuredSelectionTest.IDENTITY_COMPARER);
	}

	private void doTestEquals20(IElementComparer comparer) {
		EqualsHashCodeContractTestHelper.testExpectedEqualsObjects(
				new TreeSelection(
						newTreePaths(newTreePath("element 1", "element 2"), newTreePath("element 3", "element 4")),
						comparer),
				new StructuredSelection(Arrays.asList("element 2", "element 4"), comparer));
	}

	public void testEquals21() {
		doTestEquals21(StructuredSelectionTest.JAVA_LANG_OBJECT_COMPARER);
		doTestEquals21(StructuredSelectionTest.IDENTITY_COMPARER);
	}

	private void doTestEquals21(IElementComparer comparer) {
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(
				new TreeSelection(newTreePath("element 1"), comparer),
				new StructuredSelection(Arrays.asList("element 2"), comparer));
	}

	public void testEquals22() {
		doTestEquals22(StructuredSelectionTest.JAVA_LANG_OBJECT_COMPARER);
		doTestEquals22(StructuredSelectionTest.IDENTITY_COMPARER);
	}

	private void doTestEquals22(IElementComparer comparer) {
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(
				new TreeSelection(newTreePath("element 1"), comparer),
				new StructuredSelection(new ArrayList<>(), comparer));
	}

	public void testEquals23() {
		doTestEquals23(StructuredSelectionTest.JAVA_LANG_OBJECT_COMPARER);
		doTestEquals23(StructuredSelectionTest.IDENTITY_COMPARER);
	}

	private void doTestEquals23(IElementComparer comparer) {
		EqualsHashCodeContractTestHelper.testExpectedNotEqualsObjects(new TreeSelection((TreePath) null, comparer),
				new StructuredSelection(Arrays.asList("element"), comparer));
	}


	private static TreePath newTreePath(Object... args) {
		return new TreePath(args);
	}

	private static TreePath[] newTreePaths(TreePath... args) {
		return args;
	}
}
