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

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.swt.WidgetSideEffects;
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
 *
 * @since 3.2
 *
 */
public class SnippetSideEffectTwoWayBinding {
	public static void main(String[] args) {
		Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			// create the Task model object
			Task task = new Task();
			final Shell shell = new View(task).createShell();
			Display display1 = Display.getCurrent();
			while (!shell.isDisposed()) {
				if (!display1.readAndDispatch()) {
					display1.sleep();
				}
			}
		});
	}

	// Observable Task model
	static class Task {

		private WritableValue<String> summary = new WritableValue<>("Learn Databinding", String.class);

		/**
		 * @return the task's summary
		 * @TrackedGetter
		 */
		public String getSummary() {
			return summary.getValue();
		}

		public void setSummary(String summary) {
			this.summary.setValue(summary);
		}
	}

	static class View {
		private Task task;
		private Text summaryText;

		public View(Task task) {
			this.task = task;
		}

		public Shell createShell() {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			GridLayoutFactory.fillDefaults().applyTo(shell);
			GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

			// create a Text widget, which will be bound to the Task summary
			summaryText = new Text(shell, SWT.BORDER);
			gridDataFactory.applyTo(summaryText);

			// create a Button to modify the model at runtime
			Button setSummaryBtn = new Button(shell, SWT.PUSH);
			gridDataFactory.applyTo(setSummaryBtn);
			setSummaryBtn.setText("Set Task summary to \"done learning\"");
			setSummaryBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					task.setSummary("done learning");
				}
			});

			// add a print Button to show the current task summary in the system
			// output
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

			// Open and return the Shell
			shell.pack();
			shell.open();

			return shell;
		}

		private void bindData() {

			ISideEffectFactory sideEffectFactory = WidgetSideEffects.createFactory(summaryText);

			// create the observables, which should be bound by the SideEffect
			IObservableValue<String> textModifyObservable = WidgetProperties.text(SWT.Modify).observe(summaryText);

			sideEffectFactory.create(task::getSummary, summaryText::setText);
			sideEffectFactory.create(textModifyObservable::getValue, task::setSummary);
		}
	}

}
