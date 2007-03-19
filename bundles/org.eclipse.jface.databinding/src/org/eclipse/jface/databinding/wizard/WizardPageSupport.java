/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.wizard;

import java.util.Iterator;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IStatus;
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
	private IListChangeListener bindingsListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			ListDiff diff = event.diff;
			ListDiffEntry[] differences = diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry listDiffEntry = differences[i];
				Binding binding = (Binding) listDiffEntry.getElement();
				if (listDiffEntry.isAddition()) {
					binding.getTarget().addChangeListener(uiChangeListener);
				} else {
					binding.getTarget().removeChangeListener(uiChangeListener);
				}
			}
		}
	};
	private IStatus currentStatus;

	protected void init() {
		aggregateStatus = new AggregateValidationStatus(dbc.getBindings(),
				AggregateValidationStatus.MAX_SEVERITY);
		aggregateStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {

				currentStatus = (IStatus) event.diff.getNewValue();
				handleStatusChanged();
			}
		});
		currentStatus = (IStatus) aggregateStatus.getValue();
		handleStatusChanged();
		dbc.getBindings().addListChangeListener(bindingsListener);
		for (Iterator it = dbc.getBindings().iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.getTarget().addChangeListener(uiChangeListener);
		}
	}

	protected void handleUIChanged() {
		uiChanged = true;
		if (currentStatus != null) {
			handleStatusChanged();
		}
		dbc.getBindings().removeListChangeListener(bindingsListener);
		for (Iterator it = dbc.getBindings().iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.getTarget().removeChangeListener(uiChangeListener);
		}
	}

	protected void handleStatusChanged() {
		if (currentStatus != null
				&& currentStatus.getSeverity() == IStatus.ERROR) {
			wizardPage.setPageComplete(false);
			wizardPage.setErrorMessage(uiChanged ? currentStatus.getMessage()
					: null);
		} else {
			wizardPage.setPageComplete(true);
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
			for (Iterator it = dbc.getBindings().iterator(); it.hasNext();) {
				Binding binding = (Binding) it.next();
				binding.getTarget().removeChangeListener(uiChangeListener);
			}
			dbc.getBindings().removeListChangeListener(bindingsListener);
		}
		aggregateStatus = null;
		dbc = null;
		uiChangeListener = null;
		bindingsListener = null;
		wizardPage = null;
	}
}
