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

package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;

/**
 * Toggle step filters action delegate.
 * 
 * @since 3.3
 */
public class ToggleStepFiltersCommandActionDelegate extends DebugCommandActionDelegate {

    /** 
     * Constructor
     */
    public ToggleStepFiltersCommandActionDelegate() {
        super();
        setAction(new ToggleStepFiltersAction());
    }

    public void init(IAction action) {
        super.init(action);
        action.setChecked(DebugUITools.isUseStepFilters());
    }
}
