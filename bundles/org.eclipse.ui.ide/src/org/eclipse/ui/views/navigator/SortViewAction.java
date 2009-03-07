/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;

/**
 * Implementation of the view sorting actions.
 * @since 2.0
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class SortViewAction extends ResourceNavigatorAction {
    private int sortCriteria;

    /**
     * Creates the action.
     *
     * @param navigator the resource navigator
     * @param sortByType <code>true</code> for sort by type, <code>false</code> for sort by name
     */
    public SortViewAction(IResourceNavigator navigator, boolean sortByType) {
        super(
                navigator,
                sortByType ? ResourceNavigatorMessages.SortView_byType : ResourceNavigatorMessages.SortView_byName);
        if (sortByType) {
            setToolTipText(ResourceNavigatorMessages.SortView_toolTipByType);
        } else {
            setToolTipText(ResourceNavigatorMessages.SortView_toolTipByName);
        }
        setEnabled(true);
        sortCriteria = sortByType ? ResourceComparator.TYPE : ResourceComparator.NAME;
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				INavigatorHelpContextIds.SORT_VIEW_ACTION);
    }

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
