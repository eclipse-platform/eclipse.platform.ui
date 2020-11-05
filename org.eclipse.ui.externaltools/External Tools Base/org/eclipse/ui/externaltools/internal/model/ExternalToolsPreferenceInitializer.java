/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
package org.eclipse.ui.externaltools.internal.model;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ExternalToolsPreferenceInitializer extends AbstractPreferenceInitializer {

	public ExternalToolsPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = ExternalToolsPlugin.getDefault().getPreferenceStore();
		prefs.setDefault(IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION, true);
		prefs.setDefault(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION, true);
	}
}
