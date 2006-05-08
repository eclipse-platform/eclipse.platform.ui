/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class FormatTableRenderingAction extends Action {

	private AbstractBaseTableRendering fRendering;
	
	int fColumnSize = -1;
	int fRowSize = -1;
	
	public FormatTableRenderingAction(AbstractBaseTableRendering rendering)
	{
		fRendering = rendering;
		setText(DebugUIMessages.FormatTableRenderingAction_16);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".FormatTableRenderingAction_context"); //$NON-NLS-1$
	}

	public void run() {
		FormatTableRenderingDialog dialog = new FormatTableRenderingDialog(fRendering, DebugUIPlugin.getShell());
		dialog.open();
		fColumnSize = dialog.getColumnSize();
		fRowSize = dialog.getRowSize();
		if (fColumnSize > 0 && fRowSize > 0)
		{
			int addressableSize = fRendering.getAddressableSize();
			int columnSizeInBytes = addressableSize * fColumnSize;
			int rowSizeInBytes = addressableSize * fRowSize;
			fRendering.format(rowSizeInBytes, columnSizeInBytes);
		}
	}
}
