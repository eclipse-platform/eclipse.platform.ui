/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Boris Bokowski - bug 218269
 *     Matthew Hall - bug 218269
 *******************************************************************************/
package org.eclipse.jface.databinding.wizard;

import java.util.Iterator;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Connects the validation result from the given data binding context to the
 * given wizard page, updating the wizard page's completion state and its error
 * message accordingly.
 * 
 * This class is not intended to be extended by clients.
 *
 * @since 1.1
 */
public class WizardPageSupport {

	private WizardPage wizardPage;
	private DataBindingContext dbc;
	private AggregateValidationStatus aggregateStatus;
	private boolean uiChanged = false;

	/**
	 * Connect the validation result from the given data binding context to the
	 * given wizard page. Upon creation, the wizard page support will use the
	 * context's validation result to determine whether the page is complete.
	 * The page's error message will not be set at this time ensuring that the
	 * wizard page does not show an error right away. Upon any validation result
	 * change, {@link WizardPage#setPageComplete(boolean)} will be called
	 * reflecting the new validation result, and the wizard page's error message
	 * will be updated according to the current validation result.
	 * 
	 * @param wizardPage
	 * @param dbc
	 * @return an instance of WizardPageSupport
	 */
	public static WizardPageSupport create(WizardPage wizardPage,
			DataBindingContext dbc) {
		return new WizardPageSupport(wizardPage, dbc);
	}

	private WizardPageSupport(WizardPage wizardPage, DataBindingContext dbc) {
		this.wizardPage = wizardPage;
		this.dbc = dbc;
		init();
	}

	private IChangeListener uiChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent event) {
			handleUIChanged();
		}
	};
	private IListChangeListener validationStatusProvidersListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			ListDiff diff = event.diff;
			ListDiffEntry[] differences = diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry listDiffEntry = differences[i];
				ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) listDiffEntry
						.getElement();
				IObservableList targets = validationStatusProvider.getTargets();
				if (listDiffEntry.isAddition()) {
					targets
							.addListChangeListener(validationStatusProviderTargetsListener);
					for (Iterator it = targets.iterator(); it.hasNext();) {
						((IObservable) it.next())
								.addChangeListener(uiChangeListener);
					}
				} else {
					targets
							.removeListChangeListener(validationStatusProviderTargetsListener);
					for (Iterator it = targets.iterator(); it.hasNext();) {
						((IObservable) it.next())
								.removeChangeListener(uiChangeListener);
					}
				}
			}
		}
	};
	private IListChangeListener validationStatusProviderTargetsListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			ListDiff diff = event.diff;
			ListDiffEntry[] differences = diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry listDiffEntry = differences[i];
				IObservable target = (IObservable) listDiffEntry.getElement();
				if (listDiffEntry.isAddition()) {
					target.addChangeListener(uiChangeListener);
				} else {
					target.removeChangeListener(uiChangeListener);
				}
			}
		}
	};
	private IStatus currentStatus;

	protected void init() {
		aggregateStatus = new AggregateValidationStatus(dbc
				.getValidationStatusProviders(),
				AggregateValidationStatus.MAX_SEVERITY);
		aggregateStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {

				currentStatus = (IStatus) event.diff.getNewValue();
				handleStatusChanged();
			}
		});
		currentStatus = (IStatus) aggregateStatus.getValue();
		handleStatusChanged();
		dbc.getValidationStatusProviders().addListChangeListener(
				validationStatusProvidersListener);
		for (Iterator it = dbc.getValidationStatusProviders().iterator(); it
				.hasNext();) {
			ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) it
					.next();
			IObservableList targets = validationStatusProvider.getTargets();
			targets
					.addListChangeListener(validationStatusProviderTargetsListener);
			for (Iterator iter = targets.iterator(); iter.hasNext();) {
				((IObservable) iter.next()).addChangeListener(uiChangeListener);
			}
		}
	}

	protected void handleUIChanged() {
		uiChanged = true;
		if (currentStatus != null) {
			handleStatusChanged();
		}
		dbc.getValidationStatusProviders().removeListChangeListener(
				validationStatusProvidersListener);
		for (Iterator it = dbc.getValidationStatusProviders().iterator(); it
				.hasNext();) {
			ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) it
					.next();
			IObservableList targets = validationStatusProvider.getTargets();
			targets
					.removeListChangeListener(validationStatusProviderTargetsListener);
			for (Iterator iter = targets.iterator(); iter.hasNext();) {
				((IObservable) iter.next())
						.removeChangeListener(uiChangeListener);
			}
		}
	}

	protected void handleStatusChanged() {
		if (currentStatus != null
				&& currentStatus.getSeverity() == IStatus.ERROR) {
			wizardPage.setPageComplete(false);
			wizardPage.setMessage(null);
			wizardPage.setErrorMessage(uiChanged ? currentStatus.getMessage()
					: null);
		} else if (currentStatus != null
				&& currentStatus.getSeverity() != IStatus.OK) {
			int severity = currentStatus.getSeverity();
			wizardPage.setPageComplete((severity & IStatus.CANCEL) != 0);
			int type;
			switch (severity) {
			case IStatus.OK:
				type = IMessageProvider.NONE;
				break;
			case IStatus.CANCEL:
				type = IMessageProvider.NONE;
				break;
			case IStatus.INFO:
				type = IMessageProvider.INFORMATION;
				break;
			case IStatus.WARNING:
				type = IMessageProvider.WARNING;
				break;
			case IStatus.ERROR:
				type = IMessageProvider.ERROR;
				break;
			default:
				throw new AssertionFailedException(
						"incomplete switch statement"); //$NON-NLS-1$
			}
			wizardPage.setErrorMessage(null);
			wizardPage.setMessage(currentStatus.getMessage(), type);
		} else {
			wizardPage.setPageComplete(true);
			wizardPage.setMessage(null);
			wizardPage.setErrorMessage(null);
		}
	}

	/**
	 * Disposes of this wizard page support object, removing any listeners it
	 * may have attached.
	 */
	public void dispose() {
		aggregateStatus.dispose();
		if (!uiChanged) {
			for (Iterator it = dbc.getValidationStatusProviders().iterator(); it
					.hasNext();) {
				ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) it
						.next();
				IObservableList targets = validationStatusProvider.getTargets();
				targets
						.removeListChangeListener(validationStatusProviderTargetsListener);
				for (Iterator iter = targets.iterator(); iter.hasNext();) {
					((IObservable) iter.next())
							.removeChangeListener(uiChangeListener);
				}
			}
			dbc.getValidationStatusProviders().removeListChangeListener(
					validationStatusProvidersListener);
		}
		aggregateStatus = null;
		dbc = null;
		uiChangeListener = null;
		validationStatusProvidersListener = null;
		validationStatusProviderTargetsListener = null;
		wizardPage = null;
	}
}
