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
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action for showing or hiding address column in memory view tab
 */
public class ShowAddressColumnAction extends Action {
	
	private MemoryViewTab fViewTab;
	
	public ShowAddressColumnAction(MemoryViewTab viewTab)
	{
		super();
		WorkbenchHelp.setHelp(this, IDebugUIConstants.PLUGIN_ID + ".ShowAddressColumnAction_context"); //$NON-NLS-1$
		fViewTab = viewTab;
		updateActionLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fViewTab.showAddressColumn(!fViewTab.isShowAddressColumn());
		updateActionLabel();
	}

	/**
	 * 
	 */
	private void updateActionLabel() {
		if (fViewTab.isShowAddressColumn())
		{
			setText(DebugUIMessages.getString("ShowAddressColumnAction.0")); //$NON-NLS-1$
		}
		else 
		{
			setText(DebugUIMessages.getString("ShowAddressColumnAction.1")); //$NON-NLS-1$
		}
	}
}
