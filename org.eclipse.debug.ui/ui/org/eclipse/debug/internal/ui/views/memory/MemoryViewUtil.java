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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IMemoryBlockManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

/**
 * Util class for Memory View
 * 
 * @since 3.0
 */
public class MemoryViewUtil {
	/**
	 * @param selection
	 * @return true if the given selection is valid for creating a memory block
	 */
	static public boolean isValidSelection(ISelection selection) {
	
		if (!(selection instanceof IStructuredSelection))
			return false;
	
		//only single selection is allowed for this action
		if (selection == null || selection.isEmpty() || ((IStructuredSelection)selection).size() > 1)
		{
			return false;
		}
	
		Object elem = ((IStructuredSelection)selection).getFirstElement();
	
		// if not debug element
		if (!(elem instanceof IDebugElement))
			return false;
	
		IDebugTarget debugTarget = ((IDebugElement)elem).getDebugTarget();
		IMemoryBlockRetrieval memRetrieval =(IMemoryBlockRetrieval) ((IDebugElement)elem).getAdapter(IMemoryBlockRetrieval.class);
		
		if (memRetrieval == null)
		{
			// if debug element returns null from getAdapter, assume its debug target is going to retrieve memory blocks
			memRetrieval = debugTarget;
		}
		
		// not valid if the debug target is already terminated
		if (debugTarget.isTerminated() || debugTarget.isDisconnected())
			return false;
		
		if (memRetrieval.supportsStorageRetrieval()) {
			return true;
		}
		
		return false;
	}	

	
	/**
	 * Helper function to open an error dialog.
	 * @param title
	 * @param message
	 * @param e
	 */
	static public void openError (String title, String message, Exception e)
	{
		// open error for the exception
		String detail = ""; //$NON-NLS-1$
		if (e != null)
			detail = e.getMessage();
		
		Shell shell = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		
		MessageDialog.openError(
			shell,
			title,
			message + "\n" + detail); //$NON-NLS-1$
	}
	
	static IMemoryBlockManager getMemoryBlockManager()
	{
		return DebugPlugin.getDefault().getMemoryBlockManager();
	}
}
