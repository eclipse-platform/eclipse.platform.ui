/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.model;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ExternalToolsPreferenceInitializer extends AbstractPreferenceInitializer {

	public ExternalToolsPreferenceInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = ExternalToolsPlugin.getDefault().getPreferenceStore();
		prefs.setDefault(IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION, true);
		prefs.setDefault(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION, true);
	}
}
