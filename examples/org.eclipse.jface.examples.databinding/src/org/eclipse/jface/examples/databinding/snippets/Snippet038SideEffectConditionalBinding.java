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

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
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
 * This snippet shows how to use the {@link ISideEffect} class with conditional
 * elements in it.<br>
 * <br>
 * Also see the following part of the ISideEffect's JavaDoc:
 *
 * <ul>
 * <li>The {@link ISideEffect} can self-optimize based on branches in the run
 * method. It will remove listeners from any {@link IObservable} which wasn't
 * used on the most recent run. In the above example, there is no need to listen
 * to the lastName field when showFullNamePreference is false.
 * <li>The {@link ISideEffect} will batch changes together and run
 * asynchronously. If firstName and lastName change at the same time, the
 * {@link ISideEffect} will only run once.
 * <li>Since the {@link ISideEffect} doesn't need to be explicitly attached to
 * the observables it affects, it is impossible for it to get out of sync with
 * the underlying data.
 * </ul>
 */
public class Snippet038SideEffectConditionalBinding {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			// Create the Person model object
			final Shell shell = new View(new Person()).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	/** Observable Person model. */
	static class Person {

		private final WritableValue<String> firstName = new WritableValue<>("Simon", String.class);

		private final WritableValue<String> lastName = new WritableValue<>("Scholz", String.class);

		public String getFirstName() {
			return firstName.getValue();
		}

		public void setFirstName(String firstName) {
			this.firstName.setValue(firstName);
		}

		public String getLastName() {
			return lastName.getValue();
		}

		public void setLastName(String lastName) {
			this.lastName.setValue(lastName);
		}
	}

	static class View {
		private final Person person;
		private Text personNameText;
		private Button showDescriptionButton;
		private Button changeNameButton;

		public View(Person person) {
			this.person = person;
		}

		public Shell createShell() {
			Shell shell = new Shell();
			GridLayoutFactory.swtDefaults().applyTo(shell);
			GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

			personNameText = new Text(shell, SWT.READ_ONLY);
			gridDataFactory.applyTo(personNameText);

			showDescriptionButton = new Button(shell, SWT.CHECK);
			showDescriptionButton.setText("Show full name");
			gridDataFactory.applyTo(showDescriptionButton);

			changeNameButton = new Button(shell, SWT.PUSH);
			changeNameButton.setText("Change Last Name");
			gridDataFactory.applyTo(changeNameButton);
			changeNameButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// If the showDescriptionButton isn't checked this won't cause the ISideEffect
					// to be run
					person.setLastName("Xenos");
				}
			});

			bindData();

			shell.pack();
			shell.open();

			return shell;
		}

		private void bindData() {

			IObservableValue<Boolean> showDescription = WidgetProperties.buttonSelection()
					.observe(showDescriptionButton);

			// Create a conditional ISideEffect
			ISideEffect personNameSideEffect = ISideEffect.create(() -> {
				String name = showDescription.getValue() ? person.getFirstName() + " " + person.getLastName()
						: person.getFirstName();
				personNameText.setText(name);
			});

			// Dispose the ISideEffect object on dispose
			personNameText.addDisposeListener(e -> {
				personNameSideEffect.dispose();
			});
		}
	}

}
