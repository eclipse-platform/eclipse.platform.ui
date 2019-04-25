/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *        (through WizardPageSupport.java)
 *     Matthew Hall - initial API and implementation (bug 239900)
 *     Ben Vitale <bvitale3002@yahoo.com> - bug 263100
 *     Kai Schlamp - bug 275058
 *     Matthew Hall - bugs 275058, 278550
 *     Ovidio Mallo - bug 248877
 ******************************************************************************/

package org.eclipse.jface.databinding.dialog;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.util.Policy;
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
	private IValidationMessageProvider messageProvider = new ValidationMessageProvider();
	private IObservableValue<ValidationStatusProvider> aggregateStatusProvider;
	private boolean uiChanged = false;
	private IChangeListener uiChangeListener = new IChangeListener() {
		@Override
		public void handleChange(ChangeEvent event) {
			handleUIChanged();
		}
	};
	private IListChangeListener<ValidationStatusProvider> validationStatusProvidersListener = new IListChangeListener<ValidationStatusProvider>() {
		@Override
		public void handleListChange(ListChangeEvent<? extends ValidationStatusProvider> event) {
			for (ListDiffEntry<? extends ValidationStatusProvider> listDiffEntry : event.diff.getDifferences()) {
				IObservableList<IObservable> targets = listDiffEntry.getElement().getTargets();
				if (listDiffEntry.isAddition()) {
					targets.addListChangeListener(validationStatusProviderTargetsListener);
					for (IObservable observable : targets) {
						observable.addChangeListener(uiChangeListener);
					}
				} else {
					targets.removeListChangeListener(validationStatusProviderTargetsListener);
					for (IObservable observable : targets) {
						observable.removeChangeListener(uiChangeListener);
					}
				}
			}
		}
	};
	private IListChangeListener<IObservable> validationStatusProviderTargetsListener = new IListChangeListener<IObservable>() {
		@Override
		public void handleListChange(ListChangeEvent<? extends IObservable> event) {
			for (ListDiffEntry<? extends IObservable> listDiffEntry : event.diff.getDifferences()) {
				if (listDiffEntry.isAddition()) {
					listDiffEntry.getElement().addChangeListener(uiChangeListener);
				} else {
					listDiffEntry.getElement().removeChangeListener(uiChangeListener);
				}
			}
		}
	};
	private ValidationStatusProvider currentStatusProvider;
	private IStatus currentStatus;

	private TitleAreaDialogSupport(TitleAreaDialog dialogPage,
			DataBindingContext dbc) {
		this.dialog = dialogPage;
		this.dbc = dbc;
		init();
	}

	/**
	 * Sets the {@link IValidationMessageProvider} to use for providing the
	 * message text and message type to display on the title area dialog.
	 *
	 * @param messageProvider
	 *            The {@link IValidationMessageProvider} to use for providing
	 *            the message text and message type to display on the title area
	 *            dialog.
	 *
	 * @since 1.4
	 */
	public void setValidationMessageProvider(
			IValidationMessageProvider messageProvider) {
		this.messageProvider = messageProvider;
		handleStatusChanged();
	}

	private void init() {
		ObservableTracker.setIgnore(true);
		try {
			aggregateStatusProvider = new MaxSeverityValidationStatusProvider(dbc);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		aggregateStatusProvider.addValueChangeListener(event -> statusProviderChanged());
		dialog.getShell().addDisposeListener(e -> dispose());
		statusProviderChanged();
		dbc.getValidationStatusProviders().addListChangeListener(validationStatusProvidersListener);
		for (ValidationStatusProvider provider : dbc.getValidationStatusProviders()) {
			IObservableList<IObservable> targets = provider.getTargets();
			targets.addListChangeListener(validationStatusProviderTargetsListener);
			for (IObservable observable : targets) {
				observable.addChangeListener(uiChangeListener);
			}
		}
	}

	private void statusProviderChanged() {
		currentStatusProvider = aggregateStatusProvider.getValue();
		if (currentStatusProvider != null) {
			currentStatus = currentStatusProvider
					.getValidationStatus().getValue();
		} else {
			currentStatus = null;
		}
		handleStatusChanged();
	}

	private void handleUIChanged() {
		uiChanged = true;
		if (currentStatus != null) {
			handleStatusChanged();
		}
		dbc.getValidationStatusProviders().removeListChangeListener(validationStatusProvidersListener);
		for (ValidationStatusProvider provider : dbc.getValidationStatusProviders()) {
			IObservableList<IObservable> targets = provider.getTargets();
			targets.removeListChangeListener(validationStatusProviderTargetsListener);
			for (IObservable observable : targets) {
				observable.removeChangeListener(uiChangeListener);
			}
		}
	}

	private void handleStatusChanged() {
		if (dialog.getShell() == null || dialog.getShell().isDisposed())
			return;
		String message = messageProvider.getMessage(currentStatusProvider);
		int type = messageProvider.getMessageType(currentStatusProvider);
		if (type == IMessageProvider.ERROR) {
			dialog.setMessage(null);
			dialog.setErrorMessage(uiChanged ? message : null);
			if (currentStatus != null && currentStatusHasException()) {
				handleStatusException();
			}
		} else {
			dialog.setErrorMessage(null);
			dialog.setMessage(message, type);
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
		if (aggregateStatusProvider != null)
			aggregateStatusProvider.dispose();
		if (dbc != null && !uiChanged) {
			for (ValidationStatusProvider provider : dbc.getValidationStatusProviders()) {
				IObservableList<IObservable> targets = provider.getTargets();
				targets.removeListChangeListener(validationStatusProviderTargetsListener);
				for (IObservable observable : targets) {
					observable.removeChangeListener(uiChangeListener);
				}
			}
			dbc.getValidationStatusProviders().removeListChangeListener(validationStatusProvidersListener);
		}
		aggregateStatusProvider = null;
		dbc = null;
		uiChangeListener = null;
		validationStatusProvidersListener = null;
		validationStatusProviderTargetsListener = null;
		dialog = null;
	}
}
