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
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.roles.IRoleActivationService;
import org.eclipse.ui.roles.IRoleActivationServiceEvent;
import org.eclipse.ui.roles.IRoleActivationServiceListener;

public final class RoleActivationService implements IRoleActivationService {

	private SortedSet activeRoleIds;
	private IRoleActivationServiceEvent roleActivationServiceEvent;
	private List roleActivationServiceListeners;

	public RoleActivationService() {
	}

	public void activateRole(String roleId) {
		if (roleId == null)
			throw new NullPointerException();

		if (activeRoleIds == null)
			activeRoleIds = new TreeSet();
			
		if (activeRoleIds.add(roleId))
			fireRoleActivationServiceChanged();
	}

	public void addRoleActivationServiceListener(IRoleActivationServiceListener roleActivationServiceListener) {
		if (roleActivationServiceListener == null)
			throw new NullPointerException();
			
		if (roleActivationServiceListeners == null)
			roleActivationServiceListeners = new ArrayList();
		
		if (!roleActivationServiceListeners.contains(roleActivationServiceListener))
			roleActivationServiceListeners.add(roleActivationServiceListener);
	}

	public void deactivateRole(String roleId) {
		if (roleId == null)
			throw new NullPointerException();

		if (activeRoleIds != null && activeRoleIds.remove(roleId)) {			
			if (activeRoleIds.isEmpty())
				activeRoleIds = null;

			fireRoleActivationServiceChanged();
		}			
	}

	public SortedSet getActiveRoleIds() {
		return activeRoleIds != null ? Collections.unmodifiableSortedSet(activeRoleIds) : Util.EMPTY_SORTED_SET;
	}
	
	public void removeRoleActivationServiceListener(IRoleActivationServiceListener roleActivationServiceListener) {
		if (roleActivationServiceListener == null)
			throw new NullPointerException();
			
		if (roleActivationServiceListeners != null)
			roleActivationServiceListeners.remove(roleActivationServiceListener);
	}
	
	private void fireRoleActivationServiceChanged() {
		if (roleActivationServiceListeners != null) {
			for (int i = 0; i < roleActivationServiceListeners.size(); i++) {
				if (roleActivationServiceEvent == null)
					roleActivationServiceEvent = new RoleActivationServiceEvent(this);
							
				((IRoleActivationServiceListener) roleActivationServiceListeners.get(i)).roleActivationServiceChanged(roleActivationServiceEvent);
			}				
		}	
	}	
}
