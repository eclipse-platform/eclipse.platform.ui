/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.preference.PreferenceContentProvider;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Adds activity filtering support to <code>PreferenceContentProvider</code>.
 * 
 * @since 3.0
 */
public class FilteredPreferenceContentProvider extends
        PreferenceContentProvider {

    /**
     * Create a new instance of the <code>FilteringPreferenceContentProvider</code>.
     * 
     */
    public FilteredPreferenceContentProvider() {
        //no-op
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        Object[] children = super.getChildren(parentElement);
        ArrayList filteredChildren = new ArrayList(children.length);
        for (int i = 0; i < children.length; i++) {
            if (WorkbenchActivityHelper.filterItem(children[i]))
                continue;

            filteredChildren.add(children[i]);
        }
        return filteredChildren.toArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        Object parent = super.getParent(element);
        if (WorkbenchActivityHelper.filterItem(parent))
            return null;
        return parent;
    }
}