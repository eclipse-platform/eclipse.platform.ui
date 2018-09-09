/*******************************************************************************
 * Copyright (c) 2006, 2016 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486603
 *     Wim Jongman <wim.jongman@remainsoftware.com> - Overhaul without preloading model
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.Random;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple TreeViewer example to demonstrate the usage of an
 * ILazyContentProvider.
 *
 */
public class Snippet047VirtualLazyTreeViewer {

	private long fParentsLoaded = 0;

	private long fGlobalChildrenLoaded = 0;

	private TreeViewer fViewer;

	private Text fText;

	private class MyContentProvider implements ILazyTreeContentProvider {

		@Override
		public Object getParent(Object element) {
			return ((Node) element).getParent();
		}

		@Override
		public void updateChildCount(Object element, int currentChildCount) {
			System.out.println(element + " " + ((Node) element).getChildCount());
			fViewer.setChildCount(element, ((Node) element).getChildCount());
		}

		@Override
		public void updateElement(Object parent, int index) {
			Node element = ((Node) parent).getChild(index);
			fViewer.replace(parent, index, element);
			updateChildCount(element, -1);
			fText.setText("1 root, " + fParentsLoaded + " nodes and " + fGlobalChildrenLoaded + " leafs in memory...");
		}
	}

	public class Node {
		private int fChildCount;

		public int fChildrenLoaded;

		private Node[] fChildren;

		private int fCounter;

		private Node fParent;

		public Node(int counter, int childCount, Node parent) {
			fCounter = counter;
			fChildCount = childCount;
			fParent = parent;
		}

		public Node getParent() {
			return fParent;
		}

		public int getChildCount() {
			return fChildCount;
		}

		public Node[] getChildren() {
			if (fChildren == null) {
				fChildren = new Node[fChildCount];
			}
			return fChildren;
		}

		public Node getChild(int index) {
			if (getChildren()[index] != null) {
				return getChildren()[index];
			}

			Node leafNode = new Node(index, getRandomChildCount(), this);
			getChildren()[index] = leafNode;
			fViewer.update(leafNode.getParent(), null);
			if (leafNode.getParent().getParent() == null) {
				fParentsLoaded++;
			} else {
				fGlobalChildrenLoaded++;
			}
			fChildrenLoaded++;
			return leafNode;
		}

		@Override
		public String toString() {
			String type = getParent() == null ? "Root" : getParent().getParent() == null ? "Node" : "Leaf";
			return type + " " + this.fCounter + " of " + (getParent() == null ? "0" : getParent().getChildCount())
					+ " (" + fChildCount + "/" + fChildrenLoaded + ")";
		}
	}

	public Snippet047VirtualLazyTreeViewer(Shell shell) {
		fText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		fText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fText.setEnabled(false);

		fViewer = new TreeViewer(shell, SWT.VIRTUAL | SWT.BORDER);
		fViewer.setLabelProvider(new LabelProvider());
		fViewer.setContentProvider(new MyContentProvider());
		fViewer.setUseHashlookup(true);
		Node root = new Node(0, getRandomChildCount(), null);
		fViewer.setInput(root);
		fViewer.getTree().setLayoutData(GridDataFactory.fillDefaults().create());
		fViewer.setChildCount(root, root.getChildCount());
		fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// at this point the model only contains what is visible on the screen.
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		new Snippet047VirtualLazyTreeViewer(shell);
		shell.setSize(800, 600);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public static int getRandomChildCount() {
		return new Random().nextInt(1000) + 100;
	}
}