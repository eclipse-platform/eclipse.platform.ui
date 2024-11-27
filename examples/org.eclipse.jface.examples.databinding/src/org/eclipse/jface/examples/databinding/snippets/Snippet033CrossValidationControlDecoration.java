/*******************************************************************************
 * Copyright (c) 2009, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 268472)
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.time.LocalDate;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationUpdater;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Snippet033CrossValidationControlDecoration {
	private DateTime startDate;
	private DateTime endDate;

	public static void main(String[] args) {
		final Display display = Display.getDefault();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = new Snippet033CrossValidationControlDecoration().createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	protected Shell createShell() {
		Shell shell = new Shell();
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		shell.setLayout(layout);
		shell.setText("Snippet033CrossValidationControlDecoration.java");

		final Label label = new Label(shell, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Start date");
		startDate = new DateTime(shell, SWT.CALENDAR);
		final GridData gd_startDate = new GridData();
		gd_startDate.horizontalIndent = 10;
		startDate.setLayoutData(gd_startDate);

		final Label startDateLabel = new Label(shell, SWT.NONE);
		startDateLabel.setLayoutData(new GridData());
		startDateLabel.setText("End date");
		endDate = new DateTime(shell, SWT.CALENDAR);
		final GridData gd_endDate = new GridData();
		gd_endDate.horizontalIndent = 10;
		endDate.setLayoutData(gd_endDate);

		bindUI();

		shell.open();
		shell.pack();

		return shell;
	}

	private void bindUI() {
		IObservableValue<LocalDate> startDateObservable = WidgetProperties.localDateSelection().observe(startDate);
		IObservableValue<LocalDate> endDateObservable = WidgetProperties.localDateSelection().observe(endDate);

		ControlDecorationSupport.create(new DateRangeValidator(startDateObservable, endDateObservable,
				"Start date must be on or before end date"), SWT.LEFT | SWT.CENTER);

		// Customize the decoration's description text and image
		ControlDecorationUpdater decorationUpdater = new ControlDecorationUpdater() {
			@Override
			protected String getDescriptionText(IStatus status) {
				return "ERROR: " + super.getDescriptionText(status);
			}

			@Override
			protected Image getImage(IStatus status) {
				return status.isOK() ? null : Display.getCurrent().getSystemImage(SWT.ICON_ERROR);
			}
		};
		ControlDecorationSupport.create(
				new DateRangeValidator(Observables.constantObservableValue(LocalDate.now()), startDateObservable,
						"Choose a starting date later than today"),
				SWT.LEFT | SWT.TOP, (Composite) null, decorationUpdater);
	}

	private static class DateRangeValidator extends MultiValidator {
		private final IObservableValue<LocalDate> start;
		private final IObservableValue<LocalDate> end;
		private final String errorMessage;

		public DateRangeValidator(IObservableValue<LocalDate> start, IObservableValue<LocalDate> end,
				String errorMessage) {
			this.start = start;
			this.end = end;
			this.errorMessage = errorMessage;
		}

		@Override
		protected IStatus validate() {
			if (start.getValue().compareTo(end.getValue()) > 0) {
				return ValidationStatus.error(errorMessage);
			}
			return ValidationStatus.ok();
		}
	}
}
