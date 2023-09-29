/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.WidgetSideEffects;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This snippet shows how to use the {@link ISideEffect} class to bind a
 * {@link Text} widget to a {@link Task} and the other way round.
 */
public class Snippet041SideEffectTwoWayBinding {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			// Create the Task model object
			final Shell shell = new View(new Task()).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});
	}

	/** Observable Task model */
	static class Task {

		private final WritableValue<String> summary = new WritableValue<>("Learn Databinding", String.class);

		public String getSummary() {
			return summary.getValue();
		}

		public void setSummary(String summary) {
			this.summary.setValue(summary);
		}
	}

	static class View {
		private final Task task;
		private Text summaryText;

		public View(Task task) {
			this.task = task;
		}

		public Shell createShell() {
			Shell shell = new Shell();
			GridLayoutFactory.fillDefaults().applyTo(shell);
			GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

			// Create a Text widget, which will be bound to the Task summary
			summaryText = new Text(shell, SWT.BORDER);
			gridDataFactory.applyTo(summaryText);

			// Create a Button to modify the model at runtime
			Button setSummaryBtn = new Button(shell, SWT.PUSH);
			gridDataFactory.applyTo(setSummaryBtn);
			setSummaryBtn.setText("Set Task summary to \"done learning\"");
			setSummaryBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					task.setSummary("done learning");
				}
			});

			// Add a print Button to show the current task summary in the system output
			Button printButton = new Button(shell, SWT.PUSH);
			gridDataFactory.applyTo(printButton);
			printButton.setText("Print Task summary");
			printButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					System.out.println(task.getSummary());
				}
			});

			bindData();

			shell.pack();
			shell.open();

			return shell;
		}

		private void bindData() {

			ISideEffectFactory sideEffectFactory = WidgetSideEffects.createFactory(summaryText);

			// Create the observables, which should be bound by the SideEffect
			IObservableValue<String> textModifyObservable = WidgetProperties.text(SWT.Modify).observe(summaryText);

			sideEffectFactory.create(task::getSummary, summaryText::setText);
			sideEffectFactory.create(textModifyObservable::getValue, task::setSummary);
		}
	}

}
