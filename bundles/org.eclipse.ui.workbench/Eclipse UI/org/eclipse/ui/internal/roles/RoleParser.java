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
import java.util.List;

import org.eclipse.ui.IMemento;

/**
 * The RoleParser is a class that reads the role memento and 
 * builds a list of roles from it.
 */
final class RoleParser {

	final static String TRUE_STRING = "true"; //$NON-NLS-1$
	final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$
	final static String TAG_PATTERNS = "patterns"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$
	final static String TAG_VALUE = "value"; //$NON-NLS-1$	
	final static String TAG_ROLE = "role"; //$NON-NLS-1$	
	final static String TAG_ENABLED = "enabled"; //$NON-NLS-1$

	/**
	 * Read an individual role definition from memento.
	 * @param memento
	 * @return
	 */
	private static Role readRoleDefinition(IMemento memento) {
		if (memento == null)
			throw new IllegalArgumentException();

		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		boolean enabled = TRUE_STRING.equals(memento.getString(TAG_ENABLED));

		ArrayList patterns = new ArrayList();

		IMemento patternsMemento = memento.getChild(TAG_PATTERNS);

		IMemento[] children = patternsMemento.getChildren(TAG_PATTERN);
		for (int i = 0; i < children.length; i++) {
			patterns.add(children[i].getString(TAG_VALUE));
		}

		String[] patternArray = new String[patterns.size()];
		patterns.toArray(patternArray);

		Role newRole = new Role(name, id, patternArray);
		newRole.setEnabled(enabled);
		return newRole;
	}

	/**
	 * Read and return all fo the role definitions in memento.
	 * @param memento
	 * @return Role[]
	 */
	static Role[] readRoleDefinitions(IMemento memento) {
		if (memento == null)
			throw new IllegalArgumentException();

		IMemento[] mementos = memento.getChildren(TAG_ROLE);

		if (mementos == null)
			throw new IllegalArgumentException();

		List list = new ArrayList(mementos.length);

		for (int i = 0; i < mementos.length; i++) {
			list.add(readRoleDefinition(mementos[i]));
		}

		Role[] roles = new Role[list.size()];
		list.toArray(roles);
		return roles;
	}

}
