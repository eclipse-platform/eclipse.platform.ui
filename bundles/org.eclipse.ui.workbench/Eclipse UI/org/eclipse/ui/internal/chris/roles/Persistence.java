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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.roles.api.IRoleDefinition;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

	final static String PACKAGE_BASE = "roles"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$
	final static String TAG_ROLE = "role"; //$NON-NLS-1$	

	static IRoleDefinition readRoleDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		// TODO deprecated start
		if ("org.eclipse.ui.globalScope".equals(id)) //$NON-NLS-1$
			id = null;	
		// TODO deprecated end

		String name = memento.getString(TAG_NAME);
		String parentId = memento.getString(TAG_PARENT_ID);
		
		// TODO deprecated start
		if ("org.eclipse.ui.globalScope".equals(parentId)) //$NON-NLS-1$
			parentId = null;	
		// TODO deprecated end
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new RoleDefinition(description, id, name, parentId, pluginId);
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

	static void writeRoleDefinition(IMemento memento, IRoleDefinition roleDefinition) {
		if (memento == null || roleDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, roleDefinition.getDescription());
		memento.putString(TAG_ID, roleDefinition.getId());
		memento.putString(TAG_NAME, roleDefinition.getName());
		memento.putString(TAG_PARENT_ID, roleDefinition.getParentId());
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
