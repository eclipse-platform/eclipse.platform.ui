/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This class is created to avoid mentioning preferences
 * in this context. Ideally, JFace preference classes should be
 * renamed into something more generic (for example,
 * 'TreeNavigationDialog').
 */

public class PropertyPageManager extends PreferenceManager {
	/**
	 * The constructor.
	 */
	public PropertyPageManager() {
		super(WorkbenchPlugin.PREFERENCE_PAGE_CATEGORY_SEPARATOR);
	}

	/**
	 * Given a category search he entire tree and add the node. This
	 * is to handle the case of categories beyond the first level.
	 * @param category
	 * @param node
	 * @see #addTo(String, PropertyPageNode)
	 * @return boolean <code>true</code> if it was added/
	 */
	public boolean addToDeep(String category, PropertyPageNode node) {

		return addToDeep(category, node, getRoot());
	}

	/**
	 * Given a category search the entire tree and add the node. This
	 * is to handle the case of categories beyond the first level.
	 * @param category
	 * @param node
	 * @param top the node to add to if it is found.
	 * @see #addTo(String, PropertyPageNode)
	 * @return boolean <code>true</code> if it was added somewhere
	 */
	public boolean addToDeep(String category, PropertyPageNode node, IPreferenceNode top) {

		IPreferenceNode target = find(category, top);
		if (target != null) {
			target.add(node);
			return true;
		}
		
		IPreferenceNode [] subNodes = top.getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			if(addToDeep(category, node, subNodes[i])) {
				return true;
			}			
		}
		
		return false;
	}
}
