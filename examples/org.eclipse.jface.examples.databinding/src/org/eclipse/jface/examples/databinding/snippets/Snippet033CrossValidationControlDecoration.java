/*******************************************************************************
 * Copyright (c) 2009, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 268472)
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.util.Date;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationUpdater;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public class Snippet033CrossValidationControlDecoration {
	protected Shell shell;
	private DateTime startDate;
	private DateTime endDate;

	/**
	 * Launch the application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Snippet033CrossValidationControlDecoration window = new Snippet033CrossValidationControlDecoration();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window
	 */
	public void open() {
		final Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				createContents();
				shell.pack();
				shell.open();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
	}

	protected void createContents() {
		shell = new Shell();
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
	}

	private void bindUI() {
		IObservableValue startDateObservable = WidgetProperties.selection()
				.observe(startDate);
		IObservableValue endDateObservable = WidgetProperties.selection()
				.observe(endDate);

		ControlDecorationSupport.create(new DateRangeValidator(
				startDateObservable, endDateObservable,
				"Start date must be on or before end date"), SWT.LEFT
				| SWT.CENTER);

		// Customize the decoration's description text and image
		ControlDecorationUpdater decorationUpdater = new ControlDecorationUpdater() {
			protected String getDescriptionText(IStatus status) {
				return "ERROR: " + super.getDescriptionText(status);
			}

			protected Image getImage(IStatus status) {
				return status.isOK() ? null : Display.getCurrent()
						.getSystemImage(SWT.ICON_ERROR);
			}
		};
		ControlDecorationSupport.create(new DateRangeValidator(Observables
				.constantObservableValue(new Date()), startDateObservable,
				"Choose a starting date later than today"), SWT.LEFT | SWT.TOP,
				(Composite) null, decorationUpdater);
	}

	private static class DateRangeValidator extends MultiValidator {
		private final IObservableValue start;
		private final IObservableValue end;
		private final String errorMessage;

		public DateRangeValidator(IObservableValue start, IObservableValue end,
				String errorMessage) {
			this.start = start;
			this.end = end;
			this.errorMessage = errorMessage;
		}

		protected IStatus validate() {
			Date startDate = (Date) start.getValue();
			Date endDate = (Date) end.getValue();
			if (startDate.compareTo(endDate) > 0)
				return ValidationStatus.error(errorMessage);
			return ValidationStatus.ok();
		}
	}
}
