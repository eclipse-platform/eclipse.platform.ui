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

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Context menu action
 * 
 * @since 3.0
 */
public class ResetMemoryBlockContextAction extends ResetMemoryBlockAction
{
	private IMemoryViewTab fViewTab;
	
	public ResetMemoryBlockContextAction(IMemoryViewTab viewTab)
	{
		super();
		fViewTab = viewTab;
		WorkbenchHelp.setHelp(this, IDebugUIConstants.PLUGIN_ID + ".ResetBaseAddressContextAction_context"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.internal.actions.AbstractMemoryAction#getViewTab()
	 */
	IMemoryViewTab getViewTab()
	{
		return fViewTab;
	}

}
