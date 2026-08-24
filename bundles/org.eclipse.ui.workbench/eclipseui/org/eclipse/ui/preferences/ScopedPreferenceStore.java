/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
 *     Yves YANG <yves.yang@soyatec.com> -
 *     		Initial Fix for Bug 138078 [Preferences] Preferences Store for i18n support
 *******************************************************************************/
package org.eclipse.ui.preferences;

import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * The ScopedPreferenceStore is an IPreferenceStore that uses the scopes
 * provided in org.eclipse.core.runtime.preferences.
 * <p>
 * A ScopedPreferenceStore does the lookup of a preference based on it's search
 * scopes and sets the value of the preference based on its store scope.
 * </p>
 * <p>
 * The default scope is always included in the search scopes when searching for
 * preference values.
 * </p>
 * <p>
 * <em>Note:</em> This class is kept for backwards compatibility. Users are
 * encouraged to use the JFace preference store in order to avoid a hard
 * dependency on the Eclipse 3.x API.
 * </p>
 *
 * @see org.eclipse.core.runtime.preferences
 * @since 3.1
 */
public class ScopedPreferenceStore extends org.eclipse.jface.preference.ScopedPreferenceStore {

	/**
	 * Create a new instance of the receiver. Store the values in context in the
	 * node looked up by qualifier. <strong>NOTE:</strong> Any instance of
	 * ScopedPreferenceStore should call
	 *
	 * @param context              the scope to store to
	 * @param qualifier            the qualifier used to look up the preference node
	 * @param defaultQualifierPath the qualifier used when looking up the defaults
	 */
	public ScopedPreferenceStore(IScopeContext context, String qualifier, String defaultQualifierPath) {
		super(context, qualifier, defaultQualifierPath);
	}

	/**
	 * Create a new instance of the receiver. Store the values in context in the
	 * node looked up by qualifier.
	 *
	 * @param context   the scope to store to
	 * @param qualifier the qualifer used to look up the preference node
	 */
	public ScopedPreferenceStore(IScopeContext context, String qualifier) {
		super(context, qualifier);
	}

}
