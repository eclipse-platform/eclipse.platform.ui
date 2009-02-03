/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *        (through WizardPageSupport.java)
 *     Matthew Hall - initial API and implementation (bug 239900)
 *     Ben Vitale <bvitale3002@yahoo.com> - bug 263100
 ******************************************************************************/

package org.eclipse.jface.databinding.dialog;

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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;

/**
 * Connects the validation result from the given data binding context to the
 * given TitleAreaDialog, updating the dialog's error message accordingly.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @since 1.3
 */
public class TitleAreaDialogSupport {
	/**
	 * Connect the validation result from the given data binding context to the
	 * given TitleAreaDialog. The page's error message will not be set at time
	 * of creation, ensuring that the dialog does not show an error right away.
	 * Upon any validation result change, the dialog's error message will be
	 * updated according to the current validation result.
	 * 
	 * @param dialog
	 * @param dbc
	 * @return an instance of TitleAreaDialogSupport
	 */
	public static TitleAreaDialogSupport create(TitleAreaDialog dialog,
			DataBindingContext dbc) {
		return new TitleAreaDialogSupport(dialog, dbc);
	}

	private TitleAreaDialog dialog;
	private DataBindingContext dbc;
	private IObservableValue aggregateStatus;
	private boolean uiChanged = false;
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

	private TitleAreaDialogSupport(TitleAreaDialog dialogPage,
			DataBindingContext dbc) {
		this.dialog = dialogPage;
		this.dbc = dbc;
		init();
	}

	private void init() {
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

	private void handleUIChanged() {
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

	private void handleStatusChanged() {
		if (dialog.getShell() == null || dialog.getShell().isDisposed())
			return;
		if (currentStatus != null
				&& currentStatus.getSeverity() == IStatus.ERROR) {
			dialog.setMessage(null);
			dialog.setErrorMessage(uiChanged ? currentStatus.getMessage()
					: null);
			if (currentStatusHasException()) {
				handleStatusException();
			}
		} else if (currentStatus != null
				&& currentStatus.getSeverity() != IStatus.OK) {
			int severity = currentStatus.getSeverity();
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
				Assert.isTrue(false, "incomplete switch statement"); //$NON-NLS-1$
				return; // unreachable
			}
			dialog.setErrorMessage(null);
			dialog.setMessage(currentStatus.getMessage(), type);
		} else {
			dialog.setMessage(null);
			dialog.setErrorMessage(null);
		}
	}

	private boolean currentStatusHasException() {
		boolean hasException = false;
		if (currentStatus.getException() != null) {
			hasException = true;
		}
		if (currentStatus instanceof MultiStatus) {
			MultiStatus multiStatus = (MultiStatus) currentStatus;

			for (int i = 0; i < multiStatus.getChildren().length; i++) {
				IStatus status = multiStatus.getChildren()[i];
				if (status.getException() != null) {
					hasException = true;
					break;
				}
			}
		}
		return hasException;
	}

	/**
	 * This is called when a Override to provide custom exception handling and
	 * reporting.
	 */
	private void handleStatusException() {
		if (currentStatus.getException() != null) {
			logThrowable(currentStatus.getException());
		} else if (currentStatus instanceof MultiStatus) {
			MultiStatus multiStatus = (MultiStatus) currentStatus;
			for (int i = 0; i < multiStatus.getChildren().length; i++) {
				IStatus status = multiStatus.getChildren()[i];
				if (status.getException() != null) {
					logThrowable(status.getException());
				}
			}
		}
	}

	private void logThrowable(Throwable throwable) {
		Policy
				.getLog()
				.log(
						new Status(
								IStatus.ERROR,
								Policy.JFACE_DATABINDING,
								IStatus.OK,
								"Unhandled exception: " + throwable.getMessage(), throwable)); //$NON-NLS-1$
	}

	/**
	 * Disposes of this title area dialog support object, removing any listeners
	 * it may have attached.
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
		dialog = null;
	}
}
