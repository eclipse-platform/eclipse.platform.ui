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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.swt.WidgetSideEffects;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * This snippet shows how to migrate from the usage of the
 * {@link DataBindingContext} to the {@link ISideEffect} approach.<br/>
 * </p>
 * <p>
 * So basically two logically equal applications are implemented with the two
 * different approaches. The two bindData() methods in the view implementations
 * are the most interesting concerning the databinding migration.
 * </p>
 * <p>
 * The "old" {@link DataBindingContext} approach is shown by the
 * {@link ObservableBeanPerson} and {@link ObservableView} classes, and the
 * {@link TrackedPerson} and {@link TrackedView} classes introduce the "new"
 * {@link ISideEffect} approach.
 * </p>
 *
 * @since 3.2
 *
 */
public class SnippetSideEffectMigration {
	public static void main(String[] args) {
		Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			// create the Person model object
			ObservableBeanPerson observableBeanPerson = new ObservableBeanPerson();
			Shell observableShell = new ObservableView(observableBeanPerson).createShell();

			TrackedPerson trackedPerson = new TrackedPerson();
			Shell trackedShell = new TrackedView(trackedPerson).createShell();

			while (!observableShell.isDisposed() && !trackedShell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});
	}

	// Observable Person model
	static class ObservableBeanPerson {

		public static final String PROPERTY_FIRST_NAME = "firstName";

		public static final String PROPERTY_LAST_NAME = "lastName";

		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

		private String firstName = "Simon";

		private String lastName = "Scholz";

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			propertyChangeSupport.firePropertyChange(PROPERTY_FIRST_NAME, this.firstName, this.firstName = firstName);
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			propertyChangeSupport.firePropertyChange(PROPERTY_LAST_NAME, this.lastName, this.lastName = lastName);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
		}

	}

	static class ObservableView {
		private ObservableBeanPerson person;
		private Text personFirstNameText;
		private Text personLastNameText;

		public ObservableView(ObservableBeanPerson person) {
			this.person = person;
		}

		public Shell createShell() {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			shell.setText("DatabindingContext Sample");

			new Label(shell, SWT.NONE).setText("First Name:");

			personFirstNameText = new Text(shell, SWT.BORDER);

			new Label(shell, SWT.NONE).setText("Last Name:");

			personLastNameText = new Text(shell, SWT.BORDER);

			bindData();

			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(shell);

			// Open and return the Shell
			shell.pack();
			shell.open();

			return shell;
		}

		private void bindData() {
			DataBindingContext dbc = new DataBindingContext();

			IObservableValue personFirstNameObservable = BeanProperties.value(ObservableBeanPerson.PROPERTY_FIRST_NAME)
					.observe(person);
			ISWTObservableValue personFirstNameTextObservable = WidgetProperties.text(SWT.Modify)
					.observe(personFirstNameText);

			dbc.bindValue(personFirstNameTextObservable, personFirstNameObservable,
					new UpdateValueStrategy().setAfterConvertValidator(obj -> {
						if (obj instanceof String && ((String) obj).isEmpty()) {
							return ValidationStatus.error("First Name may not be empty");
						}
						return Status.OK_STATUS;
					}), null);

			IObservableValue personLastNameObservable = BeanProperties.value(ObservableBeanPerson.PROPERTY_LAST_NAME)
					.observe(person);
			ISWTObservableValue personLastNameTextObservable = WidgetProperties.text(SWT.Modify)
					.observe(personLastNameText);

			dbc.bindValue(personLastNameTextObservable, personLastNameObservable,
					new UpdateValueStrategy().setAfterConvertValidator(obj -> {
						if (obj instanceof String && ((String) obj).isEmpty()) {
							return ValidationStatus.error("Last Name may not be empty");
						}
						return Status.OK_STATUS;
					}), null);

			IObservableList<ValidationStatusProvider> validationStatusProviders = dbc.getValidationStatusProviders();
			for (ValidationStatusProvider statusProvider : validationStatusProviders) {
				ControlDecorationSupport.create(statusProvider, SWT.TOP | SWT.LEFT);
			}

			personFirstNameText.addDisposeListener(e -> dbc.dispose());
		}
	}

	// Observable Person model
	static class TrackedPerson {

		private WritableValue<String> firstName = new WritableValue<>("Simon", String.class);

		private WritableValue<String> lastName = new WritableValue<>("Scholz", String.class);

		/**
		 * @return the person's first name
		 * @TrackedGetter
		 */
		public String getFirstName() {
			return firstName.getValue();
		}

		/**
		 * @param firstName
		 *            The first name to set.
		 */
		public void setFirstName(String firstName) {
			this.firstName.setValue(firstName);
		}

		/**
		 * @return the person's last name.
		 * @TrackedGetter
		 */
		public String getLastName() {
			return lastName.getValue();
		}

		/**
		 * @param lastName
		 *            The last name to set.
		 */
		public void setLastName(String lastName) {
			this.lastName.setValue(lastName);
		}
	}

	static class TrackedView {
		private TrackedPerson person;
		private Text personFirstNameText;
		private Text personLastNameText;

		public TrackedView(TrackedPerson person) {
			this.person = person;
		}

		public Shell createShell() {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			shell.setText("ISideEffect Sample");

			new Label(shell, SWT.NONE).setText("First Name:");

			personFirstNameText = new Text(shell, SWT.BORDER);

			new Label(shell, SWT.NONE).setText("Last Name:");

			personLastNameText = new Text(shell, SWT.BORDER);

			bindData();

			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(shell);

			// Open and return the Shell
			shell.pack();
			shell.open();

			return shell;
		}

		private void bindData() {
			// create the observables, which should be bound by the SideEffect
			ISWTObservableValue personFirstNameTextObservable = WidgetProperties.text(SWT.Modify)
					.observe(personFirstNameText);
			ISWTObservableValue personLastNameTextObservable = WidgetProperties.text(SWT.Modify)
					.observe(personLastNameText);

			ISideEffectFactory sideEffectFactory = WidgetSideEffects.createFactory(personFirstNameText);
			sideEffectFactory.create(person::getFirstName, personFirstNameText::setText);

			WritableValue<IStatus> firstNameValidation = new WritableValue<>();
			sideEffectFactory.create(() -> {
				String firstName = (String) personFirstNameTextObservable.getValue();
				if (firstName != null && firstName.isEmpty()) {
					firstNameValidation.setValue(ValidationStatus.error("First Name may not be empty"));
					return;
				}
				person.setFirstName(firstName);
				firstNameValidation.setValue(Status.OK_STATUS);
			});

			ControlDecorationSupport.create(firstNameValidation, SWT.TOP | SWT.LEFT, personFirstNameTextObservable);

			sideEffectFactory.create(person::getLastName, personLastNameText::setText);

			WritableValue<IStatus> lastNameValidation = new WritableValue<>();
			sideEffectFactory.create(() -> {
				String lastName = (String) personLastNameTextObservable.getValue();
				if (lastName != null && lastName.isEmpty()) {
					lastNameValidation.setValue(ValidationStatus.error("Last Name may not be empty"));
					return;
				}
				person.setLastName(lastName);
				lastNameValidation.setValue(Status.OK_STATUS);
			});

			ControlDecorationSupport.create(lastNameValidation, SWT.TOP | SWT.LEFT, personLastNameTextObservable);
		}
	}

}