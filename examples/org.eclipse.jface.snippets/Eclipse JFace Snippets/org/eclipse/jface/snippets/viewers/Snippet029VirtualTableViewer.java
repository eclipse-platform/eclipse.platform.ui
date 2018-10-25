/*******************************************************************************
 * Copyright (c) 2006, 2015 Tom Schindl and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 414565, 475361
 *     Jeanderson Candido <http://jeandersonbc.github.io> - 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A simple TableViewer to demonstrate the usage of a standard content provider
 * with a virtual table
 *
 */
public class Snippet029VirtualTableViewer {


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

	public Snippet029VirtualTableViewer(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.VIRTUAL);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setUseHashlookup(true);
		createColumn(v.getTable(), "Column 1");
		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
	}

	public void createColumn(Table tb, String text) {
		TableColumn column = new TableColumn(tb, SWT.NONE);
		column.setWidth(100);
		column.setText(text);
		tb.setHeaderVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<>();

		for (int i = 0; i < 10000; i++) {
			elements.add(new MyModel(i));
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet029VirtualTableViewer(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
