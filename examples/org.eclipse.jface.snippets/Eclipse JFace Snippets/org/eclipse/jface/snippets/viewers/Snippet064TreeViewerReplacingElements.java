/*******************************************************************************
 * Copyright (c) 2016 Conrad Groth and others.
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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
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
public class Snippet064TreeViewerReplacingElements {

	public Snippet064TreeViewerReplacingElements(Shell shell) {
		Random random = new Random();
		final Composite c = new Composite(shell, SWT.NONE);
		c.setLayout(new FillLayout(SWT.VERTICAL));
		Label l = new Label(c, SWT.NONE);
		l.setText(
				"The elements are ordered lexicografically, i.e. 11 comes before 2,\nPress q, to rename one root element.\nPress w, to rename one child element.");
		final TreeViewer v = new TreeViewer(c);

		// Every observable in this example gets typed as Object, since the
		// input element is an Object
		List<Object> rootElements = Arrays.asList("root 1", "root 2", "root 3");
		Object input = new Object();
		AtomicReference<IObservableList<Object>> recentlyCreatedChildList = new AtomicReference<>();
		final IObservableList<Object> rootElementList = new WritableList<>(DisplayRealm.getRealm(shell.getDisplay()),
				Arrays.asList(rootElements), String.class);
		ITreeContentProvider contentProvider = new ObservableListTreeContentProvider<>(target -> {
			if (target == input)
				return rootElementList;
			if (target.toString().startsWith("root")) {
				recentlyCreatedChildList.set(new WritableList<>(DisplayRealm.getRealm(shell.getDisplay()),
						Arrays.asList("child 1", "child 2"), String.class));
				return recentlyCreatedChildList.get();
			}
			return null;
		}, null);
		v.setContentProvider(contentProvider);
		v.setComparator(new ViewerComparator());
		v.setInput(input);
		v.getTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// don't use 'r' and 'c', because they would iterate through the
				// root... / child... elements
				if (e.character == 'q') {
					rootElementList.set(0, "root " + random.nextInt());
				}
				if (e.character == 'w') {
					IObservableList<Object> childElementsList = recentlyCreatedChildList.get();
					if (childElementsList != null) {
						childElementsList.set(0, "child " + random.nextInt());
					} else {
						System.out.println("no children list present");
					}
				}
			}
		});
	}

	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet064TreeViewerReplacingElements(shell);
		shell.open ();

		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}

		display.dispose ();
	}
}
