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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.roles.api.IRoleDefinition;
import org.eclipse.ui.internal.roles.api.IRoleRegistry;
import org.eclipse.ui.internal.roles.api.IRoleRegistryEvent;
import org.eclipse.ui.internal.roles.api.IRoleRegistryListener;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleManager;
import org.eclipse.ui.roles.IRoleManagerEvent;
import org.eclipse.ui.roles.IRoleManagerListener;

public final class RoleManager implements IRoleManager {

	private static RoleManager instance;

	public static RoleManager getInstance() {
		if (instance == null)
			instance = new RoleManager();
			
		return instance;
	}

	public static boolean isRoleDefinitionChildOf(String ancestor, String id, Map roleDefinitionsById) {
		Set visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			IRoleDefinition roleDefinition = (IRoleDefinition) roleDefinitionsById.get(id);				
			visited.add(id);

			if (roleDefinition != null && Util.equals(id = roleDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}	

	private List activeRoleIds = new ArrayList();
	private IRoleManagerEvent roleManagerEvent;
	private List roleManagerListeners;
	private SortedMap roleDefinitionsById = new TreeMap();
	private SortedMap rolesById = new TreeMap();
	private SortedSet definedRoleIds = new TreeSet();
	private PluginRoleRegistry pluginRoleRegistry;
	private PreferenceRoleRegistry preferenceRoleRegistry;

	private RoleManager() {
		if (pluginRoleRegistry == null)
			pluginRoleRegistry = new PluginRoleRegistry(Platform.getPluginRegistry());
			
		loadPluginRoleRegistry();		

		pluginRoleRegistry.addRoleRegistryListener(new IRoleRegistryListener() {
			public void roleRegistryChanged(IRoleRegistryEvent roleRegistryEvent) {
				readRegistry();
			}
		});

		if (preferenceRoleRegistry == null)
			preferenceRoleRegistry = new PreferenceRoleRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());	

		loadPreferenceRoleRegistry();

		preferenceRoleRegistry.addRoleRegistryListener(new IRoleRegistryListener() {
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

	public List getActiveRoleIds() {
		return Collections.unmodifiableList(activeRoleIds);
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
	
	public SortedSet getDefinedRoleIds() {
		return Collections.unmodifiableSortedSet(definedRoleIds);
	}

	public void removeRoleManagerListener(IRoleManagerListener roleManagerListener) {
		if (roleManagerListener == null)
			throw new NullPointerException();
			
		if (roleManagerListeners != null)
			roleManagerListeners.remove(roleManagerListener);
	}

	public void setActiveRoleIds(List activeRoleIds) {
		activeRoleIds = Util.safeCopy(activeRoleIds, String.class);
		boolean roleManagerChanged = false;
		SortedSet updatedRoleIds = null;

		if (!this.activeRoleIds.equals(activeRoleIds)) {
			this.activeRoleIds = activeRoleIds;
			roleManagerChanged = true;	
			updatedRoleIds = updateRoles(this.definedRoleIds);	
		}
		
		if (roleManagerChanged)
			fireRoleManagerChanged();

		if (updatedRoleIds != null)
			notifyRoles(updatedRoleIds);	
	}

	// TODO private
	public IRoleRegistry getPluginRoleRegistry() {
		return pluginRoleRegistry;
	}

	// TODO private
	public IRoleRegistry getPreferenceRoleRegistry() {
		return preferenceRoleRegistry;
	}

	private void loadPluginRoleRegistry() {
		try {
			pluginRoleRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}
	}
	
	private void loadPreferenceRoleRegistry() {
		try {
			preferenceRoleRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}		
	}

	private void fireRoleManagerChanged() {
		if (roleManagerListeners != null) {
			for (int i = 0; i < roleManagerListeners.size(); i++) {
				if (roleManagerEvent == null)
					roleManagerEvent = new RoleManagerEvent(this);
								
				((IRoleManagerListener) roleManagerListeners.get(i)).roleManagerChanged(roleManagerEvent);
			}				
		}			
	}

	private void notifyRoles(Collection roleIds) {	
		Iterator iterator = roleIds.iterator();
		
		while (iterator.hasNext()) {
			String roleId = (String) iterator.next();					
			Role role = (Role) rolesById.get(roleId);
			
			if (role != null)
				role.fireRoleChanged();
		}
	}

	private void readRegistry() {
		List roleDefinitions = new ArrayList();
		roleDefinitions.addAll(pluginRoleRegistry.getRoleDefinitions());
		roleDefinitions.addAll(preferenceRoleRegistry.getRoleDefinitions());
		SortedMap roleDefinitionsById = new TreeMap(RoleDefinition.roleDefinitionsById(roleDefinitions, false));

		for (Iterator iterator = roleDefinitionsById.values().iterator(); iterator.hasNext();) {
			IRoleDefinition roleDefinition = (IRoleDefinition) iterator.next();
			String name = roleDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		for (Iterator iterator = roleDefinitionsById.keySet().iterator(); iterator.hasNext();)
			if (!isRoleDefinitionChildOf(null, (String) iterator.next(), roleDefinitionsById))
				iterator.remove();

		SortedSet definedRoleIds = new TreeSet(roleDefinitionsById.keySet());		
		boolean roleManagerChanged = false;

		if (!this.definedRoleIds.equals(definedRoleIds)) {
			this.definedRoleIds = definedRoleIds;
			roleManagerChanged = true;	
		}

		this.roleDefinitionsById = roleDefinitionsById;
		SortedSet updatedRoleIds = updateRoles(this.definedRoleIds);	
		
		if (roleManagerChanged)
			fireRoleManagerChanged();

		if (updatedRoleIds != null)
			notifyRoles(updatedRoleIds);		
	}

	private boolean updateRole(Role role) {
		boolean updated = false;
		updated |= role.setActive(activeRoleIds.contains(role.getId()));
		IRoleDefinition roleDefinition = (IRoleDefinition) roleDefinitionsById.get(role.getId());
		updated |= role.setDefined(roleDefinition != null);
		updated |= role.setDescription(roleDefinition != null ? roleDefinition.getDescription() : null);
		updated |= role.setName(roleDefinition != null ? roleDefinition.getName() : null);
		updated |= role.setParentId(roleDefinition != null ? roleDefinition.getParentId() : null);
		return updated;
	}

	private SortedSet updateRoles(Collection roleIds) {
		SortedSet updatedIds = new TreeSet();
		Iterator iterator = roleIds.iterator();
		
		while (iterator.hasNext()) {
			String roleId = (String) iterator.next();					
			Role role = (Role) rolesById.get(roleId);
			
			if (role != null && updateRole(role))
				updatedIds.add(roleId);			
		}
		
		return updatedIds;			
	}
}