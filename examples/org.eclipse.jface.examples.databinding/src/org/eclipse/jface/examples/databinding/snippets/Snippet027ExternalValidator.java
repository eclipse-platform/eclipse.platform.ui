/*******************************************************************************
 * Copyright (c) 2008, 2014 Code 9 Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Aniszczyk <zx@code9.com> - initial API and implementation
 *     Boris Bokowski, IBM - minor changes
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This snippet demonstrates how to integrate an external validator
 *
 * @since 3.5
 */
public class Snippet027ExternalValidator extends WizardPage {

	private Text nameValue;
	private Text emailValue;
	private Text phoneNumberValue;

	private Contact contact;

	// Minimal JavaBeans support
	public static abstract class AbstractModelObject {
		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
				this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName,
					listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName,
					listener);
		}

		protected void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue,
					newValue);
		}
	}

	static class Contact extends AbstractModelObject {
		String name;
		String email;
		String phoneNumber;

		public Contact(String name, String email, String number) {
			this.name = name;
			this.email = email;
			this.phoneNumber = number;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			String oldValue = this.name;
			this.name = name;
			firePropertyChange("name", oldValue, name);
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			String oldValue = this.email;
			this.email = email;
			firePropertyChange("email", oldValue, email);
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String number) {
			String oldValue = this.phoneNumber;
			this.phoneNumber = number;
			firePropertyChange("phoneNumber", oldValue, number);
		}

		public IStatus validate() {
			if (name.indexOf(' ') == -1) {
				return ValidationStatus
						.error("Please enter both first and last name separated by a space.");
			}
			if (email.indexOf('@') == -1) {
				return ValidationStatus
				.error("Please enter a valid email address containing '@'.");
			}
			if (!phoneNumber.startsWith("+")) {
				return ValidationStatus
				.error("Please enter the phone number in international format starting with '+'.");
			}
			return Status.OK_STATUS;
		}

	}

	/**
	 * Create the wizard
	 */
	public Snippet027ExternalValidator() {
		super("snippet024");
		setTitle("Snippet 024 - External Validation");
		setDescription("Please enter contact details.");
	}

	/**
	 * Create contents of the wizard
	 *
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		setControl(container);

		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Name");

		nameValue = new Text(container, SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		nameValue.setLayoutData(gd);

		final Label emailLabel = new Label(container, SWT.NONE);
		emailLabel.setText("Email");

		emailValue = new Text(container, SWT.BORDER);
		emailValue.setLayoutData(gd);

		final Label phoneLabel = new Label(container, SWT.NONE);
		phoneLabel.setText("Phone");

		phoneNumberValue = new Text(container, SWT.BORDER);
		phoneNumberValue.setLayoutData(gd);

		contact = new Contact("BorisBokowski", "boris.at.somecompany.com",
				"1-123-456-7890");

		bindUI();
	}

	private void bindUI() {
		DataBindingContext dbc = new DataBindingContext();

		final IObservableValue name = BeanProperties.value(contact.getClass(), "name").observe(contact);

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(nameValue), name,
				null, null);

		final IObservableValue email = BeanProperties.value(contact.getClass(), "email").observe(contact);

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(emailValue),
				email, null, null);

		final IObservableValue phone = BeanProperties.value(contact.getClass(), "phoneNumber").observe(contact);

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(phoneNumberValue),
				phone, null, null);

		MultiValidator validator = new MultiValidator() {
			@Override
			protected IStatus validate() {

				// Everything accessed here will trigger re-validation.
				name.getValue();
				email.getValue();
				phone.getValue();

				System.out.println("Validating...");

				return contact.validate();
			}
		};
		dbc.addValidationStatusProvider(validator);

		WizardPageSupport.create(this, dbc);
	}

	static class ExternalValidationWizard extends Wizard {
		@Override
		public void addPages() {
			addPage(new Snippet027ExternalValidator());
		}

		@Override
		public String getWindowTitle() {
			return "Snippet 024 - External Validation";
		}

		@Override
		public boolean performFinish() {
			return true;
		}
	}

	public static void main(String[] args) {
		Display display = new Display();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				IWizard wizard = new ExternalValidationWizard();
				WizardDialog dialog = new WizardDialog(null, wizard);
				dialog.open();

				// The SWT event loop
				Display display = Display.getCurrent();
				while (dialog.getShell() != null
						&& !dialog.getShell().isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});

		display.dispose();
	}
}
