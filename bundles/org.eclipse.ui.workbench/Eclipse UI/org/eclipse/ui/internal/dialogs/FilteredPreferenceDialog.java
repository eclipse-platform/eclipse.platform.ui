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

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Baseclass for preference dialogs that will show two tabs of preferences - 
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog {

    /**
     * Creates a new preference dialog under the control of the given preference 
     * manager.
     *
     * @param shell the parent shell
     * @param manager the preference manager
     */
    public FilteredPreferenceDialog(Shell parentShell, PreferenceManager manager) {
        super(parentShell, manager);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
     */
    protected TreeViewer createTreeViewer(Composite parent) {
        TreeViewer tree = super.createTreeViewer(parent);
        tree.setLabelProvider(new PreferenceLabelProvider());
        tree.setContentProvider(new FilteredPreferenceContentProvider());
        return tree;
    }

    /**
     * Differs from super implementation in that if the node is found but should
     * be filtered based on a call to 
     * <code>WorkbenchActivityHelper.filterItem()</code> then <code>null</code> 
     * is returned.
     * 
     * @see org.eclipse.jface.preference.PreferenceDialog#findNodeMatching(java.lang.String)
     */
    protected IPreferenceNode findNodeMatching(String nodeId) {
        IPreferenceNode node = super.findNodeMatching(nodeId);
        if (WorkbenchActivityHelper.filterItem(node))
            return null;
        return node;
    }
}