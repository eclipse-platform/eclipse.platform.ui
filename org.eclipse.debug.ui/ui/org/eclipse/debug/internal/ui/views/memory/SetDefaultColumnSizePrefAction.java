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
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;


public class SetDefaultColumnSizePrefAction extends Action {
	
	private static final String PREFIX = "SetDefaultColumnSizePrefAction."; //$NON-NLS-1$
	private static final String DEFAULT_COLUMN_SIZE = PREFIX + "DefaultColumnSize"; //$NON-NLS-1$
	private static final String SET_DEFAULT_COLUMN_SIZE = PREFIX + "SetDefaultColumnSize";  //$NON-NLS-1$
	
	public SetDefaultColumnSizePrefAction()
	{
		setText(DebugUIMessages.getString(DEFAULT_COLUMN_SIZE));
		setToolTipText(DebugUIMessages.getString(SET_DEFAULT_COLUMN_SIZE));
		WorkbenchHelp.setHelp(this, IDebugUIConstants.PLUGIN_ID + ".DefaultColumnSizePrefAction_context"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		Shell shell = DebugUIPlugin.getShell();
		DefaultColumnSizeDialog dialog = new DefaultColumnSizeDialog(shell);
		dialog.open();
	}
}
