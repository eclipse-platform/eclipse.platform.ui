/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
        sortCriteria = sortByType ? ResourceSorter.TYPE : ResourceSorter.NAME;
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				INavigatorHelpContextIds.SORT_VIEW_ACTION);
    }

    public void run() {
        IResourceNavigator navigator = getNavigator();
        ResourceSorter sorter = navigator.getSorter();

        if (sorter == null)
            navigator.setSorter(new ResourceSorter(sortCriteria));
        else {
            sorter.setCriteria(sortCriteria);
            navigator.setSorter(sorter);
        }

    }
}
