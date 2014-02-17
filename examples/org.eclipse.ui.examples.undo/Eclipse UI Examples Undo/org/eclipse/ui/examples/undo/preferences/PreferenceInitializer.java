/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.undo.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.examples.undo.UndoPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = UndoPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_CONFIRMUNDO, true);
		store.setDefault(PreferenceConstants.PREF_UNDOLIMIT,
				25);
		store.setDefault(PreferenceConstants.PREF_SHOWDEBUG, false);

	}

}
