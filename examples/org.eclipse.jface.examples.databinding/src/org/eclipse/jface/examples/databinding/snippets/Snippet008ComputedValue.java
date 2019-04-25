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
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
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
 * Snippet that demostrates a simple use case using ComputedValue to format a
 * name as the user enters first and last name.
 *
 * @since 3.2
 */
public class Snippet008ComputedValue {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = new Display();
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = new Shell(display);
			shell.setLayout(new FillLayout());

			final UI ui = new UI(shell);
			final Data data = new Data();

			// Bind the UI to the Data.
			DataBindingContext dbc = new DataBindingContext();
			dbc.bindValue((IObservableValue<String>) WidgetProperties.text(SWT.Modify).observe(ui.firstName),
					data.firstName);
			dbc.bindValue((IObservableValue<String>) WidgetProperties.text(SWT.Modify).observe(ui.lastName),
					data.lastName);

			// Construct the formatted name observable.
			FormattedName formattedName = new FormattedName(data.firstName, data.lastName);

			// Bind the formatted name Text to the formatted name
			// observable.
			dbc.bindValue((IObservableValue<String>) WidgetProperties.text(SWT.None).observe(ui.formattedName),
					formattedName, new UpdateValueStrategy<String, String>(false, UpdateValueStrategy.POLICY_NEVER),
					null);

			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		});
		display.dispose();
	}

	/**
	 * Creates the formatted name on change of the first or last name
	 * observables.
	 * <p>
	 * The key to understanding ComputedValue is understanding that it knows of
	 * the observables that are queried without being told. This is done with
	 * {@link ObservableTracker} voodoo. When calculate() is invoked
	 * <code>ObservableTracker</code> records the observables that are
	 * queried. It then exposes those observables and <code>ComputedValue</code>
	 * can listen to changes in those objects and react accordingly.
	 * </p>
	 *
	 * @since 3.2
	 */
	static class FormattedName extends ComputedValue<String> {
		private IObservableValue<String> firstName;

		private IObservableValue<String> lastName;

		FormattedName(IObservableValue<String> firstName, IObservableValue<String> lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		@Override
		protected String calculate() {
			String lastName = this.lastName.getValue();
			String firstName = this.firstName.getValue();
			lastName = (lastName != null && lastName.length() > 0) ? lastName
					: "[Last Name]";
			firstName = (firstName != null && firstName.length() > 0) ? firstName
					: "[First Name]";

			StringBuilder buffer = new StringBuilder();
			buffer.append(lastName).append(", ").append(firstName);

			return buffer.toString();
		}
	}

	static class Data {
		final WritableValue<String> firstName;

		final WritableValue<String> lastName;

		Data() {
			firstName = new WritableValue<>("", String.class);
			lastName = new WritableValue<>("", String.class);
		}
	}

	/**
	 * Composite that creates the UI.
	 *
	 * @since 3.2
	 */
	static class UI extends Composite {
		final Text firstName;

		final Text lastName;

		final Text formattedName;

		UI(Composite parent) {
			super(parent, SWT.NONE);

			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

			new Label(this, SWT.NONE).setText("First Name:");
			new Label(this, SWT.NONE).setText("Last Name");

			GridDataFactory gdf = GridDataFactory.swtDefaults().align(SWT.FILL,
					SWT.FILL).grab(true, false);
			firstName = new Text(this, SWT.BORDER);
			gdf.applyTo(firstName);

			lastName = new Text(this, SWT.BORDER);
			gdf.applyTo(lastName);

			gdf = GridDataFactory.swtDefaults().span(2, 1).grab(true, false)
					.align(SWT.FILL, SWT.BEGINNING);
			Label label = new Label(this, SWT.NONE);
			label.setText("Formatted Name:");
			gdf.applyTo(label);

			formattedName = new Text(this, SWT.BORDER);
			formattedName.setEditable(false);
			gdf.applyTo(formattedName);
		}
	}
}
