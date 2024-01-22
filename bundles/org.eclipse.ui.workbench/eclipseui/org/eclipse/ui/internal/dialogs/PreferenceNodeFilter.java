/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * The PreferenceNodeFilter is a filter that only matches a set of ids.
 */
public class PreferenceNodeFilter extends ViewerFilter {

	Collection ids = new HashSet();

	/**
	 * Create a new instance of the receiver on a list of filteredIds.
	 *
	 * @param filteredIds The collection of ids that will be shown.
	 */
	public PreferenceNodeFilter(String[] filteredIds) {
		super();
		ids.addAll(Arrays.asList(filteredIds));
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return checkNodeAndChildren((IPreferenceNode) element);
	}

	/**
	 * Check to see if the node or any of its children have an id in the ids.
	 *
	 * @param node WorkbenchPreferenceNode
	 * @return boolean <code>true</code> if node or oe of its children has an id in
	 *         the ids.
	 */
	private boolean checkNodeAndChildren(IPreferenceNode node) {
		if (ids.contains(node.getId())) {
			return true;
		}

		for (IPreferenceNode subNode : node.getSubNodes()) {
			if (checkNodeAndChildren(subNode)) {
				return true;
			}

		}
		return false;
	}

}
