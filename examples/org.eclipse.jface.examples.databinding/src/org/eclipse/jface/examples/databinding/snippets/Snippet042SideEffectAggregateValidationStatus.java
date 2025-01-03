/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet042SideEffectAggregateValidationStatus {

	public static void main(String[] args) {
		final Display display = new Display();
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> run(display));

		display.dispose();
	}

	private static void run(final Display display) {
		Shell shell = new Shell(display);
		shell.setText("ISideEffect & AggregateValidationStatus");
		shell.setLayout(new GridLayout(2, false));

		new Label(shell, SWT.NONE).setText("Enter '5' to be valid:");

		Text text = new Text(shell, SWT.BORDER);
		WritableValue<String> value = new WritableValue<>();

		Button okButton = new Button(shell, SWT.BORDER);
		okButton.setText("Ok");
		GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).applyTo(okButton);

		DataBindingContext bindingContext = new DataBindingContext();

		// Bind the text to the value
		IValidator<String> validator = textValue -> "5".equals(textValue) ? Status.OK_STATUS
				: ValidationStatus.error("The value was '" + value + "', not '5'");
		bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(text), value,
				new UpdateValueStrategy<Object, String>().setAfterConvertValidator(validator), null);

		// Track the max severity of all bindings
		AggregateValidationStatus validationStatus = new AggregateValidationStatus(bindingContext.getBindings(),
				AggregateValidationStatus.MAX_SEVERITY);

		// Bind the button enabled state to the validation status
		ISideEffect okButtonEnablement = ISideEffect.create(() -> validationStatus.getValue().isOK(),
				okButton::setEnabled);

		shell.addDisposeListener(e -> {
			bindingContext.dispose();
			okButtonEnablement.dispose();
		});

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
