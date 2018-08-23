/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
package org.eclipse.compare.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ComparePreferenceInitializer extends AbstractPreferenceInitializer {

	public ComparePreferenceInitializer() {
		// Nothing to do
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CompareUIPlugin.getDefault().getPreferenceStore();
		ComparePreferencePage.initDefaults(store);
	}

}
