/*******************************************************************************
 * Copyright (c) 2007, 2014 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 434287
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet that validates values across multiple bindings on change of each
 * observable. If the values of the target observables are not equal the model
 * is not updated. When the values are equal they will be written to sysout.
 *
 * @author Brad Reynolds
 */
public class Snippet011ValidateMultipleBindingsSnippet {
	public static void main(String[] args) {
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()),
				new Runnable() {
					@Override
					public void run() {
						Snippet011ValidateMultipleBindingsSnippet.run();
					}
				});
	}

	private static void run() {
		Shell shell = new Shell();

		View view = new View(shell);
		final Model model = new Model();

		DataBindingContext dbc = new DataBindingContext();
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(view.text1),
				model.value1, new UpdateValueStrategy()
						.setAfterConvertValidator(new CrossFieldValidator(
								model.value2)), null);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(view.text2),
				model.value2, new UpdateValueStrategy()
						.setAfterConvertValidator(new CrossFieldValidator(
								model.value1)), null);

		// DEBUG - print to show value change
		model.value1.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				System.out.println("Value 1: " + model.value1.getValue());
			}
		});

		// DEBUG - print to show value change
		model.value2.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				System.out.println("Value 2: " + model.value2.getValue());
			}
		});

		shell.pack();
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * @since 3.2
	 *
	 */
	private static final class CrossFieldValidator implements IValidator {
		/**
		 *
		 */
		private final IObservableValue other;

		/**
		 * @param model
		 */
		private CrossFieldValidator(IObservableValue other) {
			this.other = other;
		}

		@Override
		public IStatus validate(Object value) {
			if (!value.equals(other.getValue())) {
				// DEBUG - print validation result
				System.out.println("Validation fine");
				return ValidationStatus.ok();
			}
			// DEBUG - print validation result
			System.out.println("Validation error: values cannot be the same");
			return ValidationStatus.error("values cannot be the same");
		}
	}

	static class Model {
		WritableValue value1 = new WritableValue();
		WritableValue value2 = new WritableValue();
	}

	static class View {
		Text text1;
		Text text2;

		View(Composite composite) {
			composite.setLayout(new GridLayout(2, true));
			text1 = new Text(composite, SWT.BORDER);
			text2 = new Text(composite, SWT.BORDER);
		}
	}
}
