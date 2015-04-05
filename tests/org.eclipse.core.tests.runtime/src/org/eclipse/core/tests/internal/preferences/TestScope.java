/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.preferences;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * @since 3.0
 */
public class TestScope implements IScopeContext {

	public static final String SCOPE = "test"; //$NON-NLS-1$

	public TestScope() {
		super();
	}

	@Override
	public IPath getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return SCOPE;
	}

	@Override
	public IEclipsePreferences getNode(String qualifier) {
		return (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(SCOPE).node(qualifier);
	}
}