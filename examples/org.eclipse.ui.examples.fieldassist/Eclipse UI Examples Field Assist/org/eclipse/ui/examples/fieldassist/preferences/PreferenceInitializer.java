/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.examples.fieldassist.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.examples.fieldassist.FieldAssistPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_SHOWERRORDECORATION, true);
		store.setDefault(PreferenceConstants.PREF_SHOWERRORMESSAGE, true);
		store.setDefault(PreferenceConstants.PREF_SHOWWARNINGDECORATION, true);
		store.setDefault(PreferenceConstants.PREF_SHOWREQUIREDFIELDDECORATION,
				false);
		store.setDefault(
				PreferenceConstants.PREF_SHOWREQUIREDFIELDLABELINDICATOR, true);
		store.setDefault(PreferenceConstants.PREF_SHOWCONTENTPROPOSALCUE, true);
		store.setDefault(PreferenceConstants.PREF_DECORATOR_HORIZONTALLOCATION,
				PreferenceConstants.PREF_DECORATOR_HORIZONTALLOCATION_LEFT);
		store.setDefault(PreferenceConstants.PREF_DECORATOR_VERTICALLOCATION,
				PreferenceConstants.PREF_DECORATOR_VERTICALLOCATION_CENTER);
		store.setDefault(PreferenceConstants.PREF_DECORATOR_MARGINWIDTH, 0);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTKEY,
				PreferenceConstants.PREF_CONTENTASSISTKEY1);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTKEY_PROPAGATE,
				false);
		store.setDefault(PreferenceConstants.PREF_SHOWSECONDARYPOPUP, true);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTDELAY, 1000);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTRESULT,
				PreferenceConstants.PREF_CONTENTASSISTRESULT_REPLACE);
		store.setDefault(PreferenceConstants.PREF_CONTENTASSISTFILTER,
				PreferenceConstants.PREF_CONTENTASSISTFILTER_CHAR);
	}
}
