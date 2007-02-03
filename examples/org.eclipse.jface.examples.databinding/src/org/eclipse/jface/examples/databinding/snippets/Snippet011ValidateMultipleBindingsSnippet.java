/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.IBindingListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet that validates values across multiple bindings on change of each
 * observable. If the values of the target observables are not equal the model
 * is not updated.  When the values are equal they will be written to sysout.
 * 
 * @author Brad Reynolds
 */
public class Snippet011ValidateMultipleBindingsSnippet {
	public static void main(String[] args) {
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()),
				new Runnable() {
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
		final Binding binding1 = dbc.bindValue(SWTObservables.observeText(
				view.text1, SWT.Modify), model.value1, null);
		final Binding binding2 = dbc.bindValue(SWTObservables.observeText(
				view.text2, SWT.Modify), model.value2, null);

		/**
		 * Listener that will validate multiple bindings each time a change
		 * occurs in either target.
		 */
		IBindingListener listener = new IBindingListener() {
			/**
			 * Value of the binding that is changing.
			 */
			Object changingValue;
			/**
			 * Status to return for both bindings.
			 */
			IStatus validationStatus;

			public IStatus handleBindingEvent(BindingEvent e) {
				if (!(e.copyType == BindingEvent.EVENT_COPY_TO_MODEL && e.pipelinePosition == BindingEvent.PIPELINE_AFTER_CONVERT)) {
					return Status.OK_STATUS;
				}

				Binding other = (e.binding == binding1) ? binding2 : binding1;

				if (changingValue == null) {
					changingValue = e.convertedValue;
					validationStatus = Status.OK_STATUS;

					// force validation to run for the other
					// binding
					other.updateModelFromTarget();

					// reset stored state
					changingValue = null;
				} else if (!changingValue.equals(e.convertedValue)) {
					validationStatus = Status.CANCEL_STATUS;
				}

				return validationStatus;
			}
		};

		binding1.addBindingEventListener(listener);
		binding2.addBindingEventListener(listener);

		// DEBUG - print to show value change
		model.value1.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				System.out.println("Value 1: " + model.value1.getValue());
			}
		});

		// DEBUG - print to show value change
		model.value2.addValueChangeListener(new IValueChangeListener() {
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
