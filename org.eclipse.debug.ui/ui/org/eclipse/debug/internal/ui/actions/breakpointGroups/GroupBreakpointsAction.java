/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.ui.IBreakpointContainerFactory;
import org.eclipse.jface.action.Action;

/**
 * 
 */
public class GroupBreakpointsAction extends Action {
    
    private IBreakpointContainerFactory fFactory;
    private BreakpointsView fView;

    public GroupBreakpointsAction(IBreakpointContainerFactory factory, BreakpointsView view) {
        fFactory= factory;
        fView= view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        List list= new ArrayList();
        if (fFactory != null) {
            list.add(fFactory);
        }
        fView.setBreakpointContainerFactories(list);
    }
}
