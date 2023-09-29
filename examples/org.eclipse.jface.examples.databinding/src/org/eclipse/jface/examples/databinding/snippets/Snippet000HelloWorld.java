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
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			ViewModel viewModel = new ViewModel();
			Shell shell = new View(viewModel).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	/**
	 * The data model class.
	 * <p>
	 * In this example, we only push changes from the GUI to the model, so we don't
	 * worry about implementing JavaBeans bound properties. If we need our GUI to
	 * automatically reflect changes in the Person object, the Person object would
	 * need to implement the JavaBeans property change listener methods.
	 */
	static class Person {
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

	/**
	 * The View's model--the root of our Model graph for this particular GUI.
	 */
	static class ViewModel {
		// The model to bind
		private final Person person = new Person();

		public Person getPerson() {
			return person;
		}
	}

	/** The GUI view. */
	static class View {
		private final ViewModel viewModel;
		private Text textName;
		private Person person;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			Shell shell = new Shell();
			shell.setLayout(new GridLayout(1, false));
			textName = new Text(shell, SWT.BORDER);
			Button button = new Button(shell, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			button.setText("Print to console");
			button.addSelectionListener(widgetSelectedAdapter(e -> System.out.println(person)));

			DataBindingContext bindingContext = new DataBindingContext();
			person = viewModel.getPerson();

			bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(textName),
					PojoProperties.value("name").observe(person));

			shell.pack();
			shell.open();
			return shell;
		}
	}

}
