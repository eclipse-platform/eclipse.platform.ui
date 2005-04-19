/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;


/**
 * Preference initializer for Editors UI plug-in.
 *
 * @since 3.1
 */
public class EditorsPluginPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 * @since 3.1
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store= EditorsPlugin.getDefault().getPreferenceStore();
		TextEditorPreferenceConstants.initializeDefaultValues(store);
		migrateOverviewRulerPreference(store);
	}

	/**
	 * Migrates the overview ruler preference by re-enabling it.
	 *
	 * @param store the preference store to migrate
	 * @since 3.1
	 */
	private void migrateOverviewRulerPreference(IPreferenceStore store) {
		String preference= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
		String postfix= "_migration"; //$NON-NLS-1$
		String MIGRATED= "migrated_3.1"; //$NON-NLS-1$
		String migrationKey= preference + postfix;

		String migrationValue= store.getString(migrationKey);
		if (!MIGRATED.equals(migrationValue)) {
			store.setValue(migrationKey, MIGRATED);
			if (!store.getBoolean(preference))
				store.setValue(preference, true);
		}
	}
}
