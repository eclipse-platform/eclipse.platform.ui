/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TableViewer to demonstrate usage of an ILazyContentProvider. You can
 * compare this snippet to the Snippet029VirtualTableViewer to see the small but
 * needed difference.
 *
 */
public class Snippet030VirtualLazyTableViewer {
	private class MyContentProvider implements ILazyContentProvider {
		private TableViewer viewer;
		private MyModel[] elements;

		public MyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.elements = (MyModel[]) newInput;
		}

		@Override
		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}

	}

	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public Snippet030VirtualLazyTableViewer(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.VIRTUAL);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(new MyContentProvider(v));
		v.setUseHashlookup(true);
		MyModel[] model = createModel();
		v.setInput(model);
		v.setItemCount(model.length); // This is the difference when using a
		// ILazyContentProvider

		v.getTable().setLinesVisible(true);
	}

	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10000];

		for (int i = 0; i < 10000; i++) {
			elements[i] = new MyModel(i);
		}

		return elements;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet030VirtualLazyTableViewer(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}