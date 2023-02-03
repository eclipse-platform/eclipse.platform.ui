/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.ui.views.navigator;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;

/**
 * Implementation of the view sorting actions.
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 *
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
public class SortViewAction extends ResourceNavigatorAction {
	private int sortCriteria;

	/**
	 * Creates the action.
	 *
	 * @param navigator  the resource navigator
	 * @param sortByType <code>true</code> for sort by type, <code>false</code> for
	 *                   sort by name
	 */
	public SortViewAction(IResourceNavigator navigator, boolean sortByType) {
		super(navigator,
				sortByType ? ResourceNavigatorMessages.SortView_byType : ResourceNavigatorMessages.SortView_byName);
		if (sortByType) {
			setToolTipText(ResourceNavigatorMessages.SortView_toolTipByType);
		} else {
			setToolTipText(ResourceNavigatorMessages.SortView_toolTipByName);
		}
		setEnabled(true);
		sortCriteria = sortByType ? ResourceComparator.TYPE : ResourceComparator.NAME;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, INavigatorHelpContextIds.SORT_VIEW_ACTION);
	}

	@Override
	public void run() {
		IResourceNavigator navigator = getNavigator();
		ResourceComparator comparator = navigator.getComparator();

		if (comparator == null) {
			navigator.setComparator(new ResourceComparator(sortCriteria));
		} else {
			comparator.setCriteria(sortCriteria);
			navigator.setComparator(comparator);
		}

	}
}
