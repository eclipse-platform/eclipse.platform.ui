/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *   IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de> - Collapse all action (25826)
 ******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Collapse all project nodes.
 */
public class CollapseAllAction extends ResourceNavigatorAction {

    /**
     * Creates the action.
     * 
     * @param navigator the resource navigator
     * @param label the label for the action
     */
    public CollapseAllAction(IResourceNavigator navigator, String label) {
        super(navigator, label);
        WorkbenchHelp.setHelp(this,
                INavigatorHelpContextIds.COLLAPSE_ALL_ACTION);
        setEnabled(true);
    }

    /*
     * Implementation of method defined on <code>IAction</code>.
     */
    public void run() {
        getNavigator().getViewer().collapseAll();
    }
}