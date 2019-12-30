/*******************************************************************************
 * Copyright (c) 2009, 2018 Eric Rizzo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eric Rizzo - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.conversion.EnumConverters;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet034ComboViewerAndEnum {

	public static void main(String[] args) {
		final Display display = new Display();
		final Person model = new Person("Pat", Gender.UNKNOWN);

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = new View(model).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});
		// Print the results
		System.out.println("person.getName() = " + model.getName());
		System.out.println("person.getGender() = " + model.getGender());
	}

	enum Gender {
		MALE("Male"), FEMALE("Female"), UNKNOWN("Unknown"), OTHER("Other");

		private String displayName;

		private Gender(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
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
		private String name;
		private Gender gender;
		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

		public Person(String name, Gender gender) {
			this.name = name;
			this.gender = gender;
		}

		public String getName() {
			return name;
		}

		public void setName(String newName) {
			String old = this.name;
			this.name = newName;
			propertyChangeSupport.firePropertyChange("name", old, name);
		}

		public Gender getGender() {
			return gender;
		}

		public void setGender(Gender newGender) {
			Gender old = this.gender;
			this.gender = newGender;
			propertyChangeSupport.firePropertyChange("gender", old, gender);
		}

		public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
		}

	}

	/** The GUI view. */
	static class View {
		private Person viewModel;
		private Text name;
		private ComboViewer gender;
		private Label genderText;

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
			genderText = new Label(shell, SWT.NONE);

			// Here's the first key to binding a combo to an Enum:
			// First give it an ArrayContentProvider,
			// then set the input to the list of values from the Enum.
			gender.setContentProvider(ArrayContentProvider.getInstance());
			gender.setInput(Gender.values());

			// Bind the fields
			DataBindingContext bindingContext = new DataBindingContext();

			bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(name),
					PojoProperties.value(Person.class, "name").observe(viewModel));

			// The second key to binding a combo to an Enum is to use a
			// selection observable from the ComboViewer:
			bindingContext.bindValue(ViewerProperties.singleSelection(Gender.class).observe(gender),
					PojoProperties.value(Person.class, "gender").observe(viewModel));

			// The EnumConverters class is convenient when binding an enum in a situation
			// where a Viewer can not be used
			bindingContext.bindValue(WidgetProperties.text().observe(genderText),
					BeanProperties.value(Person.class, "gender", Gender.class).observe(viewModel), null,
					UpdateValueStrategy.create(EnumConverters.toString(Gender.class)));

			shell.pack();
			shell.open();
			return shell;
		}
	}
}
