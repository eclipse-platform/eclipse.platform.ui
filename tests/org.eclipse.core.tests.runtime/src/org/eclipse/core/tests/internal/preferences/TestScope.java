/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.preferences;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @since 3.0
 */
public class TestScope extends EclipsePreferences implements IScopeContext {

	public static final String SCOPE = "test"; //$NON-NLS-1$

	public TestScope() {
		super(null, null);
	}

	private TestScope(EclipsePreferences parent, String key) {
		super(parent, key);
	}

	public void flush() throws BackingStoreException {
		// do nothing (don't persist to disk)
	}

	public void sync() throws BackingStoreException {
		// do nothing (don't persist to disk)
	}

	public String getName() {
		return SCOPE;
	}

	public IEclipsePreferences getNode(String qualifier) {
		return (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(SCOPE).node(qualifier);
	}

	public IPath getLocation() {
		return null;
	}

	public IEclipsePreferences create(EclipsePreferences nodeParent, String nodeName) {
		return new TestScope(nodeParent, nodeName);
	}
}