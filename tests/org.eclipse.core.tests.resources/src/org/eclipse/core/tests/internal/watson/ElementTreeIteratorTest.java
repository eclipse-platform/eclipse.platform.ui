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
package org.eclipse.core.tests.internal.watson;

import java.util.ArrayList;
import java.util.Stack;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/** 
 * Unit tests for <code>ElementTreeIterator</code>.
 */
public class ElementTreeIteratorTest extends WatsonTest {
	public ElementTreeIteratorTest() {
		super(null);
	}

	public ElementTreeIteratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		// do nothing
	}

	static void setupElementTree(ElementTree tree, int num) {
		final IElementTreeData data = new IElementTreeData() {
			public Object clone() {
				try {
					return super.clone();
				} catch (CloneNotSupportedException e) {
				}
				return null;
			}
		};
		IPath sol = Path.ROOT.append("sol");
		tree.createElement(sol, data);
		for (int p = 0; p < num; p++) {
			IPath proj = sol.append("proj" + p);
			tree.createElement(proj, data);
			for (int k = 0; k < num; k++) {
				IPath folder = proj.append("folder" + k);
				tree.createElement(folder, data);
				for (int c = 0; c < num; c++) {
					IPath file = folder.append("file" + c);
					tree.createElement(file, data);
				}
			}
		}
	}

	public void testConcurrentModification() {
		//the dining detectives problem
		ElementTree baseTree = new ElementTree();
		int n = 3;
		setupElementTree(baseTree, n);
		baseTree.immutable();
		final ElementTree tree = baseTree.newEmptyDelta();
		modifyTree(tree);
		final ArrayList elts = new ArrayList();
		final IElementContentVisitor visitor = new IElementContentVisitor() {
			public boolean visitElement(ElementTree tree, IPathRequestor requestor, Object info) {
				elts.add(info);
				return true;
			}
		};
		Thread reader = new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < 80000; i++) {
					new ElementTreeIterator(tree, Path.ROOT).iterate(visitor);
				}
			}
		}, "Holmes (reader)");
		Thread writer = new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < 1000; i++) {
					modifyTree(tree);
					recursiveDelete(tree, Path.ROOT);
					setupElementTree(tree, 3);
				}
			}
		}, "Doyle (writer)");

		reader.start();
		writer.start();
		//wait for both threads to finish
		try {
			reader.join();
			writer.join();
		} catch (InterruptedException e) {
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ElementTreeIteratorTest.class);
		return suite;
	}

	/**
	 * 
	 */
	protected void tearDown() throws Exception {
		//ElementTree tests don't use the CoreTest infrastructure
	}

	public void testContentIterator() {
		ElementTree tree = new ElementTree();
		int n = 3;
		setupElementTree(tree, n);
		final ArrayList elts = new ArrayList();
		IElementContentVisitor elementVisitor = new IElementContentVisitor() {
			public boolean visitElement(ElementTree tree, IPathRequestor requestor, Object info) {
				elts.add(requestor.requestPath());
				return true;
			}
		};
		new ElementTreeIterator(tree, Path.ROOT).iterate(elementVisitor);
		assertEquals("1", 2 + n + n * n + n * n * n, elts.size());

		elts.clear();
		IPath innerElement = Path.ROOT.append("sol").append("proj1");
		new ElementTreeIterator(tree, innerElement).iterate(elementVisitor);
		assertEquals("2", 1 + n + n * n, elts.size());
	}

	/**
	 * Method deleteChild.
	 * @param path
	 */
	private void recursiveDelete(ElementTree tree, IPath path) {
		IPath[] children = tree.getChildren(path);
		for (int i = 0; i < children.length; i++) {
			recursiveDelete(tree, children[i]);
		}
		tree.deleteElement(path);
	}

	protected void modifyTree(ElementTree tree) {
		class MyStack extends Stack {
			/**
			 * All serializable objects should have a stable serialVersionUID
			 */
			private static final long serialVersionUID = 1L;

			public void pushAll(Object[] array) {
				for (int i = 0; i < array.length; i++) {
					push(array[i]);
				}
			}
		}
		MyStack toModify = new MyStack();
		IPath[] children = tree.getChildren(Path.ROOT);
		toModify.pushAll(children);
		while (!toModify.isEmpty()) {
			IPath visit = (IPath) toModify.pop();
			tree.openElementData(visit);
			toModify.pushAll(tree.getChildren(visit));
		}
	}
}
