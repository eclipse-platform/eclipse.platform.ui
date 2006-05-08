/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * Resize all columns
 * 
 * @since 3.0
 */
public class ReformatAction extends Action {
	
	private AbstractBaseTableRendering fRendering;
	
	public ReformatAction(AbstractBaseTableRendering rendering)
	{
		super(DebugUIMessages.ReformatAction_title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".ReformatAction_context"); //$NON-NLS-1$
		fRendering = rendering;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fRendering.resizeColumnsToPreferredSize();
	}

}
