/*******************************************************************************
 * Copyright (c) 2022 Jens Lidestrom and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - Initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.examples.databinding.snippets;

import static org.eclipse.core.databinding.AggregateValidationStatus.MAX_SEVERITY;
import static org.eclipse.core.databinding.validation.ValidationStatus.error;
import static org.eclipse.core.databinding.validation.ValidationStatus.ok;

import java.util.Objects;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.bind.Bind;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet that demonstrates the fluent databinding API based on the
 * {@link Bind} class.
 */
public class Snippet043BindApi {
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
		shell.setText("Data Binding Snippet 004");
		shell.setLayout(new GridLayout(2, false));

		LabelFactory.newLabel(SWT.NONE).text("Enter '5' to be valid:").create(shell);

		Text text = TextFactory.newText(SWT.BORDER).layoutData(GridDataFactory.fillDefaults().create()).create(shell);

		LabelFactory.newLabel(SWT.NONE).text("Error:").create(shell);

		Label errorLabel = LabelFactory.newLabel(SWT.NONE) //
				.foreground(Display.getCurrent().getSystemColor(SWT.COLOR_RED)) //
				.layoutData(GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).create()) //
				.create(shell);

		var value = new WritableValue<>(3, Integer.class);

		// The zeroButton and fiveButton update the model, which in turn updates the GUI
		// target. This demonstrates the two-way binding.

		ButtonFactory.newButton(SWT.NONE).text("Set zero!").onSelect(e -> value.setValue(0)).create(shell);

		ButtonFactory.newButton(SWT.NONE).text("Set five!").onSelect(e -> value.setValue(5)).create(shell);

		var bindingContext = new DataBindingContext();

		// Long binding chain that demonstrates both converters and validators

		IValidator<Integer> fiveValidator = v -> v == 5 ? ok() : error("Value was '" + v + "', not '5'");

		Bind.twoWay() //
				.from(WidgetProperties.text(SWT.Modify).observe(text)) //
				// This validator runs for both target-to-value and value-to-target directions
				.validateTwoWay(v -> v.matches("\\s*\\d+\\s*") ? Status.OK_STATUS : error("Not a number: '" + v + "'"))
				.convertTo(IConverter.create(s -> s.isEmpty() ? 0 : Integer.decode(s))) //
				.convertFrom(IConverter.create(i -> Objects.toString(i, ""))) //
				.to(value) //
				.validateAfterConvert(fiveValidator) //
				.bind(bindingContext);

		// Separate binding only for model-to-target validation status. This binding
		// updates the validation status, but never the actual value. By having one
		// binding that updates the value and another one that performs validation we
		// can get a validation result without blocking the value being updated.
		Bind.oneWay() //
				.from(value) //
				.validateAfterGet(fiveValidator).defaultConvert() // This triggers default converters in
																	// DataBindingContext
				.to(Observables.constantObservableValue(null)) // Dummy value for convert only binding
				.convertOnly() //
				.bind(bindingContext);

		// Bind the error label to the validation error on the bindingContext
		Bind.oneWay() //
				.from(new AggregateValidationStatus(bindingContext.getBindings(), MAX_SEVERITY)) //
				.defaultConvert() // This triggers default converters in DataBindingContext
				.to(WidgetProperties.text().observe(errorLabel)) //
				.bind(bindingContext);

		shell.pack();
		shell.open();

		return shell;
	}
}
