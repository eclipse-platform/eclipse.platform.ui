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

package org.eclipse.ui.internal.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.roles.IActivityBinding;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleManager;
import org.eclipse.ui.roles.IRoleManagerListener;
import org.eclipse.ui.roles.RoleEvent;
import org.eclipse.ui.roles.RoleManagerEvent;

public final class RoleManager implements IRoleManager {

	private Map activityBindingsByRoleId = new HashMap();
	private Set definedRoleIds = new HashSet();
	private Map roleDefinitionsById = new HashMap();
	private List roleManagerListeners;
	private IRoleRegistry roleRegistry;
	private Map rolesById = new WeakHashMap();
	private Set rolesWithListeners = new HashSet();

	public RoleManager() {
		this(new ExtensionRoleRegistry(Platform.getExtensionRegistry()));
	}

	public RoleManager(IRoleRegistry roleRegistry) {
		if (roleRegistry == null)
			throw new NullPointerException();

		this.roleRegistry = roleRegistry;

		this.roleRegistry.addRoleRegistryListener(new IRoleRegistryListener() {
			public void roleRegistryChanged(RoleRegistryEvent roleRegistryEvent) {
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

	private void fireRoleManagerChanged(RoleManagerEvent roleManagerEvent) {
		if (roleManagerEvent == null)
			throw new NullPointerException();

		if (roleManagerListeners != null)
			for (int i = 0; i < roleManagerListeners.size(); i++)
				(
					(IRoleManagerListener) roleManagerListeners.get(
						i)).roleManagerChanged(
					roleManagerEvent);
	}

	public Set getDefinedRoleIds() {
		return Collections.unmodifiableSet(definedRoleIds);
	}

	public IRole getRole(String roleId) {
		if (roleId == null)
			throw new NullPointerException();

		Role role = (Role) rolesById.get(roleId);

		if (role == null) {
			role = new Role(this, roleId);
			updateRole(role);
			rolesById.put(roleId, role);
		}

		return role;
	}

	Set getRolesWithListeners() {
		return rolesWithListeners;
	}

	private void notifyRoles(Map roleEventsByRoleId) {
		for (Iterator iterator = roleEventsByRoleId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String roleId = (String) entry.getKey();
			RoleEvent roleEvent = (RoleEvent) entry.getValue();
			Role role = (Role) rolesById.get(roleId);

			if (role != null)
				role.fireRoleChanged(roleEvent);
		}
	}

	private void readRegistry() {
		Collection roleDefinitions = new ArrayList();
		roleDefinitions.addAll(roleRegistry.getRoleDefinitions());
		Map roleDefinitionsById =
			new HashMap(
				RoleDefinition.roleDefinitionsById(roleDefinitions, false));

		for (Iterator iterator = roleDefinitionsById.values().iterator();
			iterator.hasNext();
			) {
			IRoleDefinition roleDefinition = (IRoleDefinition) iterator.next();
			String name = roleDefinition.getName();

			if (name == null || name.length() == 0)
				iterator.remove();
		}

		Map activityBindingDefinitionsByRoleId =
			ActivityBindingDefinition.activityBindingDefinitionsByRoleId(
				roleRegistry.getActivityBindingDefinitions());
		Map activityBindingsByRoleId = new HashMap();

		for (Iterator iterator =
			activityBindingDefinitionsByRoleId.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String roleId = (String) entry.getKey();

			if (roleDefinitionsById.containsKey(roleId)) {
				Collection activityBindingDefinitions =
					(Collection) entry.getValue();

				if (activityBindingDefinitions != null)
					for (Iterator iterator2 =
						activityBindingDefinitions.iterator();
						iterator2.hasNext();
						) {
						IActivityBindingDefinition activityBindingDefinition =
							(IActivityBindingDefinition) iterator2.next();
						String activityId =
							activityBindingDefinition.getActivityId();

						if (activityId != null && activityId.length() != 0) {
							IActivityBinding activityBinding =
								new ActivityBinding(activityId);
							Set activityBindings =
								(Set) activityBindingsByRoleId.get(roleId);

							if (activityBindings == null) {
								activityBindings = new HashSet();
								activityBindingsByRoleId.put(
									roleId,
									activityBindings);
							}

							activityBindings.add(activityBinding);
						}
					}
			}
		}

		this.activityBindingsByRoleId = activityBindingsByRoleId;
		this.roleDefinitionsById = roleDefinitionsById;
		boolean roleManagerChanged = false;
		Set definedRoleIds = new HashSet(roleDefinitionsById.keySet());

		if (!definedRoleIds.equals(this.definedRoleIds)) {
			this.definedRoleIds = definedRoleIds;
			roleManagerChanged = true;
		}

		Map roleEventsByRoleId = updateRoles(rolesById.keySet());

		if (roleManagerChanged)
			fireRoleManagerChanged(new RoleManagerEvent(this, true));

		if (roleEventsByRoleId != null)
			notifyRoles(roleEventsByRoleId);
	}

	public void removeRoleManagerListener(IRoleManagerListener roleManagerListener) {
		if (roleManagerListener == null)
			throw new NullPointerException();

		if (roleManagerListeners != null)
			roleManagerListeners.remove(roleManagerListener);
	}

	private RoleEvent updateRole(Role role) {
		Set activityBindings = (Set) activityBindingsByRoleId.get(role.getId());
		boolean activityBindingsChanged =
			role.setActivityBindings(
				activityBindings != null
					? activityBindings
					: Collections.EMPTY_SET);
		IRoleDefinition roleDefinition =
			(IRoleDefinition) roleDefinitionsById.get(role.getId());
		boolean definedChanged = role.setDefined(roleDefinition != null);
		boolean descriptionChanged =
			role.setDescription(
				roleDefinition != null
					? roleDefinition.getDescription()
					: null);
		boolean nameChanged =
			role.setName(
				roleDefinition != null ? roleDefinition.getName() : null);

		if (activityBindingsChanged
			|| definedChanged
			|| descriptionChanged
			|| nameChanged)
			return new RoleEvent(
				role,
				activityBindingsChanged,
				definedChanged,
				descriptionChanged,
				nameChanged);
		else
			return null;
	}

	private Map updateRoles(Collection roleIds) {
		Map roleEventsByRoleId = new TreeMap();

		for (Iterator iterator = roleIds.iterator(); iterator.hasNext();) {
			String roleId = (String) iterator.next();
			Role role = (Role) rolesById.get(roleId);

			if (role != null) {
				RoleEvent roleEvent = updateRole(role);

				if (roleEvent != null)
					roleEventsByRoleId.put(roleId, roleEvent);
			}
		}

		return roleEventsByRoleId;
	}
}
