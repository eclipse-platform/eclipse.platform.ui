/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc., IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class SearchPreferenceInitializer extends AbstractPreferenceInitializer {

	public SearchPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		SearchPreferencePage.initDefaults(SearchPlugin.getDefault().getPreferenceStore());
	}
}
