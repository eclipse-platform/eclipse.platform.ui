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
package org.eclipse.debug.ui.actions;

import org.eclipse.debug.core.model.IVariable;
import org.eclipse.swt.widgets.Shell;

/**
 * A variable value editor allows the user to edit a variable's value.
 * Variable value editors are contributed for a debug model via the
 * org.eclipse.debug.ui.variableValueEditors extension point.
 * 
 * @since 3.1
 */
public interface IVariableValueEditor {

    /**
     * Edits the given variable, if appropriate. If this editor does not apply to
     * the given variable this method returns false, which indicates that the
     * Debug Platform's default variable edit dialog should be used.
     * 
     * @param variable the variable to edit
     * @param shell the currently active shell, which can be used to open a dialog
     *  for the user
     * @return whether this editor has completed the edit operation for the given variable.
     *  <code>true</code> if no more work should be done, <code>false</code> if the debug
     *  platform should prompt the user to edit the given variable using the default
     *  variable editor.
     */
    public boolean editVariable(IVariable variable, Shell shell);
}
