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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LazyArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A simple example to demonstrate the usage of {@link LazyArrayContentProvider}
 */
public class Snippet068TableViewerLazy {

	private static final int LARGE_NUMBER_OF_ITEMS = 1_000_000;

	private final class LabelProviderExtension extends LabelProvider {

		AtomicLong counter = new AtomicLong();

		@Override
		public String getText(Object element) {
			counter.incrementAndGet();
			return super.getText(element);
		}
	}

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

	public Snippet068TableViewerLazy(Shell shell) {
		final TableViewer v = new TableViewer(shell,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL);
		LabelProviderExtension ext = new LabelProviderExtension();
		v.setLabelProvider(ext);
		v.setContentProvider(new LazyArrayContentProvider());
		v.getTable().setLinesVisible(true);
		v.setUseHashlookup(true);
		createColumn(v.getTable(), "Values");
		System.out.println("Create Model with " + LARGE_NUMBER_OF_ITEMS + " items...");
		MyModel[] model = createModel();
		System.out.println("Set Input ...");
		v.setInput(model);
		System.out.println("We are done here...");
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					long last = 0;
					while (true) {
						TimeUnit.SECONDS.sleep(1);
						long current = ext.counter.get();
						if (current != last) {
							last = current;
							System.out.println("Total render requests: " + current);
						}
					}
				} catch (InterruptedException e) {
					return;
				}

			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public void createColumn(Table tb, String text) {
		TableColumn column = new TableColumn(tb, SWT.NONE);
		column.setWidth(100);
		column.setText(text);
		tb.setHeaderVisible(true);
	}

	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[LARGE_NUMBER_OF_ITEMS];

		for (int i = 0; i < elements.length; i++) {
			elements[i] = new MyModel(i);
		}

		return elements;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet068TableViewerLazy(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
