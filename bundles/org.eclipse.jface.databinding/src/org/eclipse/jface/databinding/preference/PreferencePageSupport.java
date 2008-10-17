/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 239900)
 *******************************************************************************/
package org.eclipse.jface.databinding.preference;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.DialogPageSupport;
import org.eclipse.jface.preference.PreferencePage;

/**
 * Connects the validation result from the given data binding context to the
 * given preference page, updating the preference page's valid state and its
 * error message accordingly.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.3
 */
public class PreferencePageSupport extends DialogPageSupport {
	private PreferencePage preferencePage;

	private PreferencePageSupport(PreferencePage preferencePage,
			DataBindingContext dbc) {
		super(preferencePage, dbc);
		this.preferencePage = preferencePage;
	}

	/**
	 * Connect the validation result from the given data binding context to the
	 * given preference page. Upon creation, the preference page support will
	 * use the context's validation result to determine whether the page is
	 * valid. The page's error message will not be set at this time ensuring
	 * that the preference page does not show an error right away. Upon any
	 * validation result change, {@link PreferencePage#setValid(boolean)} will
	 * be called reflecting the new validation result, and the preference page's
	 * error message will be updated according to the current validation result.
	 * 
	 * @param preferencePage
	 * @param dbc
	 * @return an instance of PreferencePageSupport
	 */
	public static PreferencePageSupport create(PreferencePage preferencePage,
			DataBindingContext dbc) {
		return new PreferencePageSupport(preferencePage, dbc);
	}

	protected void handleStatusChanged() {
		super.handleStatusChanged();
		boolean valid = true;
		if (currentStatusStale) {
			valid = false;
		} else if (currentStatus != null) {
			valid = !currentStatus.matches(IStatus.ERROR | IStatus.CANCEL);
		}
		preferencePage.setValid(valid);
	}
}
