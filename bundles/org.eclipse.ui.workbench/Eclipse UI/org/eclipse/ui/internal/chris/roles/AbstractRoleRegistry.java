/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.chris.roles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.internal.roles.api.IRoleRegistry;
import org.eclipse.ui.internal.roles.api.IRoleRegistryEvent;
import org.eclipse.ui.internal.roles.api.IRoleRegistryListener;

abstract class AbstractRoleRegistry implements IRoleRegistry {

	private IRoleRegistryEvent roleRegistryEvent;
	private List roleRegistryListeners;
	
	protected List roleDefinitions = Collections.EMPTY_LIST;
	
	protected AbstractRoleRegistry() {
	}

	public void addRoleRegistryListener(IRoleRegistryListener roleRegistryListener) {
		if (roleRegistryListener == null)
			throw new NullPointerException();
			
		if (roleRegistryListeners == null)
			roleRegistryListeners = new ArrayList();
		
		if (!roleRegistryListeners.contains(roleRegistryListener))
			roleRegistryListeners.add(roleRegistryListener);
	}

	public List getRoleDefinitions() {
		return roleDefinitions;
	}
	
	public void removeRoleRegistryListener(IRoleRegistryListener roleRegistryListener) {
		if (roleRegistryListener == null)
			throw new NullPointerException();
			
		if (roleRegistryListeners != null)
			roleRegistryListeners.remove(roleRegistryListener);
	}

	protected void fireRoleRegistryChanged() {
		if (roleRegistryListeners != null) {
			for (int i = 0; i < roleRegistryListeners.size(); i++) {
				if (roleRegistryEvent == null)
					roleRegistryEvent = new RoleRegistryEvent(this);
							
				((IRoleRegistryListener) roleRegistryListeners.get(i)).roleRegistryChanged(roleRegistryEvent);
			}				
		}	
	}
}	
