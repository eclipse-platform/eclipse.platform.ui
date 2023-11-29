/*******************************************************************************
 * Copyright (c) 2006, 2018 Tom Schindl and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 475361
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example how to fill a {@link TableViewer} from the background without
 * blocking the UI
 *
 * @author Tom Schindl &lt;tom.schindl@bestsolution.at&gt;
 */
public class Snippet045TableViewerFillFromBackgroundThread {
	private static int COUNTER = 0;

	public static class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public static class MyLabelProvider extends LabelProvider implements
			ITableLabelProvider, ITableFontProvider, ITableColorProvider {
		FontRegistry registry = new FontRegistry();

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			MyModel m = (MyModel) element;
			if (m.counter == 0) {
				return "Column " + columnIndex + " => " + "Initial input";
			}
			return "Column " + columnIndex + " => thread added " + element;
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			if (((MyModel) element).counter % 2 == 0) {
				return registry.getBold(Display.getCurrent().getSystemFont()
						.getFontData()[0].getName());
			}
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			if (((MyModel) element).counter % 2 == 0) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}
			return null;
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			if (((MyModel) element).counter % 2 == 1) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}
			return null;
		}

	}

	public Snippet045TableViewerFillFromBackgroundThread(final Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.setLabelProvider(new MyLabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 1");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 2");

		final List<MyModel> model = new ArrayList<>();
		model.add(new MyModel(0));
		model.add(new MyModel(0));
		model.add(new MyModel(0));
		v.setInput(model);
		v.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				MyModel m1 = (MyModel) e1;
				MyModel m2 = (MyModel) e2;
				return m2.counter - m1.counter;
			}

		});
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				shell.getDisplay().syncExec(() -> {
					MyModel el = new MyModel(++COUNTER);
					v.add(el);
					model.add(el);
				});
			}
		};

		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(task, 3000, 1000);
	}

	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet045TableViewerFillFromBackgroundThread(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}