/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Control helper for a button that switches enablement of the Role system.
 * 
 * @since 3.0
 */
public class RoleSystemEnablementHelper {

    private Button roleEnablement;

    /**
     * Create the Button control.
     * 
     * @param parent
     * @return
     * @since 3.0
     */
    public Control createControl(Composite parent) {
        roleEnablement = new Button(parent, SWT.CHECK);
        roleEnablement.setText(RoleMessages.getString("RolePreferencePage.RoleBasedFilteringLabel")); //$NON-NLS-1$
        roleEnablement.setSelection(RoleManager.getInstance().isFiltering());
        return roleEnablement;
    }

    /**
     * Update the Role system enablement based on the selection state of the 
     * Button.
     * 
     * @since 3.0
     */
    public void updateRoleState() {
        RoleManager.getInstance().setFiltering(roleEnablement.getSelection());
    }

    /**
     * @return Button
     * @since 3.0
     */
    public Control getControl() {
        return roleEnablement;
    }
}
