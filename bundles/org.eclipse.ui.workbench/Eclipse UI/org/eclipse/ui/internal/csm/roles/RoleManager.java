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

package org.eclipse.ui.internal.csm.roles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleManager;
import org.eclipse.ui.roles.IRoleManagerEvent;
import org.eclipse.ui.roles.IRoleManagerListener;

public final class RoleManager implements IRoleManager {

	private Map activityBindingDefinitionsByRoleId = new HashMap();
	private Map activityBindingsByRoleId = new HashMap();	
	private Set definedRoleIds = new HashSet();
	private ExtensionRoleRegistry extensionRoleRegistry;
	private Map roleDefinitionsById = new HashMap();
	private IRoleManagerEvent roleManagerEvent;
	private List roleManagerListeners;	
	private Map rolesById = new HashMap();	

	public RoleManager() {
		if (extensionRoleRegistry == null)
			extensionRoleRegistry = new ExtensionRoleRegistry(Platform.getExtensionRegistry());
			
		loadExtensionRoleRegistry();		

		extensionRoleRegistry.addRoleRegistryListener(new IRoleRegistryListener() {
			public void roleRegistryChanged(IRoleRegistryEvent roleRegistryEvent) {
				readRegistry();
			}
		});

		readRegistry();
	}

	public void addRoleManagerListener(IRoleManagerListener roleManagerListener) {
		if (roleManagerListener == null)
			throw new NullPointerException();
			
		if (roleManagerListeners == null)
			roleManagerListeners = new ArrayList();
		
		if (!roleManagerListeners.contains(roleManagerListener))
			roleManagerListeners.add(roleManagerListener);
	}

	public IRole getRole(String roleId) {
		if (roleId == null)
			throw new NullPointerException();
			
		Role role = (Role) rolesById.get(roleId);
		
		if (role == null) {
			role = new Role(roleId);
			updateRole(role);
			rolesById.put(roleId, role);
		}
		
		return role;
	}
	
	public Set getDefinedRoleIds() {
		return Collections.unmodifiableSet(definedRoleIds);
	}

	public void removeRoleManagerListener(IRoleManagerListener roleManagerListener) {
		if (roleManagerListener == null)
			throw new NullPointerException();
			
		if (roleManagerListeners != null)
			roleManagerListeners.remove(roleManagerListener);
	}

	private void fireRoleManagerChanged() {
		if (roleManagerListeners != null)
			for (int i = 0; i < roleManagerListeners.size(); i++) {
				if (roleManagerEvent == null)
					roleManagerEvent = new RoleManagerEvent(this);
								
				((IRoleManagerListener) roleManagerListeners.get(i)).roleManagerChanged(roleManagerEvent);
			}				
	}

	private void loadExtensionRoleRegistry() {
		try {
			extensionRoleRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}
	}
	
	private void notifyRoles(Collection roleIds) {	
		for (Iterator iterator = roleIds.iterator(); iterator.hasNext();) {	
			String roleId = (String) iterator.next();					
			Role role = (Role) rolesById.get(roleId);
			
			if (role != null)
				role.fireRoleChanged();
		}
	}

	private void readRegistry() {
		Collection roleDefinitions = new ArrayList();
		roleDefinitions.addAll(extensionRoleRegistry.getRoleDefinitions());				
		Map roleDefinitionsById = new HashMap(RoleDefinition.roleDefinitionsById(roleDefinitions, false));

		for (Iterator iterator = roleDefinitionsById.values().iterator(); iterator.hasNext();) {
			IRoleDefinition roleDefinition = (IRoleDefinition) iterator.next();
			String name = roleDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		Collection activityBindingDefinitions = new ArrayList();
		activityBindingDefinitions.addAll(extensionRoleRegistry.getActivityBindingDefinitions());
		Map activityBindingDefinitionsByRoleId = new HashMap(ActivityBindingDefinition.activityBindingDefinitionsByRoleId(activityBindingDefinitions, false));

		for (Iterator iterator = activityBindingDefinitionsByRoleId.values().iterator(); iterator.hasNext();) {
			IActivityBindingDefinition activityBindingDefinition = (IActivityBindingDefinition) iterator.next();
			String roleId = activityBindingDefinition.getRoleId();
				
			if (roleId == null || roleId.length() == 0)
				iterator.remove();
		}

		for (Iterator iterator = activityBindingDefinitionsByRoleId.keySet().iterator(); iterator.hasNext();)
			if (!roleDefinitionsById.containsKey(iterator.next()))
				iterator.remove();		

		this.roleDefinitionsById = roleDefinitionsById;
		this.activityBindingDefinitionsByRoleId = activityBindingsByRoleId;			
		boolean roleManagerChanged = false;			
		Set definedRoleIds = new TreeSet(roleDefinitionsById.keySet());		

		if (!definedRoleIds.equals(this.definedRoleIds)) {
			this.definedRoleIds = definedRoleIds;
			roleManagerChanged = true;	
		}

		Collection updatedRoleIds = updateRoles(rolesById.keySet());	
		
		if (roleManagerChanged)
			fireRoleManagerChanged();

		if (updatedRoleIds != null)
			notifyRoles(updatedRoleIds);		
	}

	private boolean updateRole(Role role) {
		boolean updated = false;
		IRoleDefinition roleDefinition = (IRoleDefinition) roleDefinitionsById.get(role.getId());
		updated |= role.setDefined(roleDefinition != null);
		updated |= role.setDescription(roleDefinition != null ? roleDefinition.getDescription() : null);		
		updated |= role.setName(roleDefinition != null ? roleDefinition.getName() : null);
		Set activityBindings = (Set) activityBindingsByRoleId.get(role.getId());
		updated |= role.setActivityBindings(activityBindings != null ? activityBindings : Collections.EMPTY_SET);
		return updated;
	}

	private Collection updateRoles(Collection roleIds) {
		Collection updatedIds = new TreeSet();
		
		for (Iterator iterator = roleIds.iterator(); iterator.hasNext();) {		
			String roleId = (String) iterator.next();					
			Role role = (Role) rolesById.get(roleId);
			
			if (role != null && updateRole(role))
				updatedIds.add(roleId);			
		}
		
		return updatedIds;			
	}
}
