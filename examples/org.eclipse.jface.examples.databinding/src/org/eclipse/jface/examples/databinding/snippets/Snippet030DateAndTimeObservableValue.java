/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 169876)
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.util.Date;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.DateAndTimeObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 *
 */
public class Snippet030DateAndTimeObservableValue {
	protected Shell shell;
	private Text modelText;
	private DateTime date;
	private DateTime calendar;
	private DateTime time;
	private Button syncTime;

	/**
	 * Launch the application
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Snippet030DateAndTimeObservableValue window = new Snippet030DateAndTimeObservableValue();
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
			@Override
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
		layout.numColumns = 3;
		shell.setLayout(layout);
		shell.setText("Snippet030DateAndTimeObservableValue.java");

		new Label(shell, SWT.NONE).setText("Model date + time");
		modelText = new Text(shell, SWT.BORDER);
		modelText.setEditable(false);
		modelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		new Label(shell, SWT.NONE).setText("Target date (SWT.DATE)");
		date = new DateTime(shell, SWT.DATE | SWT.BORDER);
		date.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2,
				1));

		new Label(shell, SWT.NONE).setText("Target date (SWT.CALENDAR)");
		calendar = new DateTime(shell, SWT.CALENDAR);
		calendar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				2, 1));

		new Label(shell, SWT.NONE).setText("Target time");
		time = new DateTime(shell, SWT.TIME | SWT.BORDER);

		syncTime = new Button(shell, SWT.CHECK);
		syncTime.setLayoutData(new GridData());
		syncTime.setText("Sync with system time");

		bindUI();
	}

	private void bindUI() {
		DataBindingContext dbc = new DataBindingContext();

		IObservableValue model = WritableValue.withValueType(Date.class);
		model.setValue(new Date());

		dbc.bindValue(WidgetProperties.text().observe(modelText), model);

		final IObservableValue timeSelection = WidgetProperties.selection()
				.observe(time);

		dbc.bindValue(new DateAndTimeObservableValue(WidgetProperties
				.selection().observe(date), timeSelection), model);
		dbc.bindValue(new DateAndTimeObservableValue(WidgetProperties
				.selection().observe(calendar), timeSelection), model);

		syncTime.addListener(SWT.Selection, new Listener() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					if (syncTime.getSelection()) {
						timeSelection.setValue(new Date());
						Display.getCurrent().timerExec(100, this);
					}
				}
			};

			@Override
			public void handleEvent(Event event) {
				time.setEnabled(!syncTime.getSelection());
				if (syncTime.getSelection()) {
					runnable.run();
				}
			}
		});
	}
}
