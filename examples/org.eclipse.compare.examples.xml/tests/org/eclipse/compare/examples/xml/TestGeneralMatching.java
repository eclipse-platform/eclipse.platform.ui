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
package org.eclipse.compare.examples.xml;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.compare.examples.xml.GeneralMatching;
import org.eclipse.compare.examples.xml.XMLChildren;
import org.eclipse.compare.examples.xml.AbstractMatching.Match;
import org.eclipse.jface.text.Document;

/**
 * Runs General Matching algorithm in GeneralMatching.java, which uses a distance table
 * which is filled using the min cost bipartite matching algorithm
 */
public class TestGeneralMatching extends TestCase {

	GeneralMatching fGM;
	Document fdoc;

	class Pair {
		int fx;
		int fy;

		Pair(int x, int y) {
			fx= x;
			fy= y;
		}

		public boolean equals(Object obj) {
			if (obj instanceof Pair) {
				Pair p= (Pair) obj;
				return fx == p.fx && fy == p.fy;
			}
			return false;
		}

		public String toString() {
			return "(" + fx + "," + fy + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public TestGeneralMatching(String name) {
		super(name);
	}
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
		//TestRunner.run(suite());
	}

	protected void setUp() {
		System.out.println("TestGeneralMatching.name()==" + getName()); //$NON-NLS-1$
		fGM= new GeneralMatching();
		fdoc= new Document();
	}

	protected void tearDown() throws Exception {
		//remove set-up
	}

	public static Test suite() {
		return new TestSuite(TestGeneralMatching.class);
	}

	//General case without ids
	public void test0() {
		XMLChildren LeftTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		XMLChildren RightTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		//create Left Tree
		XMLChildren parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"a_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setName(Integer.toString(1));
		LeftTree.addChild(parent);
		parent.setParent(LeftTree);
		XMLChildren current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(2));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(3));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b1_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b1" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(4));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[2]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(5));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[3]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(6));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(7));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		XMLNode attr=
			new XMLNode(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"attr", //$NON-NLS-1$
				"hello", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "attr" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		attr.setName(Integer.toString(8));
		parent.addChild(attr);
		attr.setParent(parent);

		//create Right Tree
		parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"a_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setName(Integer.toString(1));
		RightTree.addChild(parent);
		parent.setParent(RightTree);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(2));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(3));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		attr=
			new XMLNode(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"attr", //$NON-NLS-1$
				"hello", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "attr" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		attr.setName(Integer.toString(4));
		parent.addChild(attr);
		attr.setParent(parent);
		parent= (XMLChildren) parent.getParent().getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[2]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(5));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[3]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(6));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(7));
		parent.addChild(current);
		current.setParent(parent);

		//run matching algorithm
		try {
			fGM.match(LeftTree, RightTree, false, null);
		} catch (InterruptedException e) {
		}
		Vector expected= new Vector(8);
		expected.add(new Pair(0, 0));
		expected.add(new Pair(1, 1));
		expected.add(new Pair(6, 2));
		expected.add(new Pair(5, 5));
		expected.add(new Pair(2, 6));
		expected.add(new Pair(4, -1));
		expected.add(new Pair(7, 3));
		expected.add(new Pair(3, 7));
		expected.add(new Pair(8, 4));
		Vector Matches= fGM.getMatches();
		Vector MatchingPairs= new Vector();
		for (ListIterator it_M= Matches.listIterator(); it_M.hasNext();) {
			Match m= (Match) it_M.next();
			MatchingPairs.add(
				new Pair(
					(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
					(m.fy == null) ? -1 : Integer.parseInt(m.fy.getName())));
		}
		//		for (Enumeration enum = MatchingPairs.elements(); enum.hasMoreElements(); ) {
		//			System.out.print(enum.nextElement() + " ");
		//		}
		//		System.out.println();
		assertTrue(expected.size() == MatchingPairs.size());
		for (Enumeration enum= expected.elements(); enum.hasMoreElements();) {
			assertTrue(MatchingPairs.contains(enum.nextElement()));
		}
	}

	//Simulate plugin.xml with ids
	public void test1() {
		XMLChildren LeftTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		LeftTree.setName(Integer.toString(0));
		XMLChildren RightTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		RightTree.setName(Integer.toString(0));
		int numbering= 1;
		//create Left Tree
		XMLChildren parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setUsesIDMAP(true);
		parent.setName(Integer.toString(numbering++));
		LeftTree.addChild(parent);
		parent.setParent(LeftTree);
		XMLChildren current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"id", //$NON-NLS-1$
				"org.eclipse.ui", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"runtime<[1]", //$NON-NLS-1$
				"runtime<[1]", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"runtime<[2]", //$NON-NLS-1$
				"runtime<[2]", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"library<workbench.jar", //$NON-NLS-1$
				"library<workbench.jar", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "library" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"export<*", //$NON-NLS-1$
				"export<*", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "library" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "export" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent().getParent();
		//parent is now plugin
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<importWizards", //$NON-NLS-1$
				"extension-point<importWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "library" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "export" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<popupMenus", //$NON-NLS-1$
				"extension-point<popupMenus", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension<org.eclipse.ui.newWizards", //$NON-NLS-1$
				"extension<org.eclipse.ui.newWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current= parent;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"wizard<org.eclipse.ui.wizards.new.project.addedsomething", //$NON-NLS-1$
				"wizard<org.eclipse.ui.wizards.new.project.addedsomething", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "wizard" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"wizard<org.eclipse.ui.wizards.new.folder", //$NON-NLS-1$
				"wizard<org.eclipse.ui.wizards.new.folder", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "wizard" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();

		//showNodeNames(LeftTree);

		//create Right Tree
		numbering= 1;
		parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setUsesIDMAP(true);
		parent.setName(Integer.toString(numbering++));
		RightTree.addChild(parent);
		parent.setParent(RightTree);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"id", //$NON-NLS-1$
				"org.eclipse.ui", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"runtime<[1]", //$NON-NLS-1$
				"runtime<[1]", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"library<workbench.jar", //$NON-NLS-1$
				"library<workbench.jar", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "library" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"export<*", //$NON-NLS-1$
				"export<*", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "library" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "export" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent().getParent();
		//parent is now plugin
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<popupMenus", //$NON-NLS-1$
				"extension-point<popupMenus", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<exportWizards", //$NON-NLS-1$
				"extension-point<importWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "library" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "export" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension<org.eclipse.ui.newWizards", //$NON-NLS-1$
				"extension<org.eclipse.ui.newWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current= parent;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"wizard<org.eclipse.ui.wizards.new.project", //$NON-NLS-1$
				"wizard<org.eclipse.ui.wizards.new.project.addedsomething", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "wizard" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"wizard<org.eclipse.ui.wizards.new.folder", //$NON-NLS-1$
				"wizard<org.eclipse.ui.wizards.new.folder", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "wizard" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();

		//showNodeNames(RightTree);

		//run matching algorithm
		try {
			fGM.match(LeftTree, RightTree, false, null);
		} catch (InterruptedException e) {
		}
		Vector expected= new Vector();
		expected.add(new Pair(0, 0));
		expected.add(new Pair(1, 1));
		expected.add(new Pair(8, 6));
		expected.add(new Pair(9, 8));
		expected.add(new Pair(11, 10));
		expected.add(new Pair(2, 2));
		expected.add(new Pair(4, 3));
		expected.add(new Pair(3, -1));
		expected.add(new Pair(5, 4));
		expected.add(new Pair(6, 5));
		Vector Matches= fGM.getMatches();
		Vector MatchingPairs= new Vector();
		for (ListIterator it_M= Matches.listIterator(); it_M.hasNext();) {
			Match m= (Match) it_M.next();
			MatchingPairs.add(
				new Pair(
					(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
					(m.fy == null) ? -1 : Integer.parseInt(m.fy.getName())));
		}
		//		for (Enumeration enum = MatchingPairs.elements(); enum.hasMoreElements(); ) {
		//			System.out.print(enum.nextElement() + " ");
		//		}
		//		System.out.println();
		assertTrue(expected.size() == MatchingPairs.size());
		for (Enumeration enum= expected.elements(); enum.hasMoreElements();) {
			assertTrue(MatchingPairs.contains(enum.nextElement()));
		}
	}

	//Three-way compare, general case without ids
	public void test2() {
		XMLChildren AncestorTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		XMLChildren LeftTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		XMLChildren RightTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		int numbering= 1;
		//create Ancestor Tree
		XMLChildren parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"a_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setName(Integer.toString(numbering++));
		AncestorTree.addChild(parent);
		parent.setParent(AncestorTree);
		XMLChildren current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[2]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		XMLNode attr=
			new XMLNode(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"attr", //$NON-NLS-1$
				"world", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "attr" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		attr.setName(Integer.toString(numbering++));
		parent.addChild(attr);
		attr.setParent(parent);

		//create Left Tree
		numbering= 1;
		parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"a_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setName(Integer.toString(numbering++));
		LeftTree.addChild(parent);
		parent.setParent(LeftTree);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[2]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		attr=
			new XMLNode(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"attr", //$NON-NLS-1$
				"hello", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "attr" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		attr.setName(Integer.toString(numbering++));
		parent.addChild(attr);
		attr.setParent(parent);

		//create Right Tree
		numbering= 1;
		parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"a_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setName(Integer.toString(numbering++));
		RightTree.addChild(parent);
		parent.setParent(RightTree);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b1_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"e_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[2]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		attr=
			new XMLNode(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"attr", //$NON-NLS-1$
				"world", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "attr" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		attr.setName(Integer.toString(numbering++));
		parent.addChild(attr);
		attr.setParent(parent);

		//run matching algorithm
		try {
			fGM.match(LeftTree, RightTree, false, null);
			Vector LRMatches= fGM.getMatches();
			Vector LRMatchingPairs= new Vector();
			for (ListIterator it_M= LRMatches.listIterator();
				it_M.hasNext();
				) {
				Match m= (Match) it_M.next();
				LRMatchingPairs.add(
					new Pair(
						(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
						(m.fy == null)
							? -1
							: Integer.parseInt(m.fy.getName())));
			}
			fGM.match(LeftTree, AncestorTree, true, null);
			Vector LAMatches= fGM.getMatches();
			Vector LAMatchingPairs= new Vector();
			for (ListIterator it_M= LAMatches.listIterator();
				it_M.hasNext();
				) {
				Match m= (Match) it_M.next();
				LAMatchingPairs.add(
					new Pair(
						(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
						(m.fy == null)
							? -1
							: Integer.parseInt(m.fy.getName())));

			}
			fGM.match(RightTree, AncestorTree, true, null);
			Vector RAMatches= fGM.getMatches();
			Vector RAMatchingPairs= new Vector();
			for (ListIterator it_M= RAMatches.listIterator();
				it_M.hasNext();
				) {
				Match m= (Match) it_M.next();
				RAMatchingPairs.add(
					new Pair(
						(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
						(m.fy == null)
							? -1
							: Integer.parseInt(m.fy.getName())));
			}
			Vector LRexpected= new Vector();
			LRexpected.add(new Pair(0, 0));
			LRexpected.add(new Pair(1, 1));
			LRexpected.add(new Pair(2, 2));
			LRexpected.add(new Pair(-1, 4));
			LRexpected.add(new Pair(4, 6));
			LRexpected.add(new Pair(3, 3));
			LRexpected.add(new Pair(5, 7));
			Vector LAexpected= new Vector();
			LAexpected.add(new Pair(0, 0));
			LAexpected.add(new Pair(1, 1));
			LAexpected.add(new Pair(2, 2));
			LAexpected.add(new Pair(3, 3));
			LAexpected.add(new Pair(4, 4));
			LAexpected.add(new Pair(5, 5));
			Vector RAexpected= new Vector();
			RAexpected.add(new Pair(0, 0));
			RAexpected.add(new Pair(1, 1));
			RAexpected.add(new Pair(2, 2));
			RAexpected.add(new Pair(6, 4));
			RAexpected.add(new Pair(4, -1));
			RAexpected.add(new Pair(3, 3));
			RAexpected.add(new Pair(7, 5));

			assertTrue(LRexpected.size() == LRMatchingPairs.size());
			for (Enumeration enum= LRexpected.elements();
				enum.hasMoreElements();
				) {
				assertTrue(LRMatchingPairs.contains(enum.nextElement()));
			}
			assertTrue(LAexpected.size() == LAMatchingPairs.size());
			for (Enumeration enum= LAexpected.elements();
				enum.hasMoreElements();
				) {
				assertTrue(LAMatchingPairs.contains(enum.nextElement()));
			}
			assertTrue(RAexpected.size() == RAMatchingPairs.size());
			for (Enumeration enum= RAexpected.elements();
				enum.hasMoreElements();
				) {
				assertTrue(RAMatchingPairs.contains(enum.nextElement()));
			}
		} catch (InterruptedException e) {
		}

	}

	//Three-way compare of plugin.xml with ids
	public void test3() {
		XMLChildren AncestorTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		AncestorTree.setName(Integer.toString(0));
		XMLChildren LeftTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		LeftTree.setName(Integer.toString(0));
		XMLChildren RightTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		RightTree.setName(Integer.toString(0));
		int numbering= 1;
		//create Ancestor Tree
		XMLChildren parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setUsesIDMAP(true);
		parent.setName(Integer.toString(numbering++));
		AncestorTree.addChild(parent);
		parent.setParent(AncestorTree);
		XMLChildren current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"id", //$NON-NLS-1$
				"org.eclipse.ui", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		//parent is plugin
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<importWizards", //$NON-NLS-1$
				"extension-point<importWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "library" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "export" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<editorActions", //$NON-NLS-1$
				"extension-point<editorActions", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<popupMenus", //$NON-NLS-1$
				"extension-point<popupMenus", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<exportWizards", //$NON-NLS-1$
				"extension-point<exportWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);

		//create LeftTree Tree
		numbering= 1;
		parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setUsesIDMAP(true);
		parent.setName(Integer.toString(numbering++));
		LeftTree.addChild(parent);
		parent.setParent(LeftTree);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"id", //$NON-NLS-1$
				"org.eclipse.ui", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		//parent is plugin
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<editorActions", //$NON-NLS-1$
				"extension-point<editorActions", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<popupMenus", //$NON-NLS-1$
				"extension-point<popupMenus", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<exportWizards", //$NON-NLS-1$
				"extension-point<exportWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);

		//create RightTree Tree
		numbering= 1;
		parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				"plugin" + XMLStructureCreator.ID_SEPARATOR + "org.eclipse.ui", //$NON-NLS-1$ //$NON-NLS-2$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setUsesIDMAP(true);
		parent.setName(Integer.toString(numbering++));
		RightTree.addChild(parent);
		parent.setParent(RightTree);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ATTRIBUTE,
				"id", //$NON-NLS-1$
				"org.eclipse.ui", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "runtime" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ATTRIBUTE),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		//parent is plugin
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<editorActions", //$NON-NLS-1$
				"extension-point<editorActions", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<popupMenus", //$NON-NLS-1$
				"extension-point<popupMenus", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"extension-point<importWizards", //$NON-NLS-1$
				"extension-point<importWizards", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "plugin" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "extension-point" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setUsesIDMAP(true);
		current.setName(Integer.toString(numbering++));
		parent.addChild(current);
		current.setParent(parent);

		//run matching algorithm
		try {
			fGM.match(LeftTree, RightTree, false, null);
			Vector LRMatches= fGM.getMatches();
			Vector LRMatchingPairs= new Vector();
			for (ListIterator it_M= LRMatches.listIterator();
				it_M.hasNext();
				) {
				Match m= (Match) it_M.next();
				LRMatchingPairs.add(
					new Pair(
						(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
						(m.fy == null)
							? -1
							: Integer.parseInt(m.fy.getName())));
			}
			fGM.match(LeftTree, AncestorTree, true, null);
			Vector LAMatches= fGM.getMatches();
			Vector LAMatchingPairs= new Vector();
			for (ListIterator it_M= LAMatches.listIterator();
				it_M.hasNext();
				) {
				Match m= (Match) it_M.next();
				LAMatchingPairs.add(
					new Pair(
						(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
						(m.fy == null)
							? -1
							: Integer.parseInt(m.fy.getName())));
			}
			fGM.match(RightTree, AncestorTree, true, null);
			Vector RAMatches= fGM.getMatches();
			Vector RAMatchingPairs= new Vector();
			for (ListIterator it_M= RAMatches.listIterator();
				it_M.hasNext();
				) {
				Match m= (Match) it_M.next();
				RAMatchingPairs.add(
					new Pair(
						(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
						(m.fy == null)
							? -1
							: Integer.parseInt(m.fy.getName())));
			}
			Vector LRexpected= new Vector();
			LRexpected.add(new Pair(0, 0));
			LRexpected.add(new Pair(1, 1));
			LRexpected.add(new Pair(2, 2));
			LRexpected.add(new Pair(3, 3));
			LRexpected.add(new Pair(4, 4));
			Vector LAexpected= new Vector();
			LAexpected.add(new Pair(0, 0));
			LAexpected.add(new Pair(1, 1));
			LAexpected.add(new Pair(2, 2));
			LAexpected.add(new Pair(3, 4));
			LAexpected.add(new Pair(4, 5));
			LAexpected.add(new Pair(5, 6));
			Vector RAexpected= new Vector();
			RAexpected.add(new Pair(0, 0));
			RAexpected.add(new Pair(1, 1));
			RAexpected.add(new Pair(2, 2));
			RAexpected.add(new Pair(3, 4));
			RAexpected.add(new Pair(4, 5));
			RAexpected.add(new Pair(5, 3));

			assertTrue(LRexpected.size() == LRMatchingPairs.size());
			for (Enumeration enum= LRexpected.elements();
				enum.hasMoreElements();
				) {
				assertTrue(LRMatchingPairs.contains(enum.nextElement()));
			}
			assertTrue(LAexpected.size() == LAMatchingPairs.size());
			for (Enumeration enum= LAexpected.elements();
				enum.hasMoreElements();
				) {
				assertTrue(LAMatchingPairs.contains(enum.nextElement()));
			}
			assertTrue(RAexpected.size() == RAMatchingPairs.size());
			for (Enumeration enum= RAexpected.elements();
				enum.hasMoreElements();
				) {
				assertTrue(RAMatchingPairs.contains(enum.nextElement()));
			}
		} catch (InterruptedException e) {
		}
	}

	public void test4() {
		ArrayList Ordered= new ArrayList();
		Ordered.add("root>a>b>"); //$NON-NLS-1$
		Ordered.add("root>a>b>c>"); //$NON-NLS-1$
		fGM= new GeneralMatching(Ordered);
		int i= 1;
		XMLChildren LeftTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		XMLChildren RightTree=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				XMLStructureCreator.ROOT_ID,
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		//create Left Tree
		XMLChildren parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"a_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setName(Integer.toString(i++));
		LeftTree.addChild(parent);
		parent.setParent(LeftTree);
		XMLChildren current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c1_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c1" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c2_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c2" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c3_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c3" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[2]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"d1_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "d1" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"d2_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "d2" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"d3_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "d3" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent().getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[3]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);

		//create Right Tree
		i= 1;
		parent=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"a_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		parent.setName(Integer.toString(i++));
		RightTree.addChild(parent);
		parent.setParent(RightTree);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c1_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c1" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c2_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c2" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c4_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c4" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c3_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c3" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[2]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"c_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= current;
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"d1_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "d1" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"d3_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "d3" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"d2_[1]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "c" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "d2" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);
		parent= (XMLChildren) parent.getParent().getParent();
		current=
			new XMLChildren(
				XMLStructureCreator.TYPE_ELEMENT,
				"b_[3]", //$NON-NLS-1$
				"", //$NON-NLS-1$
				(XMLStructureCreator.ROOT_ID
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "a" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ "b" //$NON-NLS-1$
					+ XMLStructureCreator.SIGN_SEPARATOR
					+ XMLStructureCreator.SIGN_ELEMENT),
				fdoc,
				0,
				0);
		current.setName(Integer.toString(i++));
		parent.addChild(current);
		current.setParent(parent);

		//run matching algorithm
		try {
			fGM.match(LeftTree, RightTree, false, null);
		} catch (InterruptedException e) {
		}
		Vector expected= new Vector(8);
		expected.add(new Pair(0, 0));
		expected.add(new Pair(1, 1));
		expected.add(new Pair(2, 2));
		expected.add(new Pair(6, 7));
		expected.add(new Pair(11, 12));
		expected.add(new Pair(3, 3));
		expected.add(new Pair(4, 4));
		expected.add(new Pair(-1, 5));
		expected.add(new Pair(5, 6));
		expected.add(new Pair(7, 8));
		expected.add(new Pair(8, 9));
		expected.add(new Pair(-1, 10));
		expected.add(new Pair(9, 11));
		expected.add(new Pair(10, -1));
		Vector Matches= fGM.getMatches();
		Vector MatchingPairs= new Vector();
		for (ListIterator it_M= Matches.listIterator(); it_M.hasNext();) {
			Match m= (Match) it_M.next();
			MatchingPairs.add(
				new Pair(
					(m.fx == null) ? -1 : Integer.parseInt(m.fx.getName()),
					(m.fy == null) ? -1 : Integer.parseInt(m.fy.getName())));
			//			System.out.println("("+ ((m.fx==null)?-1:Integer.parseInt(m.fx.getName()))+","+ ((m.fy==null)?-1:Integer.parseInt(m.fy.getName())) +")");
		}
		//		for (Enumeration enum = MatchingPairs.elements(); enum.hasMoreElements(); ) {
		//			System.out.print(enum.nextElement() + " ");
		//		}
		//		System.out.println();
		assertTrue(expected.size() == MatchingPairs.size());
		for (Enumeration enum= expected.elements(); enum.hasMoreElements();) {
			assertTrue(MatchingPairs.contains(enum.nextElement()));
		}
	}

	protected void showNodeNames(XMLNode root) {
		if (root != null) {
			System.out.print(root.getName() + ", "); //$NON-NLS-1$
			Object[] children= root.getChildren();
			if (children != null) {
				for (int i= 0; i < children.length; i++)
					showNodeNames((XMLNode) children[i]);
			}
		}
	}
}
