/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920, 159768
 *     Matthew Hall - bug 260329
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet that displays how to bind the validation error of the
 * {@link DataBindingContext} to a label. http://www.eclipse.org
 * 
 * @since 3.2
 */
public class Snippet004DataBindingContextErrorLabel {
	public static void main(String[] args) {
		final Display display = new Display();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				Shell shell = new Shell(display);
				shell.setText("Data Binding Snippet 004");
				shell.setLayout(new GridLayout(2, false));

				new Label(shell, SWT.NONE).setText("Enter '5' to be valid:");

				Text text = new Text(shell, SWT.BORDER);
				WritableValue value = WritableValue.withValueType(String.class);
				new Label(shell, SWT.NONE).setText("Error:");

				Label errorLabel = new Label(shell, SWT.BORDER);
				errorLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
				GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).applyTo(
						errorLabel);

				DataBindingContext dbc = new DataBindingContext();

				// Bind the text to the value.
				dbc.bindValue(
						SWTObservables.observeText(text, SWT.Modify),
						value,
						new UpdateValueStrategy().setAfterConvertValidator(new FiveValidator()),
						null);

				// Bind the error label to the validation error on the dbc.
				dbc.bindValue(SWTObservables.observeText(errorLabel),
						new AggregateValidationStatus(dbc.getBindings(),
								AggregateValidationStatus.MAX_SEVERITY));

				shell.pack();
				shell.open();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
		display.dispose();
	}

	/**
	 * Validator that returns validation errors for any value other than 5.
	 * 
	 * @since 3.2
	 */
	private static class FiveValidator implements IValidator {
		public IStatus validate(Object value) {
			return ("5".equals(value)) ? Status.OK_STATUS : ValidationStatus
					.error("the value was '" + value + "', not '5'");
		}
	}
}
