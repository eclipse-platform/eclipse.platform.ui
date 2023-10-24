/*******************************************************************************
 * Copyright (c) 2015 vogella GmbH and others.
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

import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This snippet shows how to use the
 * {@link ISideEffect#create(java.util.function.Supplier, java.util.function.Consumer)}
 * method to react properly, when an observable is changed inside an async
 * action, e.g., a Job.
 */
public class Snippet040SideEffectRunOnce {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = new View().createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	static class View {

		public Shell createShell() {
			Shell shell = new Shell();

			Label label = new Label(shell, SWT.CENTER);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			label.setText("Loading JSON...");

			IObservableValue<String> loadJsonFromRemote = loadJsonFromRemote();
			ISideEffect.consumeOnceAsync(loadJsonFromRemote::getValue, System.out::println);
			ISideEffect.consumeOnceAsync(loadJsonFromRemote::getValue, label::setText);
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(shell);

			shell.pack();
			shell.open();
			shell.setSize(400, 100);

			return shell;
		}
	}

	/**
	 * Create an {@link IObservableValue}, which will contain JSON once it has been
	 * loaded.
	 */
	public static IObservableValue<String> loadJsonFromRemote() {
		IObservableValue<String> json = new WritableValue<>();

		Job loadJsonJob = Job.create("Loading JSON from remote...", monitor -> {

			// Mimic a delay of a real JSON call
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
			}
			json.getRealm().asyncExec(() -> {
				json.setValue("{ 'name': 'Simon', 'lastName': 'Scholz', 'company': 'vogella GmbH' }");
			});
		});
		loadJsonJob.schedule();

		return json;
	}

}
