/*******************************************************************************
 * Copyright (c) 2008, 2010 Ovidio Mallo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 235195)
 *     Ovidio Mallo - bugs 237856, 248877
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

/**
 * @since 1.2
 */
public class WizardPageSupportTest extends AbstractSWTTestCase {

	/**
	 * Bug 235195.
	 */
	@Test
	public void testPageComplete() {
		IWizardPage page = new WizardPage("Page") {
			@Override
			public void createControl(Composite parent) {
				setControl(parent);

				IObservableValue<IStatus> validation = new WritableValue<>(ValidationStatus.ok(), IStatus.class);

				DataBindingContext dbc = new DataBindingContext();
				ValidationProvider validationProvider = new ValidationProvider(
						validation);
				dbc.addValidationStatusProvider(validationProvider);

				WizardPageSupport.create(this, dbc);

				assertTrue(isPageComplete());

				validation.setValue(ValidationStatus.info("INFO"));
				assertTrue(isPageComplete());

				validation.setValue(ValidationStatus.warning("WARNING"));
				assertTrue(isPageComplete());

				validation.setValue(ValidationStatus.error("ERROR"));
				assertFalse(isPageComplete());

				validation.setValue(ValidationStatus.cancel("CANCEL"));
				assertFalse(isPageComplete());
			}
		};

		loadWizardPage(page);
	}

	@Test
	public void testPageCompleteOnValidationStaleness() {
		IWizardPage page = new WizardPage("Page") {
			@Override
			public void createControl(Composite parent) {
				setControl(parent);

				ValidationObservable validation = new ValidationObservable();

				DataBindingContext dbc = new DataBindingContext();
				dbc.addValidationStatusProvider(new ValidationProvider(
						validation));

				WizardPageSupport.create(this, dbc);

				assertTrue(isPageComplete());

				validation.setStale(true);
				assertFalse(isPageComplete());

				validation.setStale(false);
				assertTrue(isPageComplete());
			}
		};

		loadWizardPage(page);
	}

	@Test
	public void testValidationMessageProvider() {
		IWizardPage page = new WizardPage("Page") {
			@Override
			public void createControl(Composite parent) {
				setControl(parent);

				ValidationObservable validation = new ValidationObservable();

				DataBindingContext dbc = new DataBindingContext();
				dbc.addValidationStatusProvider(new ValidationProvider(
						validation));

				WizardPageSupport wizardPageSupport = WizardPageSupport.create(
						this, dbc);
				TestValidationMessageProvider messageProvider = new TestValidationMessageProvider();
				wizardPageSupport.setValidationMessageProvider(messageProvider);

				// We have an info message but display a warning with a
				// different text.
				messageProvider.message = "message1";
				messageProvider.messageType = IMessageProvider.WARNING;

				validation.setValue(ValidationStatus.info("INFO"));
				assertEquals(messageProvider.message, getMessage());
				assertEquals(messageProvider.messageType, getMessageType());
				assertNull(getErrorMessage());
				assertTrue(isPageComplete());

				// We have an error which, however, is no displayed as such, so
				// the error message on the wizard page must be null.
				// Nevertheless,
				// the page must *not* be marked as complete!
				messageProvider.message = "message2";
				messageProvider.messageType = IMessageProvider.NONE;

				validation.setValue(ValidationStatus.error("ERROR"));
				assertEquals(messageProvider.message, getMessage());
				assertEquals(messageProvider.messageType, getMessageType());
				assertNull(getErrorMessage());
				assertFalse(isPageComplete());

				// null should be allowed as message.
				messageProvider.message = null;

				validation.setValue(ValidationStatus.ok());
				assertEquals(messageProvider.message, getMessage());
				assertEquals(messageProvider.messageType, getMessageType());
				assertNull(getErrorMessage());
				assertTrue(isPageComplete());

				// Errors should be displayed using setErrorMessage().
				messageProvider.message = "message3";
				messageProvider.messageType = IMessageProvider.ERROR;

				validation.setValue(ValidationStatus.error("ERROR"));
				assertNull(getMessage());
				assertEquals(IMessageProvider.NONE, getMessageType());
				assertEquals(messageProvider.message, getErrorMessage());
				assertFalse(isPageComplete());
			}
		};

		loadWizardPage(page);
	}

	private void loadWizardPage(IWizardPage page) {
		Wizard wizard = new Wizard() {
			@Override
			public boolean performFinish() {
				return true;
			}
		};
		wizard.addPage(page);

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
	}

	private static class ValidationObservable extends AbstractObservableValue<IStatus> {

		private IStatus value = ValidationStatus.ok();

		private boolean stale = false;

		public ValidationObservable() {
			super(Realm.getDefault());
		}

		@Override
		protected IStatus doGetValue() {
			return value;
		}

		@Override
		protected void doSetValue(IStatus value) {
			IStatus oldValue = this.value;
			this.value = value;
			if (!Objects.equals(oldValue, value)) {
				fireValueChange(Diffs.createValueDiff(oldValue, value));
			}
		}

		@Override
		public boolean isStale() {
			ObservableTracker.getterCalled(this);
			return stale;
		}

		public void setStale(boolean stale) {
			if (this.stale != stale) {
				this.stale = stale;
				if (stale) {
					fireStale();
				} else {
					fireValueChange(Diffs.createValueDiff(value, value));
				}
			}
		}

		@Override
		public Object getValueType() {
			return IStatus.class;
		}
	}

	private static class ValidationProvider extends ValidationStatusProvider {

		private final IObservableValue<IStatus> validation;

		public ValidationProvider(IObservableValue<IStatus> validation) {
			this.validation = validation;
		}

		@Override
		public IObservableValue<IStatus> getValidationStatus() {
			return validation;
		}

		@Override
		public IObservableList<IObservable> getTargets() {
			WritableList<IObservable> targets = new WritableList<>();
			targets.add(validation);
			return targets;
		}

		@Override
		public IObservableList<IObservable> getModels() {
			return Observables.emptyObservableList();
		}
	}

	private static class TestValidationMessageProvider extends
			ValidationMessageProvider {

		public String message;

		public int messageType;

		@Override
		public String getMessage(ValidationStatusProvider statusProvider) {
			return message;
		}

		@Override
		public int getMessageType(ValidationStatusProvider statusProvider) {
			return messageType;
		}
	}
}
