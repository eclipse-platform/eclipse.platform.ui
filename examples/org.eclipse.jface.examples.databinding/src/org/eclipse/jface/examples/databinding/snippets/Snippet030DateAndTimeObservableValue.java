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
 *     Matthew Hall - initial API and implementation (bug 169876)
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.LocalDateTimeObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet030DateAndTimeObservableValue {
	private Text modelText;
	private DateTime date;
	private DateTime calendar;
	private DateTime time;
	private Button syncTime;

	public static void main(String[] args) {
		final Display display = Display.getDefault();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = new Snippet030DateAndTimeObservableValue().createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	private Shell createShell() {
		Shell shell = new Shell();
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		shell.setLayout(layout);
		shell.setText("Snippet030DateAndTimeObservableValue.java");

		new Label(shell, SWT.NONE).setText("Model date + time");
		modelText = new Text(shell, SWT.BORDER);
		modelText.setEditable(false);
		modelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		new Label(shell, SWT.NONE).setText("Target date (SWT.DATE)");
		date = new DateTime(shell, SWT.DATE | SWT.BORDER);
		date.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		new Label(shell, SWT.NONE).setText("Target date (SWT.CALENDAR)");
		calendar = new DateTime(shell, SWT.CALENDAR);
		calendar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		new Label(shell, SWT.NONE).setText("Target time");
		time = new DateTime(shell, SWT.TIME | SWT.BORDER);

		syncTime = new Button(shell, SWT.CHECK);
		syncTime.setLayoutData(new GridData());
		syncTime.setText("Sync with system time");

		bindUI();

		shell.pack();
		shell.open();

		return shell;
	}

	private void bindUI() {
		DataBindingContext bindingContext = new DataBindingContext();

		IObservableValue<LocalDateTime> model = WritableValue.withValueType(LocalDateTime.class);
		model.setValue(LocalDateTime.now());

		bindingContext.bindValue(WidgetProperties.text().observe(modelText), model);

		final IObservableValue<LocalTime> timeSelection = WidgetProperties.localTimeSelection().observe(time);

		bindingContext.bindValue(
				new LocalDateTimeObservableValue(WidgetProperties.localDateSelection().observe(date), timeSelection),
				model);

		bindingContext.bindValue(new LocalDateTimeObservableValue(
				WidgetProperties.localDateSelection().observe(calendar), timeSelection), model);

		syncTime.addListener(SWT.Selection, event -> {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					if (!syncTime.isDisposed() && syncTime.getSelection()) {
						timeSelection.setValue(LocalTime.now());
						Display.getCurrent().timerExec(100, this);
					}
				}
			};

			time.setEnabled(!syncTime.getSelection());
			if (syncTime.getSelection()) {
				runnable.run();
			}
		});
	}
}
