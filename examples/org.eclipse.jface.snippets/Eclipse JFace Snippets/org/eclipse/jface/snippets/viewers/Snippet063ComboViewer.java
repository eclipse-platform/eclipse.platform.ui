/*******************************************************************************
 * Copyright (c) 2014 Hendrik Still and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hendrik Still<hendrik.still@gammas.de> - initial implementation
 *     Lars Vogel <lars.vogel@gmail.com> - Bug 414565
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple ComboViewer to demonstrate usage
 *
 */

public class Snippet063ComboViewer {

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

	public Snippet063ComboViewer(Shell shell) {

		GridLayoutFactory.fillDefaults().numColumns(2)
				.margins(LayoutConstants.getMargins()).generateLayout(shell);

		final Label l = new Label(shell, SWT.None);
		l.setText("Choose Item:");
		final ComboViewer v = new ComboViewer(shell);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());

		List<MyModel> model = createModel();
		v.setInput(model);

		// Select the initial Element
		if (model.size() > 0) {
			v.setSelection(new StructuredSelection(model.get(0)));
		}
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>();

		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i));
		}
		return elements;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		new Snippet063ComboViewer(shell);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();

	}

}
