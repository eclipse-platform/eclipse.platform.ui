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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

	final static String PACKAGE_BASE = "roles"; //$NON-NLS-1$
	final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$	
	final static String PACKAGE_FULL = PACKAGE_PREFIX + '.' + PACKAGE_BASE;
	final static String TAG_ACTIVITY_ID = "activityId"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$
	final static String TAG_ROLE = "role"; //$NON-NLS-1$	
	final static String TAG_ROLE_ID = "roleId"; //$NON-NLS-1$	
	final static String TAG_ROLE_ACTIVITY_BINDING = "roleActivityBinding"; //$NON-NLS-1$	

	static IRoleActivityBindingDefinition readRoleActivityBindingDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String activityId = memento.getString(TAG_ACTIVITY_ID);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		String roleId = memento.getString(TAG_ROLE_ID);
		return new RoleActivityBindingDefinition(activityId, pluginId, roleId);
	}

	static List readRoleActivityBindingDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readRoleActivityBindingDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}	
	
	static IRoleDefinition readRoleDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new RoleDefinition(description, id, name, pluginId);
	}

	static List readRoleDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readRoleDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}	

	static void writeRoleActivityBindingDefinition(IMemento memento, IRoleActivityBindingDefinition roleActivityBindingDefinition) {
		if (memento == null || roleActivityBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_ACTIVITY_ID, roleActivityBindingDefinition.getActivityId());
		memento.putString(TAG_PLUGIN_ID, roleActivityBindingDefinition.getPluginId());
		memento.putString(TAG_ROLE_ID, roleActivityBindingDefinition.getRoleId());
	}

	static void writeRoleActivityBindingDefinitions(IMemento memento, String name, List roleActivityBindingDefinitions) {
		if (memento == null || name == null || roleActivityBindingDefinitions == null)
			throw new NullPointerException();
		
		roleActivityBindingDefinitions = new ArrayList(roleActivityBindingDefinitions);
		Iterator iterator = roleActivityBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IRoleActivityBindingDefinition.class);

		iterator = roleActivityBindingDefinitions.iterator();

		while (iterator.hasNext()) 
			writeRoleActivityBindingDefinition(memento.createChild(name), (IRoleActivityBindingDefinition) iterator.next());
	}	
	
	static void writeRoleDefinition(IMemento memento, IRoleDefinition roleDefinition) {
		if (memento == null || roleDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, roleDefinition.getDescription());
		memento.putString(TAG_ID, roleDefinition.getId());
		memento.putString(TAG_NAME, roleDefinition.getName());
		memento.putString(TAG_PLUGIN_ID, roleDefinition.getPluginId());
	}

	static void writeRoleDefinitions(IMemento memento, String name, List roleDefinitions) {
		if (memento == null || name == null || roleDefinitions == null)
			throw new NullPointerException();
		
		roleDefinitions = new ArrayList(roleDefinitions);
		Iterator iterator = roleDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IRoleDefinition.class);

		iterator = roleDefinitions.iterator();

		while (iterator.hasNext()) 
			writeRoleDefinition(memento.createChild(name), (IRoleDefinition) iterator.next());
	}

	private Persistence() {
	}	
}
