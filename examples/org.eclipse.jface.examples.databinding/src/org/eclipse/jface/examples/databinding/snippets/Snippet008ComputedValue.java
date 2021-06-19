/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds and others.
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
 *     Patrik Suzzi - 479848
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet that demonstrates a simple use case using {@link ComputedValue} to
 * format a name as the user enters first and last name.
 */
public class Snippet008ComputedValue {
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
		shell.setLayout(new FillLayout());

		final Composite ui = new Composite(shell, SWT.NONE);

		WritableValue<String> firstNameValue = new WritableValue<>();
		WritableValue<String> lastNameValue = new WritableValue<>();

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(ui);

		new Label(ui, SWT.NONE).setText("First Name:");
		new Label(ui, SWT.NONE).setText("Last Name");

		GridDataFactory dataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false);
		Text firstName = new Text(ui, SWT.BORDER);
		dataFactory.applyTo(firstName);

		Text lastName = new Text(ui, SWT.BORDER);
		dataFactory.applyTo(lastName);

		dataFactory = GridDataFactory.swtDefaults().span(2, 1).grab(true, false).align(SWT.FILL, SWT.BEGINNING);
		Label label = new Label(ui, SWT.NONE);
		label.setText("Formatted Name:");
		dataFactory.applyTo(label);

		Text formattedName = new Text(ui, SWT.BORDER);
		formattedName.setEditable(false);
		dataFactory.applyTo(formattedName);

		// Bind the UI to the Data
		DataBindingContext bindingContext = new DataBindingContext();

		bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(firstName), firstNameValue);
		bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(lastName), lastNameValue);

		//
		// Creates the formatted name on change of the first or last name observables.
		//
		// The key to understanding ComputedValue is understanding that it knows of the
		// observables that are queried without being told. This is done with
		// ObservableTracker voodoo. When calculate() is invoked
		// ObservableTracker records the observables that are queried. It
		// then exposes those observables and ComputedValue can listen to
		// changes in those objects and react accordingly.
		//
		ComputedValue<String> name = new ComputedValue<>() {
			@Override
			protected String calculate() {
				String lastName = lastNameValue.getValue();
				String firstName = firstNameValue.getValue();
				String lastNameOrDefault = lastName == null || lastName.isEmpty() ? "[Last Name]" : lastName;
				String firstNameOrDefault = firstName == null || firstName.isEmpty() ? "[First Name]" : firstName;

				return lastNameOrDefault + ", " + firstNameOrDefault;
			}
		};

		// Bind the formatted name Text to the formatted name observable
		bindingContext.bindValue(WidgetProperties.text(SWT.None).observe(formattedName), name,
				UpdateValueStrategy.never(), null);

		shell.pack();
		shell.open();

		return shell;
	}

}
