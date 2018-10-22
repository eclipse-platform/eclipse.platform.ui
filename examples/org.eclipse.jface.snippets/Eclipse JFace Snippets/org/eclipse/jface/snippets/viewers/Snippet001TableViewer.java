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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565, 487940
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A simple TableViewer implementation to demonstrate its usage
 */
public class Snippet001TableViewer {

	private class MyContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return (MyModel[]) inputElement;
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

	public Snippet001TableViewer(Shell shell) {
		final TableViewer v = new TableViewer(shell);
		v.setLabelProvider(new LabelProvider());
		// for demonstration purposes use custom content provider
		// alternatively you could use ArrayContentProvider.getInstance()
		v.setContentProvider(new MyContentProvider());
		createColumn(v.getTable(), "Values");
		MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
	}

	public void createColumn(Table tb, String text) {
		TableColumn column = new TableColumn(tb, SWT.NONE);
		column.setWidth(100);
		column.setText(text);
		tb.setHeaderVisible(true);
	}

	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10];

		for (int i = 0; i < 10; i++) {
			elements[i] = new MyModel(i);
		}

		return elements;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet001TableViewer(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
