/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;


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
		TextEditorPreferenceConstants.initializeDefaultValues(EditorsPlugin.getDefault().getPreferenceStore());
	}
}
