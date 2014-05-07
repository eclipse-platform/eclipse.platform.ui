/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public class Snippet022ComputedListCombo {
	private static WritableList model;

	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				Snippet022ComputedListCombo snippet = new Snippet022ComputedListCombo();
				snippet.createModel();
				snippet.createControls(shell);
			}
		});

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * 
	 */
	protected void createModel() {
		model = new WritableList();
		model.add(new Thing("Alice", true, false));
		model.add(new Thing("Beth", true, false));
		model.add(new Thing("Cathy", true, false));
		model.add(new Thing("Arthur", false, true));
		model.add(new Thing("Bob", false, true));
		model.add(new Thing("Curtis", false, true));
		model.add(new Thing("Snail", true, true));
		model.add(new Thing("Nail", false, false));
	}

	/**
	 * @param shell
	 */
	protected void createControls(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		Group group = new Group(composite, SWT.NONE);
		group.setText("Filter");
		Button male = new Button(group, SWT.CHECK);
		male.setText("Male");
		Button female = new Button(group, SWT.CHECK);
		female.setText("Female");
		final IObservableValue femaleObservable = SWTObservables
				.observeSelection(female);
		final IObservableValue maleObservable = SWTObservables
				.observeSelection(male);
		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridDataFactory.defaultsFor(combo).align(SWT.BEGINNING, SWT.BEGINNING)
				.applyTo(combo);
		ComboViewer viewer = new ComboViewer(combo);
		viewer.setContentProvider(new ObservableListContentProvider());
		// We should really have an out-of-the box filtered list...
		IObservableList filteredList = new ComputedList() {
			@Override
			protected List calculate() {
				ArrayList result = new ArrayList();
				for (Iterator it = model.iterator(); it.hasNext();) {
					Thing thing = (Thing) it.next();
					if (((Boolean) femaleObservable.getValue()).booleanValue()
							&& !thing.female)
						continue;
					if (((Boolean) maleObservable.getValue()).booleanValue()
							&& !thing.male)
						continue;
					result.add(thing);
				}
				return result;
			}
		};
		viewer.setInput(filteredList);
		GridLayoutFactory.swtDefaults().applyTo(group);
		GridLayoutFactory.swtDefaults().applyTo(composite);
	}

	static class Thing {
		String name;
		boolean male;
		boolean female;

		public Thing(String name, boolean female, boolean male) {
			this.name = name;
			this.female = female;
			this.male = male;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
