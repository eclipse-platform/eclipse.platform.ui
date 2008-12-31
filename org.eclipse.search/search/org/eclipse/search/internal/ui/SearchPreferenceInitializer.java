/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc., IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class SearchPreferenceInitializer extends AbstractPreferenceInitializer {

	public SearchPreferenceInitializer() {
	}

	public void initializeDefaultPreferences() {
		SearchPreferencePage.initDefaults(SearchPlugin.getDefault().getPreferenceStore());
	}
}
