/*******************************************************************************
 * Copyright (c) 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * {@link ISideEffect#runOnce(java.util.function.Supplier, java.util.function.Consumer)}
 * method to react properly, when an observable is changed inside an async
 * action, e.g., a Job.
 *
 * @since 3.2
 *
 */
public class SnippetSideEffectRunOnce {
	public static void main(String[] args) {
		Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			final Shell shell = new View().createShell();
			// The SWT event loop
			Display d = Display.getCurrent();
			while (!shell.isDisposed()) {
				if (!d.readAndDispatch()) {
					d.sleep();
				}
			}
		});
	}

	static class View {

		public Shell createShell() {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);

			Label label = new Label(shell, SWT.NONE);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			label.setText("Loading JSON...");

			IObservableValue<String> loadJsonFromRemote = loadJsonFromRemote();
			ISideEffect.consumeOnceAsync(loadJsonFromRemote::getValue, System.out::println);
			ISideEffect.consumeOnceAsync(loadJsonFromRemote::getValue, label::setText);
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(shell);

			// Open and return the Shell
			shell.pack();
			shell.open();

			return shell;
		}
	}

	/**
	 * Create an {@link IObservableValue}, which will contain JSON once it has
	 * been loaded.
	 *
	 * @return {@link IObservableValue}
	 */
	public static IObservableValue<String> loadJsonFromRemote() {
		IObservableValue<String> json = new WritableValue<>();

		Job loadJsonJob = Job.create("Loading JSON from remote", monitor -> {

			// mimic a delay of a real json call
			try {
				TimeUnit.SECONDS.sleep(2);
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
