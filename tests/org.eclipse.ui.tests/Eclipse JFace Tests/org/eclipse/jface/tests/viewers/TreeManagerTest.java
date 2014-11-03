/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ui.internal.dialogs.TreeManager;
import org.eclipse.ui.internal.dialogs.TreeManager.TreeItem;

public class TreeManagerTest extends TestCase {

	private static final List STATE_NAMES;

	private static final int CHECKSTATE_UNCHECKED = 0;
	private static final int CHECKSTATE_GRAY = 1;
	private static final int CHECKSTATE_CHECKED = 2;

	static {
		STATE_NAMES = new ArrayList();
		STATE_NAMES.add(CHECKSTATE_UNCHECKED, "unchecked");
		STATE_NAMES.add(CHECKSTATE_GRAY, "gray");
		STATE_NAMES.add(CHECKSTATE_CHECKED, "checked");
	}

	private static String getName(int checkstate) {
		return (String)STATE_NAMES.get(checkstate);
	}

	private static void assertState(TreeItem item, int expectedState) throws Exception {
		Field checkStateField = TreeItem.class.getDeclaredField("checkState");
		checkStateField.setAccessible(true);
		int checkState = checkStateField.getInt(item);

		assertEquals("For TreeItem " + item.getLabel() + ", expected " +
				getName(expectedState) + " but was " + getName(checkState) + ";",
				expectedState, checkState);
	}

	public void testSingleEntry() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem item = manager.new TreeItem("item");

		item.setCheckState(true);
		assertState(item, CHECKSTATE_CHECKED);

		item.setCheckState(true);
		assertState(item, CHECKSTATE_CHECKED);

		item.setCheckState(false);
		assertState(item, CHECKSTATE_UNCHECKED);

		item.setCheckState(true);
		assertState(item, CHECKSTATE_CHECKED);

		item.setCheckState(false);
		assertState(item, CHECKSTATE_UNCHECKED);

		item.setCheckState(false);
		assertState(item, CHECKSTATE_UNCHECKED);
	}

	public void testSingleChildAffectsParent() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem parent = manager.new TreeItem("parent");
		TreeItem child = manager.new TreeItem("child");
		parent.addChild(child);

		child.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		child.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		child.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);

		child.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		child.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);

		child.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);
	}

	public void testTwoChildrenAffectParent() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem parent = manager.new TreeItem("parent");
		TreeItem son = manager.new TreeItem("son");
		TreeItem daughter = manager.new TreeItem("daughter");
		parent.addChild(son);
		parent.addChild(daughter);

		son.setCheckState(true);
		daughter.setCheckState(false);
		assertState(parent, CHECKSTATE_GRAY);
		assertState(son, CHECKSTATE_CHECKED);
		assertState(daughter, CHECKSTATE_UNCHECKED);

		daughter.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(son, CHECKSTATE_CHECKED);
		assertState(daughter, CHECKSTATE_CHECKED);

		son.setCheckState(false);
		assertState(parent, CHECKSTATE_GRAY);
		assertState(son, CHECKSTATE_UNCHECKED);
		assertState(daughter, CHECKSTATE_CHECKED);

		daughter.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(son, CHECKSTATE_UNCHECKED);
		assertState(daughter, CHECKSTATE_UNCHECKED);
	}

	public void testCheckUncheckChildAt3Deep() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem grandparent = manager.new TreeItem("grandparent");
		TreeItem parent = manager.new TreeItem("parent");
		TreeItem child = manager.new TreeItem("child");
		grandparent.addChild(parent);
		parent.addChild(child);

		child.setCheckState(true);
		assertState(grandparent, CHECKSTATE_CHECKED);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		child.setCheckState(true);
		assertState(grandparent, CHECKSTATE_CHECKED);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		child.setCheckState(false);
		assertState(grandparent, CHECKSTATE_UNCHECKED);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);

		child.setCheckState(true);
		assertState(grandparent, CHECKSTATE_CHECKED);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		child.setCheckState(false);
		assertState(grandparent, CHECKSTATE_UNCHECKED);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);

		child.setCheckState(false);
		assertState(grandparent, CHECKSTATE_UNCHECKED);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);
	}

	public void testCauseGrayAt3Deep() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem grandparent = manager.new TreeItem("grandparent");
		TreeItem parent = manager.new TreeItem("parent");
		TreeItem child1 = manager.new TreeItem("child1");
		TreeItem child2 = manager.new TreeItem("child2");
		grandparent.addChild(parent);
		parent.addChild(child1);
		parent.addChild(child2);

		child1.setCheckState(true);
		child2.setCheckState(false);
		assertState(grandparent, CHECKSTATE_GRAY);
		assertState(parent, CHECKSTATE_GRAY);
		assertState(child1, CHECKSTATE_CHECKED);
		assertState(child2, CHECKSTATE_UNCHECKED);

		child2.setCheckState(true);
		assertState(grandparent, CHECKSTATE_CHECKED);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child1, CHECKSTATE_CHECKED);
		assertState(child2, CHECKSTATE_CHECKED);

		child1.setCheckState(false);
		assertState(grandparent, CHECKSTATE_GRAY);
		assertState(parent, CHECKSTATE_GRAY);
		assertState(child1, CHECKSTATE_UNCHECKED);
		assertState(child2, CHECKSTATE_CHECKED);

		child2.setCheckState(false);
		assertState(grandparent, CHECKSTATE_UNCHECKED);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child1, CHECKSTATE_UNCHECKED);
		assertState(child2, CHECKSTATE_UNCHECKED);
	}

	public void testCheckUncheckChildAt5Deep() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem items[] = new TreeItem[5];
		for(int i = 0; i < items.length; i++) {
			items[i] = manager.new TreeItem("item" + i);
			if(i > 0) {
				items[i-1].addChild(items[i]);
			}
		}

		items[4].setCheckState(true);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_CHECKED);
		}

		items[4].setCheckState(false);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_UNCHECKED);
		}
	}

	public void testCauseGrayAt5Deep() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem items[] = new TreeItem[4];
		for(int i = 0; i < items.length; i++) {
			items[i] = manager.new TreeItem("item" + i);
			if(i > 0) {
				items[i-1].addChild(items[i]);
			}
		}
		TreeItem child1 = manager.new TreeItem("child1");
		TreeItem child2 = manager.new TreeItem("child2");
		items[3].addChild(child1);
		items[3].addChild(child2);

		child1.setCheckState(true);
		child2.setCheckState(false);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_GRAY);
		}
		assertState(child1, CHECKSTATE_CHECKED);
		assertState(child2, CHECKSTATE_UNCHECKED);

		child2.setCheckState(true);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_CHECKED);
		}
		assertState(child1, CHECKSTATE_CHECKED);
		assertState(child2, CHECKSTATE_CHECKED);

		child1.setCheckState(false);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_GRAY);
		}
		assertState(child1, CHECKSTATE_UNCHECKED);
		assertState(child2, CHECKSTATE_CHECKED);

		child2.setCheckState(false);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_UNCHECKED);
		}
		assertState(child1, CHECKSTATE_UNCHECKED);
		assertState(child2, CHECKSTATE_UNCHECKED);
	}

	public void testChildrenInMultipleBranchesAffectAncestors() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem root = manager.new TreeItem("root");
		TreeItem itemA = manager.new TreeItem("itemA");
		TreeItem itemA1 = manager.new TreeItem("itemA1");
		TreeItem itemA2 = manager.new TreeItem("itemA2");
		TreeItem itemB = manager.new TreeItem("itemB");
		TreeItem itemB1 = manager.new TreeItem("itemB1");
		TreeItem itemB2 = manager.new TreeItem("itemB2");

		root.addChild(itemA);
		root.addChild(itemB);
		itemA.addChild(itemA1);
		itemA.addChild(itemA2);
		itemB.addChild(itemB1);
		itemB.addChild(itemB2);

		itemA1.setCheckState(true);
		itemA2.setCheckState(false);
		itemB1.setCheckState(false);
		itemB2.setCheckState(false);

		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_GRAY);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_UNCHECKED);
		assertState(itemB1, CHECKSTATE_UNCHECKED);
		assertState(itemB2, CHECKSTATE_UNCHECKED);

		itemB2.setCheckState(true);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_GRAY);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_GRAY);
		assertState(itemB1, CHECKSTATE_UNCHECKED);
		assertState(itemB2, CHECKSTATE_CHECKED);

		itemA1.setCheckState(false);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_UNCHECKED);
		assertState(itemA1, CHECKSTATE_UNCHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_GRAY);
		assertState(itemB1, CHECKSTATE_UNCHECKED);
		assertState(itemB2, CHECKSTATE_CHECKED);

		itemB1.setCheckState(true);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_UNCHECKED);
		assertState(itemA1, CHECKSTATE_UNCHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);
		assertState(itemB1, CHECKSTATE_CHECKED);
		assertState(itemB2, CHECKSTATE_CHECKED);

		itemA1.setCheckState(true);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_GRAY);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);
		assertState(itemB1, CHECKSTATE_CHECKED);
		assertState(itemB2, CHECKSTATE_CHECKED);

		itemA2.setCheckState(true);
		assertState(root, CHECKSTATE_CHECKED);
		assertState(itemA, CHECKSTATE_CHECKED);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_CHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);
		assertState(itemB1, CHECKSTATE_CHECKED);
		assertState(itemB2, CHECKSTATE_CHECKED);

		itemB2.setCheckState(false);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_CHECKED);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_CHECKED);
		assertState(itemB, CHECKSTATE_GRAY);
		assertState(itemB1, CHECKSTATE_CHECKED);
		assertState(itemB2, CHECKSTATE_UNCHECKED);

		itemA1.setCheckState(false);
		itemA2.setCheckState(false);
		itemB1.setCheckState(false);
		assertState(root, CHECKSTATE_UNCHECKED);
		assertState(itemA, CHECKSTATE_UNCHECKED);
		assertState(itemA1, CHECKSTATE_UNCHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_UNCHECKED);
		assertState(itemB1, CHECKSTATE_UNCHECKED);
		assertState(itemB2, CHECKSTATE_UNCHECKED);
	}

	public void testMultipleChildrenInOneBranchAffectParent() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem root = manager.new TreeItem("root");
		TreeItem items[] = new TreeItem[5];
		for(int i = 0; i < items.length; i++) {
			items[i] = manager.new TreeItem("child" + i);
			root.addChild(items[i]);
			items[i].setCheckState(false);
		}

		items[0].setCheckState(true);
		assertState(root, CHECKSTATE_GRAY);
		assertState(items[0], CHECKSTATE_CHECKED);
		assertState(items[1], CHECKSTATE_UNCHECKED);
		assertState(items[2], CHECKSTATE_UNCHECKED);
		assertState(items[3], CHECKSTATE_UNCHECKED);
		assertState(items[4], CHECKSTATE_UNCHECKED);

		items[1].setCheckState(true);
		assertState(root, CHECKSTATE_GRAY);
		assertState(items[0], CHECKSTATE_CHECKED);
		assertState(items[1], CHECKSTATE_CHECKED);
		assertState(items[2], CHECKSTATE_UNCHECKED);
		assertState(items[3], CHECKSTATE_UNCHECKED);
		assertState(items[4], CHECKSTATE_UNCHECKED);

		items[0].setCheckState(false);
		assertState(root, CHECKSTATE_GRAY);
		assertState(items[0], CHECKSTATE_UNCHECKED);
		assertState(items[1], CHECKSTATE_CHECKED);
		assertState(items[2], CHECKSTATE_UNCHECKED);
		assertState(items[3], CHECKSTATE_UNCHECKED);
		assertState(items[4], CHECKSTATE_UNCHECKED);

		items[2].setCheckState(true);
		items[3].setCheckState(true);
		assertState(root, CHECKSTATE_GRAY);
		assertState(items[0], CHECKSTATE_UNCHECKED);
		assertState(items[1], CHECKSTATE_CHECKED);
		assertState(items[2], CHECKSTATE_CHECKED);
		assertState(items[3], CHECKSTATE_CHECKED);
		assertState(items[4], CHECKSTATE_UNCHECKED);

		items[0].setCheckState(true);
		items[4].setCheckState(true);
		assertState(root, CHECKSTATE_CHECKED);
		assertState(items[0], CHECKSTATE_CHECKED);
		assertState(items[1], CHECKSTATE_CHECKED);
		assertState(items[2], CHECKSTATE_CHECKED);
		assertState(items[3], CHECKSTATE_CHECKED);
		assertState(items[4], CHECKSTATE_CHECKED);

		items[2].setCheckState(false);
		assertState(root, CHECKSTATE_GRAY);
		assertState(items[0], CHECKSTATE_CHECKED);
		assertState(items[1], CHECKSTATE_CHECKED);
		assertState(items[2], CHECKSTATE_UNCHECKED);
		assertState(items[3], CHECKSTATE_CHECKED);
		assertState(items[4], CHECKSTATE_CHECKED);

		items[0].setCheckState(false);
		items[1].setCheckState(false);
		items[3].setCheckState(false);
		items[4].setCheckState(false);
		assertState(root, CHECKSTATE_UNCHECKED);
		assertState(items[0], CHECKSTATE_UNCHECKED);
		assertState(items[1], CHECKSTATE_UNCHECKED);
		assertState(items[2], CHECKSTATE_UNCHECKED);
		assertState(items[3], CHECKSTATE_UNCHECKED);
		assertState(items[4], CHECKSTATE_UNCHECKED);
	}

	public void testParentAffectsSingleChild() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem parent = manager.new TreeItem("parent");
		TreeItem child = manager.new TreeItem("child");
		parent.addChild(child);

		parent.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		parent.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		parent.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);

		parent.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(child, CHECKSTATE_CHECKED);

		parent.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);

		parent.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(child, CHECKSTATE_UNCHECKED);
	}

	public void testParentAffectsTwoChildren() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem parent = manager.new TreeItem("parent");
		TreeItem son = manager.new TreeItem("son");
		TreeItem daughter = manager.new TreeItem("daughter");
		parent.addChild(son);
		parent.addChild(daughter);

		parent.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(son, CHECKSTATE_CHECKED);
		assertState(daughter, CHECKSTATE_CHECKED);

		parent.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(son, CHECKSTATE_CHECKED);
		assertState(daughter, CHECKSTATE_CHECKED);

		parent.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(son, CHECKSTATE_UNCHECKED);
		assertState(daughter, CHECKSTATE_UNCHECKED);

		parent.setCheckState(true);
		assertState(parent, CHECKSTATE_CHECKED);
		assertState(son, CHECKSTATE_CHECKED);
		assertState(daughter, CHECKSTATE_CHECKED);

		parent.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(son, CHECKSTATE_UNCHECKED);
		assertState(daughter, CHECKSTATE_UNCHECKED);

		parent.setCheckState(false);
		assertState(parent, CHECKSTATE_UNCHECKED);
		assertState(son, CHECKSTATE_UNCHECKED);
		assertState(daughter, CHECKSTATE_UNCHECKED);
	}

	public void testParentAffectsMultipleChildrenInOneBranch() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem root = manager.new TreeItem("root");
		TreeItem items[] = new TreeItem[5];
		for(int i = 0; i < items.length; i++) {
			items[i] = manager.new TreeItem("child" + i);
			root.addChild(items[i]);
			items[i].setCheckState(false);
		}

		root.setCheckState(true);
		assertState(root, CHECKSTATE_CHECKED);
		assertState(items[0], CHECKSTATE_CHECKED);
		assertState(items[1], CHECKSTATE_CHECKED);
		assertState(items[2], CHECKSTATE_CHECKED);
		assertState(items[3], CHECKSTATE_CHECKED);
		assertState(items[4], CHECKSTATE_CHECKED);

		root.setCheckState(false);
		assertState(root, CHECKSTATE_UNCHECKED);
		assertState(items[0], CHECKSTATE_UNCHECKED);
		assertState(items[1], CHECKSTATE_UNCHECKED);
		assertState(items[2], CHECKSTATE_UNCHECKED);
		assertState(items[3], CHECKSTATE_UNCHECKED);
		assertState(items[4], CHECKSTATE_UNCHECKED);
	}

	public void testParentAffectsDescendantsInMultipleBranches() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem root = manager.new TreeItem("root");
		TreeItem itemA = manager.new TreeItem("itemA");
		TreeItem itemA1 = manager.new TreeItem("itemA1");
		TreeItem itemA2 = manager.new TreeItem("itemA2");
		TreeItem itemB = manager.new TreeItem("itemB");
		TreeItem itemB1 = manager.new TreeItem("itemB1");
		TreeItem itemB2 = manager.new TreeItem("itemB2");

		root.addChild(itemA);
		root.addChild(itemB);
		itemA.addChild(itemA1);
		itemA.addChild(itemA2);
		itemB.addChild(itemB1);
		itemB.addChild(itemB2);

		root.setCheckState(true);
		assertState(root, CHECKSTATE_CHECKED);
		assertState(itemA, CHECKSTATE_CHECKED);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_CHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);
		assertState(itemB1, CHECKSTATE_CHECKED);
		assertState(itemB2, CHECKSTATE_CHECKED);

		root.setCheckState(false);
		assertState(root, CHECKSTATE_UNCHECKED);
		assertState(itemA, CHECKSTATE_UNCHECKED);
		assertState(itemA1, CHECKSTATE_UNCHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_UNCHECKED);
		assertState(itemB1, CHECKSTATE_UNCHECKED);
		assertState(itemB2, CHECKSTATE_UNCHECKED);
	}

	public void testCheckUncheckParentWithDescendants5Deep() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem items[] = new TreeItem[5];
		for(int i = 0; i < items.length; i++) {
			items[i] = manager.new TreeItem("item" + i);
			if(i > 0) {
				items[i-1].addChild(items[i]);
			}
		}

		items[0].setCheckState(true);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_CHECKED);
		}

		items[0].setCheckState(false);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_UNCHECKED);
		}
	}

	public void testChangeOnGray() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem root = manager.new TreeItem("root");
		TreeItem itemA = manager.new TreeItem("itemA");
		TreeItem itemA1 = manager.new TreeItem("itemA1");
		TreeItem itemA2 = manager.new TreeItem("itemA2");
		TreeItem itemB = manager.new TreeItem("itemB");

		root.addChild(itemA);
		root.addChild(itemB);
		itemA.addChild(itemA1);
		itemA.addChild(itemA2);

		root.setCheckState(true);
		assertState(root, CHECKSTATE_CHECKED);
		assertState(itemA, CHECKSTATE_CHECKED);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_CHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);

		itemA.setCheckState(false);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_UNCHECKED);
		assertState(itemA1, CHECKSTATE_UNCHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);

		itemA1.setCheckState(true);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_GRAY);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);

		itemA.setCheckState(false);
		assertState(root, CHECKSTATE_GRAY);
		assertState(itemA, CHECKSTATE_UNCHECKED);
		assertState(itemA1, CHECKSTATE_UNCHECKED);
		assertState(itemA2, CHECKSTATE_UNCHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);

		itemA.setCheckState(true);
		assertState(root, CHECKSTATE_CHECKED);
		assertState(itemA, CHECKSTATE_CHECKED);
		assertState(itemA1, CHECKSTATE_CHECKED);
		assertState(itemA2, CHECKSTATE_CHECKED);
		assertState(itemB, CHECKSTATE_CHECKED);
	}

	public void testCheckUncheckItemWithAncestorsAndDescendants() throws Exception {
		TreeManager manager = new TreeManager();
		TreeItem items[] = new TreeItem[5];
		for(int i = 0; i < items.length; i++) {
			items[i] = manager.new TreeItem("item" + i);
			if(i > 0) {
				items[i-1].addChild(items[i]);
			}
		}

		items[3].setCheckState(true);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_CHECKED);
		}

		items[3].setCheckState(false);
		for (TreeItem item : items) {
			assertState(item, CHECKSTATE_UNCHECKED);
		}
	}


}
