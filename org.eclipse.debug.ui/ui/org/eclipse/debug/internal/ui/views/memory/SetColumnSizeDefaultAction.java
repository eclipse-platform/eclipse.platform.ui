/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Set column size of current view tab as the default column size
 * 
 * @since 3.0
 */
public class SetColumnSizeDefaultAction extends Action
{
	private static final String PREFIX = "SetColumnSizeDefaultAction."; //$NON-NLS-1$
	private static final String LABEL = PREFIX + "Set_as_default"; //$NON-NLS-1$
	
	private ITableMemoryViewTab fViewTab;
	
	public SetColumnSizeDefaultAction(ITableMemoryViewTab viewTab)
	{
		super (DebugUIMessages.getString(LABEL));
		fViewTab = viewTab;
		WorkbenchHelp.setHelp(this, IDebugUIConstants.PLUGIN_ID + ".SetColumnSizeDefaultAction_context"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		int numUnits = fViewTab.getAddressibleUnitPerColumn();
		
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		prefStore.setValue(IDebugPreferenceConstants.PREF_COLUMN_SIZE, numUnits);
	}

}
