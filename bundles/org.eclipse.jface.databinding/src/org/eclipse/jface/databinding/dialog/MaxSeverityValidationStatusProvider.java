/*******************************************************************************
 * Copyright (c) 2009, 2018 Ovidio Mallo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 248877)
 ******************************************************************************/

package org.eclipse.jface.databinding.dialog;

import java.util.Collection;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.runtime.IStatus;

/*package*/ class MaxSeverityValidationStatusProvider extends ComputedValue<ValidationStatusProvider> {

	private Collection<ValidationStatusProvider> validationStatusProviders;

	public MaxSeverityValidationStatusProvider(DataBindingContext dbc) {
		super(ValidationStatusProvider.class);
		this.validationStatusProviders = dbc.getValidationStatusProviders();
	}

	@Override
	protected ValidationStatusProvider calculate() {
		int maxSeverity = IStatus.OK;
		ValidationStatusProvider maxSeverityProvider = null;
		for (ValidationStatusProvider provider : validationStatusProviders) {
			IStatus status = provider.getValidationStatus().getValue();
			if (status.getSeverity() > maxSeverity) {
				maxSeverity = status.getSeverity();
				maxSeverityProvider = provider;
			}
		}
		return maxSeverityProvider;
	}

	@Override
	public synchronized void dispose() {
		validationStatusProviders = null;
		super.dispose();
	}
}
