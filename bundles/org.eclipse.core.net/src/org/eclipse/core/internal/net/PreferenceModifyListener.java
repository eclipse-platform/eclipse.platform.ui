/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.net;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class PreferenceModifyListener extends
		org.eclipse.core.runtime.preferences.PreferenceModifyListener {

	public PreferenceModifyListener() {
		// Nothing to do
	}
	
	public IEclipsePreferences preApply(IEclipsePreferences node) {
		try {
			if (node.nodeExists(InstanceScope.SCOPE)) {
				((ProxyManager)ProxyManager.getProxyManager()).migrateInstanceScopePreferences(
								node.node(InstanceScope.SCOPE).node(Activator.ID), 
								node.node(ConfigurationScope.SCOPE).node(Activator.ID), false);
			}
		} catch (BackingStoreException e) {
			Activator.logError("Could not access instance preferences", e); //$NON-NLS-1$
		}
		return super.preApply(node);
	}
}
