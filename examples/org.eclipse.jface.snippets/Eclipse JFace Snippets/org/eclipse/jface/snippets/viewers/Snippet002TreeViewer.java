/*******************************************************************************
 * Copyright (c) 2006 - 2016 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars.Vogel <Lars.Vogel@vogella.com> - Bug 414565, 475361, 487940
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * A simple TreeViewer to demonstrate usage
 *
 */

public class Snippet002TreeViewer {
	private class MyContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((MyModel)inputElement).child.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if( element == null) {
				return null;
			}

			return ((MyModel)element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			return ((MyModel)element).child.size() > 0;
		}

	}

	public class MyModel {
		public MyModel parent;
		public List<MyModel> child = new ArrayList<>();
		public int counter;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		@Override
		public String toString() {
			String rv = "Item ";
			if( parent != null ) {
				rv = parent.toString() + ".";
			}

			rv += counter;

			return rv;
		}
	}

	public Snippet002TreeViewer(Shell shell) {
		final TreeViewer v = new TreeViewer(shell);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(new MyContentProvider());
		createColumn(v.getTree(), "Values");
		v.setInput(createModel());
	}

	public void createColumn(Tree tr, String text) {
		TreeColumn column = new TreeColumn(tr, SWT.NONE);
		column.setWidth(100);
		column.setText(text);
		tr.setHeaderVisible(true);
	}

	private MyModel createModel() {

		MyModel root = new MyModel(0,null);
		root.counter = 0;

		MyModel tmp;
		for( int i = 1; i < 10; i++ ) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for( int j = 1; j < i; j++ ) {
				tmp.child.add(new MyModel(j,tmp));
			}
		}

		return root;
	}

	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet002TreeViewer(shell);
		shell.open ();

		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose ();
	}
}
