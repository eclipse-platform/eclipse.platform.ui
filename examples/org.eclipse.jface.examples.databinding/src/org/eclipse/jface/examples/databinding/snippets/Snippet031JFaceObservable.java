/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.util.JFaceProperties;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet031JFaceObservable {

	public static final String NAME_PROPERTY = "name_property";

	public static void main(String[] args) {
		Display display = new Display();
		final ViewModel viewModel = new ViewModel();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				final Shell shell = new View(viewModel).createShell();
				// The SWT event loop
				Display display = Display.getCurrent();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
		// Print the results
		System.out.println("person.getName() = "
				+ viewModel.getPerson().getName());
	}

	// The data model class. This is normally a persistent class of some sort.
	// 
	// In this example, we extend the EventManager class
	// to manage our listeners and we fire a property change
	// event when the object state changes.
	public static class Person extends EventManager {
		// A property...
		String name = "HelloWorld";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			fireChange(new PropertyChangeEvent(this, NAME_PROPERTY, this.name,
					this.name = name));
		}

		public void addPropertyChangeListener(IPropertyChangeListener listener) {
			addListenerObject(listener);
		}

		public void removePropertyChangeListener(
				IPropertyChangeListener listener) {
			removeListenerObject(listener);
		}

		private void fireChange(PropertyChangeEvent event) {
			final Object[] list = getListeners();
			for (int i = 0; i < list.length; ++i) {
				((IPropertyChangeListener) list[i]).propertyChange(event);
			}
		}

	}

	// The View's model--the root of our Model graph for this particular GUI.
	//
	// Typically each View class has a corresponding ViewModel class.
	// The ViewModel is responsible for getting the objects to edit from the
	// DAO. Since this snippet doesn't have any persistent objects to
	// retrieve, this ViewModel just instantiates a model object to edit.
	static class ViewModel {
		// The model to bind
		private Person person = new Person();

		public Person getPerson() {
			return person;
		}
	}

	// The GUI view
	static class View {
		private ViewModel viewModel;
		private Text name;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			shell.setLayout(new RowLayout(SWT.VERTICAL));
			name = new Text(shell, SWT.BORDER);

			// Bind it
			DataBindingContext bindingContext = new DataBindingContext();
			Person person = viewModel.getPerson();

			IValueProperty nameProperty = JFaceProperties.value(Person.class,
					"name", NAME_PROPERTY);

			bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(name), nameProperty.observe(person),
					null, null);

			Label label = new Label(shell, SWT.NONE);
			bindingContext.bindValue(WidgetProperties.text().observe(label),
					nameProperty.observe(person), null, null);

			// Open and return the Shell
			shell.pack();
			shell.open();
			return shell;
		}
	}

}
