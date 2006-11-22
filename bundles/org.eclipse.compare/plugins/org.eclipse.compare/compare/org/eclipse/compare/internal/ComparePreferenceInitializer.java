/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ComparePreferenceInitializer extends AbstractPreferenceInitializer {

	public ComparePreferenceInitializer() {
		// Nothing to do
	}

	public void initializeDefaultPreferences() {
		IPreferenceStore store = CompareUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(ICompareUIConstants.PREF_NAVIGATION_END_ACTION, ICompareUIConstants.PREF_VALUE_PROMPT);
		store.setDefault(ICompareUIConstants.PREF_NAVIGATION_END_ACTION_LOCAL, ICompareUIConstants.PREF_VALUE_LOOP);
		ComparePreferencePage.initDefaults(store);
	}

}
