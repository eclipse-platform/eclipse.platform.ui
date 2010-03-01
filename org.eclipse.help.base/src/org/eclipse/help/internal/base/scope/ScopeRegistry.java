/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.base.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.HelpBasePlugin;

public class ScopeRegistry {

	public static final String SCOPE_XP_NAME = "org.eclipse.help.base.scope"; //$NON-NLS-1$
	public static final String ENABLEMENT_SCOPE_ID = "org.eclipse.help.enablement"; //$NON-NLS-1$
	public static final String SEARCH_SCOPE_SCOPE_ID = "org.eclipse.help.searchscope"; //$NON-NLS-1$

	private static List scopes = null;
	
	private static ScopeRegistry instance;

	private boolean initialized = false;
	
	private ScopeRegistry() {
	}
	
	public static ScopeRegistry getInstance() {
		if (instance == null) {
			instance = new ScopeRegistry();
		}
		return instance;
	}
	
	public AbstractHelpScope getScope(String id) {
		if (id == null) {
			return new UniversalScope();
		}
		readScopes();
		// Lookup in scope registry
		for (Iterator iter = scopes.iterator(); iter.hasNext();) {
			ScopeHandle handle = (ScopeHandle) iter.next();
			if (id.equals(handle.getId())) {
				return handle.getScope();
			}
		}
		return null;
	}

	synchronized private void readScopes() {
		if (initialized ) {
			return;
		}	
		scopes = new ArrayList();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry
				.getConfigurationElementsFor(SCOPE_XP_NAME);
		for (int i = 0; i < elements.length; i++) {

			Object obj = null;
			try {
				obj = elements[i].createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				HelpBasePlugin.logError("Create extension failed:[" //$NON-NLS-1$
						+ SCOPE_XP_NAME + "].", e); //$NON-NLS-1$
			}
			if (obj instanceof AbstractHelpScope) {
				String id = elements[i].getAttribute("id"); //$NON-NLS-1$
				ScopeHandle filter = new ScopeHandle(id, (AbstractHelpScope) obj);
				scopes.add(filter);
			}
		}
		initialized = true;
	}
	
	public ScopeHandle[] getScopes() {
		readScopes();
		return (ScopeHandle[]) scopes.toArray(new ScopeHandle[scopes.size()]);
	}

}
