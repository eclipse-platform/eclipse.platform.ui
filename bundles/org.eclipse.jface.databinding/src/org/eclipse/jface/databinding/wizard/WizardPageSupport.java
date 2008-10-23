/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Boris Bokowski - bug 218269
 *     Matthew Hall - bug 218269, 240444, 239900
 *     Ashley Cambrell - bug 199179 
 *     Ovidio Mallo - bug 235195, 237856
 *******************************************************************************/
package org.eclipse.jface.databinding.wizard;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.DialogPageSupport;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Connects the validation result from the given data binding context to the
 * given wizard page, updating the wizard page's completion state and its error
 * message accordingly.
 * 
 * <p>
 * The completion state of the wizard page will only be set to <code>true</code>
 * if <i>all</i> of the following conditions are met:
 * <ul>
 * <li>The validation result from the data binding context has none of the
 * severities {@link IStatus#ERROR} or {@link IStatus#CANCEL}.</li>
 * <li>None of the validation status observables of the data binding context is
 * stale.</li>
 * </ul>
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 *
 * @since 1.1
 */
public class WizardPageSupport extends DialogPageSupport {
	private WizardPageSupport(WizardPage wizardPage, DataBindingContext dbc) {
		super(wizardPage, dbc);
	}

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

	protected void handleStatusChanged() {
		super.handleStatusChanged();
		boolean pageComplete = true;
		if (currentStatusStale) {
			pageComplete = false;
		} else if (currentStatus != null) {
			pageComplete = !currentStatus.matches(IStatus.ERROR
					| IStatus.CANCEL);
		}
		((WizardPage) getDialogPage()).setPageComplete(pageComplete);
	}
}
