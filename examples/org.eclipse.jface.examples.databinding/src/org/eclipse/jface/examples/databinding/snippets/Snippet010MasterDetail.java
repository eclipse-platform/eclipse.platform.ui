/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 260329
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
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
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(display);
				shell.setLayout(new GridLayout());

				Person[] persons = new Person[] { new Person("Me"),
						new Person("Myself"), new Person("I") };

				ListViewer viewer = new ListViewer(shell);
				viewer.setContentProvider(new ArrayContentProvider());
				viewer.setInput(persons);

				Text name = new Text(shell, SWT.BORDER | SWT.READ_ONLY);

				// 1. Observe changes in selection.
				IObservableValue selection = ViewersObservables
						.observeSingleSelection(viewer);

				// 2. Observe the name property of the current selection.
				IObservableValue detailObservable = BeansObservables
						.observeDetailValue(selection, "name", String.class);

				// 3. Bind the Text widget to the name detail (selection's
				// name).
				new DataBindingContext().bindValue(SWTObservables.observeText(
						name, SWT.None), detailObservable,
						new UpdateValueStrategy(false,
								UpdateValueStrategy.POLICY_NEVER), null);

				shell.open();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
		display.dispose();
	}

	public static class Person {
		private String name;
		private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

		Person(String name) {
			this.name = name;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}
		
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}
		
		/**
		 * @return Returns the name.
		 */
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
