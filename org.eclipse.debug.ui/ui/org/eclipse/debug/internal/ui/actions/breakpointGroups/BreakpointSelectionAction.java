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
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * A selection listener action for the breakpoints view.
 */
public abstract class BreakpointSelectionAction extends SelectionListenerAction {
    
    private BreakpointsView fView;
    
    /**
     * Constructs an action for the breakpoints view.
     * 
     * @param text action name
     * @param view breakpoints view
     */
    public BreakpointSelectionAction(String text, BreakpointsView view) {
        super(text);
        fView = view;
    }
    
    /**
     * Returns the breakpoints view.
     * 
     * @return breakpoints view
     */
    protected BreakpointsView getBreakpointsView() {
        return fView;
    }

}
