/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

/**
 * Set column size of current view tab as the default column size
 * 
 * @since 3.0
 */
public class SetColumnSizeDefaultAction extends Action {
    private AbstractTableRendering fRendering;

    public SetColumnSizeDefaultAction(AbstractTableRendering rendering) {
        super(DebugUIMessages.SetColumnSizeDefaultAction_Set_as_default);
        fRendering = rendering;
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".SetColumnSizeDefaultAction_context"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        int numUnits = fRendering.getAddressableUnitPerColumn();

        IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
        prefStore.setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, numUnits);
    }

}
