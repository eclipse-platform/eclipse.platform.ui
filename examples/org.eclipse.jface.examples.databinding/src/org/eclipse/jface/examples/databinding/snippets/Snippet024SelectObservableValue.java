/*******************************************************************************
 * Copyright (c) 2008, 2014 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 249992)
 *     Matthew Hall - bug 260329
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrate usage of SelectObservableValue
 *
 * @since 3.2
 */
public class Snippet024SelectObservableValue {
	protected Shell shell;

	public static void main(String[] args) {
		final Display display = Display.getDefault();
		Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {
			@Override
			public void run() {
				try {
					Snippet024SelectObservableValue window = new Snippet024SelectObservableValue();
					window.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void open() {
		final Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	protected void createContents() {
		shell = new Shell();
		shell.setSize(400, 300);
		shell.setLayout(new GridLayout(2, true));
		shell.setText("Snippet024SelectObservableValue");

		final ListViewer listViewer = new ListViewer(shell, SWT.BORDER);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		final Group group = new Group(shell, SWT.NONE);
		group.setText("Radio Group");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new GridLayout());

		// Data Binding
		Color[] colors = Color.values();

		listViewer.setInput(colors);
		IViewerObservableValue listViewerSelection = ViewersObservables
				.observeSingleSelection(listViewer);

		SelectObservableValue radioGroup = new SelectObservableValue();
		for (int i = 0; i < colors.length; i++) {
			Button button = new Button(group, SWT.RADIO);
			button.setText(colors[i].toString());
			radioGroup.addOption(colors[i], WidgetProperties.selection().observe(button));
		}

		DataBindingContext dbc = new DataBindingContext();
		dbc.bindValue(radioGroup, listViewerSelection);
	}

	public static class Color {
		public static final Color RED = new Color("Red");
		public static final Color ORANGE = new Color("Orange");
		public static final Color YELLOW = new Color("Yellow");
		public static final Color GREEN = new Color("Green");
		public static final Color BLUE = new Color("Blue");
		public static final Color INDIGO = new Color("Indigo");
		public static final Color VIOLET = new Color("Violet");

		private final String name;

		private Color(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static Color[] values() {
			return new Color[] { RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO,
					VIOLET };
		}
	}
}
