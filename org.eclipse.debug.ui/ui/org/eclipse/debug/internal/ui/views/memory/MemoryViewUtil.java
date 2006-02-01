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
package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IMemoryBlockManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

/**
 * Util class for Memory View
 * 
 * @since 3.0
 */
public class MemoryViewUtil {
	
	public static final int[] ignoreKeyEvents =
	{
		SWT.ARROW_UP,
		SWT.ARROW_DOWN,
		SWT.ARROW_LEFT,
		SWT.ARROW_RIGHT,
		SWT.PAGE_UP,
		SWT.PAGE_DOWN,
		SWT.HOME,
		SWT.END,
		SWT.INSERT,
		SWT.F1,
		SWT.F2,
		SWT.F3,
		SWT.F4,
		SWT.F5,
		SWT.F6,
		SWT.F7,
		SWT.F8,
		SWT.F9,
		SWT.F10,
		SWT.F11,
		SWT.F12,
		SWT.F13,
		SWT.F14,
		SWT.F15,
		SWT.HELP,
		SWT.CAPS_LOCK,
		SWT.NUM_LOCK,
		SWT.SCROLL_LOCK,
		SWT.PAUSE,
		SWT.BREAK,
		SWT.PRINT_SCREEN,
		SWT.ESC,
		SWT.CTRL,
		SWT.ALT
	};	
	
	public static ArrayList MEMORY_BLOCKS_HISTORY = new ArrayList();
	
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
		
		if (debugTarget == null)
			return false;
		
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
	static public void openError (final String title, final String message, final Exception e)
	{
		UIJob uiJob = new UIJob("open error"){ //$NON-NLS-1$

			public IStatus runInUIThread(IProgressMonitor monitor) {
//				 open error for the exception
				String detail = ""; //$NON-NLS-1$
				if (e != null)
					detail = e.getMessage();
				
				Shell shell = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
				
				MessageDialog.openError(
					shell,
					title,
					message + "\n" + detail); //$NON-NLS-1$
				return Status.OK_STATUS;
			}};
		uiJob.setSystem(true);
		uiJob.schedule();
	}
	
	static IMemoryBlockManager getMemoryBlockManager()
	{
		return DebugPlugin.getDefault().getMemoryBlockManager();
	}
	
	static public boolean isLinuxGTK()
	{	
		String ws = Platform.getWS();
		return ws.equals(Platform.WS_GTK);
	}
	
	/**
	 * Checks to see if the event is valid for activating
	 * cell editing in a view tab
	 * @param event
	 * @return true if the edit event is valid  for activating the cell editor
	 */
	public static boolean isValidEditEvent(int event) {
		for (int i = 0; i < MemoryViewUtil.ignoreKeyEvents.length; i++) {
			if (event == MemoryViewUtil.ignoreKeyEvents[i])
				return false;
		}
		return true;
	}
	
	public static BigInteger alignDoubleWordBoundary(BigInteger integer)
	{
		String str =integer.toString(16);
		if (!str.endsWith("0")) //$NON-NLS-1$
		{
			str = str.substring(0, str.length() - 1);
			str += "0"; //$NON-NLS-1$
			integer = new BigInteger(str, 16);
		}		
		
		return integer;
	}
	
	public static void addHistory(String expression)
	{		
		if (!MEMORY_BLOCKS_HISTORY.contains(expression))
			MEMORY_BLOCKS_HISTORY.add(0, expression);
		
		if (MEMORY_BLOCKS_HISTORY.size() > 5)
			MEMORY_BLOCKS_HISTORY.remove(MEMORY_BLOCKS_HISTORY.size()-1);
	}
	
	public static String[] getHistory() 
	{
		return (String[])MEMORY_BLOCKS_HISTORY.toArray(new String[MEMORY_BLOCKS_HISTORY.size()]);
	}
}
