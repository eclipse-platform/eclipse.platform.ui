/*******************************************************************************
 * Copyright (c) 2007, 2018 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 260329
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet that displays a simple master detail use case. A list of persons is
 * displayed in a list and upon selection the name of the selected person will
 * be displayed in a Text widget.
 */
public class Snippet010MasterDetail {
	public static void main(String[] args) {
		final Display display = new Display();
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	private static Shell createShell() {
		Shell shell = new Shell();
		shell.setLayout(new GridLayout());

		Person[] persons = new Person[] { new Person("Me"), new Person("Myself"), new Person("I") };

		ListViewer viewer = new ListViewer(shell);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(persons);

		Text name = new Text(shell, SWT.BORDER | SWT.READ_ONLY);

		// 1. Observe changes in selection
		IObservableValue<Person> selection = ViewerProperties.singleSelection(Person.class).observe(viewer);

		// 2. Observe the name property of the current selection
		IObservableValue<String> detailObservable = PojoProperties.value(Person.class, "name", String.class)
				.observeDetail(selection);

		// 3. Bind the Text widget to the name detail (selection's name)
		new DataBindingContext().bindValue(WidgetProperties.text(SWT.NONE).observe(name), detailObservable,
				UpdateValueStrategy.never(), null);

		shell.open();
		return shell;
	}

	public static class Person {
		private final String name;

		Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
