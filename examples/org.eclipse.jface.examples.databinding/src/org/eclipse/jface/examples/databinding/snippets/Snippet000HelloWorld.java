/*******************************************************************************
 * Copyright (c) 2006, 2018 The Pampered Chef, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     The Pampered Chef, Inc. - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Benjamin Cabe - bug 252219
 *     Matthew Hall - bug 260329
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 434283, 529926
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Hello, databinding. Bind changes in a GUI to a Model object but don't worry
 * about propagating changes from the Model to the GUI.
 * <p>
 * Illustrates the basic Model-ViewModel-Binding-View architecture typically
 * used in data binding applications.
 */
public class Snippet000HelloWorld {
	public static void main(String[] args) {
		Display display = new Display();
		final ViewModel viewModel = new ViewModel();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			final Shell shell = new View(viewModel).createShell();
			// The SWT event loop
			Display display1 = Display.getCurrent();
			while (!shell.isDisposed()) {
				if (!display1.readAndDispatch()) {
					display1.sleep();
				}
			}
		});
	}

	// The data model class. This is normally a persistent class of some sort.
	//
	// In this example, we only push changes from the GUI to the model, so we
	// don't worry about implementing JavaBeans bound properties. If we need
	// our GUI to automatically reflect changes in the Person object, the
	// Person object would need to implement the JavaBeans property change
	// listener methods.
	static class Person {
		// A property...
		String name = "HelloWorld";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
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
		private Text textName;
		private Person person;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			shell.setLayout(new GridLayout(1, false));
			textName = new Text(shell, SWT.BORDER);
			Button button = new Button(shell, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			button.setText("Print to console");
			button.addSelectionListener(widgetSelectedAdapter(e -> System.out.println(person)));

			DataBindingContext bindingContext = new DataBindingContext();
			person = viewModel.getPerson();

			bindingContext.bindValue(
					WidgetProperties.text(SWT.Modify).observe(textName),
					PojoProperties.value("name").observe(person));

			// Open and return the Shell
			shell.pack();
			shell.open();
			return shell;
		}
	}

}
