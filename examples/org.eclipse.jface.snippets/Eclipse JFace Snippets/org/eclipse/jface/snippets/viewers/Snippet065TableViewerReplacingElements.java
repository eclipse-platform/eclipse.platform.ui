/*******************************************************************************
 * Copyright (c) 2017 Conrad Groth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Conrad Groth - Bug 491682
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import java.util.Arrays;
import java.util.Random;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A TreeViewer with observable collections as input, to demonstrate, how
 * elements are replaced, especially what happens to selected items on
 * replacement
 */
public class Snippet065TableViewerReplacingElements {

	public Snippet065TableViewerReplacingElements(Shell shell) {
		Random random = new Random();
		final Composite c = new Composite(shell, SWT.NONE);
		c.setLayout(new FillLayout(SWT.VERTICAL));
		Label l = new Label(c, SWT.NONE);
		l.setText(
				"The elements are ordered lexicografically, i.e. 11 comes before 2,\nPress q, to rename one element.");
		final TableViewer v = new TableViewer(c);
		String[] rootElements = new String[] { "root 1", "root 2", "root 3" };
		final IObservableList<String> input = new WritableList<>(DisplayRealm.getRealm(shell.getDisplay()));
		input.addAll(Arrays.asList(rootElements));
		IContentProvider contentProvider = new ObservableListContentProvider();
		v.setContentProvider(contentProvider);
		v.setComparator(new ViewerComparator());
		v.setInput(input);
		v.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// don't use 'r' and 'c', because they would iterate through the
				// root... / child... elements
				if (e.character == 'q') {
					input.set(0, "root " + random.nextInt());
				}
			}
		});
	}

	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet065TableViewerReplacingElements(shell);
		shell.open ();

		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose ();
	}
}
