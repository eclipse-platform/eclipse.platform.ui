/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
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
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			final Shell shell = new View(new ViewModel()).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});
		// Print the results
		System.out.println("person.getName() = " + new ViewModel().getPerson().getName());
	}

	/**
	 * The data model class.
	 * <p>
	 * In this example, we extend the EventManager class to manage our listeners and
	 * we fire a property change event when the object state changes.
	 */
	public static class Person extends EventManager {
		String name = "HelloWorld";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			fireChange(new PropertyChangeEvent(this, NAME_PROPERTY, this.name, this.name = name));
		}

		public void addPropertyChangeListener(IPropertyChangeListener listener) {
			addListenerObject(listener);
		}

		public void removePropertyChangeListener(IPropertyChangeListener listener) {
			removeListenerObject(listener);
		}

		private void fireChange(PropertyChangeEvent event) {
			final Object[] list = getListeners();
			for (Object element : list) {
				((IPropertyChangeListener) element).propertyChange(event);
			}
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

			IValueProperty<Person, String> nameProperty = JFaceProperties.value(Person.class, "name", NAME_PROPERTY);

			bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(name), nameProperty.observe(person));

			Label label = new Label(shell, SWT.NONE);
			bindingContext.bindValue(WidgetProperties.text().observe(label), nameProperty.observe(person));

			shell.pack();
			shell.open();
			return shell;
		}
	}

}
