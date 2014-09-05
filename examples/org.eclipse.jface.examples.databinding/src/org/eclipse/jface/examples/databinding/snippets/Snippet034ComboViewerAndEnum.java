/*******************************************************************************
 * Copyright (c) 2009, 2014 Eric Rizzo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eric Rizzo - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet034ComboViewerAndEnum {

	public static void main(String[] args) {
		Display display = new Display();
		final Person model = new Person("Pat", Gender.Unknown);

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				final Shell shell = new View(model).createShell();
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
		System.out.println("person.getName() = " + model.getName());
		System.out.println("person.getGender() = " + model.getGender());
	}

	static enum Gender {
		Male, Female, Unknown;
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
		String name;
		Gender gender;

		public Person(String name, Gender gender) {
			this.name = name;
			this.gender = gender;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Gender getGender() {
			return gender;
		}

		public void setGender(Gender newGender) {
			this.gender = newGender;
		}
	}

	// The GUI view
	static class View {
		private Person viewModel;
		private Text name;
		private ComboViewer gender;

		public View(Person viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			Display display = Display.getDefault();
			Shell shell = new Shell(display);

			RowLayout layout = new RowLayout(SWT.VERTICAL);
			layout.fill = true;
			layout.marginWidth = layout.marginHeight = 5;
			shell.setLayout(layout);

			name = new Text(shell, SWT.BORDER);
			gender = new ComboViewer(shell, SWT.READ_ONLY);

			// Here's the first key to binding a combo to an Enum:
			// First give it an ArrayContentProvider,
			// then set the input to the list of values from the Enum.
			gender.setContentProvider(ArrayContentProvider.getInstance());
			gender.setInput(Gender.values());

			// Bind the fields
			DataBindingContext bindingContext = new DataBindingContext();

			IObservableValue widgetObservable = WidgetProperties.text(SWT.Modify).observe(
					name);
			bindingContext.bindValue(widgetObservable, PojoObservables
					.observeValue(viewModel, "name"));

			// The second key to binding a combo to an Enum is to use a
			// selection observable from the ComboViewer:
			widgetObservable = ViewersObservables
					.observeSingleSelection(gender);
			bindingContext.bindValue(widgetObservable, PojoObservables
					.observeValue(viewModel, "gender"));

			// Open and return the Shell
			shell.pack();
			shell.open();
			return shell;
		}
	}

}
