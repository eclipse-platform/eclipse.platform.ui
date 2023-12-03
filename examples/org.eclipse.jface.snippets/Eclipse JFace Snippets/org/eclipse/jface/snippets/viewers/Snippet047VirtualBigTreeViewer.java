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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
 */
public class Snippet047VirtualBigTreeViewer {

	private static final int MODEL_DEEP = 1;
	private static final int MODEL_MAX_CHILD_COUNT = 20_000;
	private static final int MODEL_MIN_CHILDS = 10;
	private static final Random RANDOM = new Random();
	private static AtomicLong rendered = new AtomicLong();
	private static AtomicLong updated = new AtomicLong();
	private static AtomicLong childcount = new AtomicLong();
	private final TreeViewer fViewer;
	private final Text fText;

	private class MyContentProvider implements ILazyTreeContentProvider {

		@Override
		public Object getParent(Object element) {

			return ((Node) element).getParent();
		}

		@Override
		public void updateChildCount(Object element, int currentChildCount) {

			childcount.incrementAndGet();
			int cc = ((Node) element).getChildCount();
			if (cc != currentChildCount) {
				fViewer.setChildCount(element, cc);
			}
		}

		@Override
		public void updateElement(Object parent, int index) {

			updated.incrementAndGet();
			Node element = ((Node) parent).getChild(index);
			fViewer.replace(parent, index, element);
			fViewer.setChildCount(element, element.getChildCount());
		}
	}

	public class Node {

		private Node[] fChildren;
		private final int fCounter;
		private final Node fParent;

		public Node(int counter, Node parent) {

			fCounter = counter;
			fParent = parent;
		}

		public Node getParent() {

			return fParent;
		}

		public Node[] getChildren() {

			return fChildren;
		}

		public Node getChild(int index) {

			if (fChildren == null) {
				return null;
			}
			return fChildren[index];
		}

		public int getChildCount() {

			if (fChildren == null) {
				return 0;
			}
			return fChildren.length;
		}

		@Override
		public String toString() {

			Node parent = getParent();
			String type = parent == null ? "Root" : parent.getParent() == null ? "Node" : "Leaf";
			return type + " " + this.fCounter + " of " + (parent == null ? "" : String.valueOf(parent.getChildCount()))
					+ " with " + getChildCount() + " childs";
		}
	}

	public Snippet047VirtualBigTreeViewer(Shell shell) {

		System.out.println("Create large model...");
		Node root = createModel();
		System.out.println("Show UI");
		fText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		fText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fText.setEnabled(false);
		fViewer = new TreeViewer(shell, SWT.VIRTUAL | SWT.BORDER);
		fViewer.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {

				rendered.incrementAndGet();
				return super.getText(element);
			}
		});
		fViewer.setContentProvider(new MyContentProvider());
		fViewer.setUseHashlookup(true);
		fViewer.setInput(root);
		fViewer.getTree().setLayoutData(GridDataFactory.fillDefaults().create());
		fViewer.setChildCount(root, root.getChildCount());
		fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// at this point the model only contains what is visible on the screen.
	}

	private Node createModel() {

		Node root = new Node(-1, null);
		createChilds(root, 0);
		return root;
	}

	private void createChilds(Node parent, int i) {

		if (i > MODEL_DEEP) {
			return;
		}
		int childs = RANDOM.nextInt(MODEL_MIN_CHILDS, MODEL_MAX_CHILD_COUNT);
		parent.fChildren = new Node[childs];
		for (int j = 0; j < childs; j++) {
			parent.fChildren[j] = new Node(j, parent);
			createChilds(parent.fChildren[j], i + 1);
		}
	}

	public static void main(String[] args) {

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					long last = 0;
					while (true) {
						TimeUnit.SECONDS.sleep(1);
						long current = rendered.get();
						if (current != last) {
							last = current;
							System.out.println("Total render requests: " + current + ", " + updated.get()
									+ " update elements, " + childcount.get() + " update childcounts");
						}
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		new Snippet047VirtualBigTreeViewer(shell);
		shell.setSize(800, 600);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}