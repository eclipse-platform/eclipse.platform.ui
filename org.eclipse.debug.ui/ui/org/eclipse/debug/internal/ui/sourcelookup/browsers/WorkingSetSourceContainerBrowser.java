/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup.browsers;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser;
import org.eclipse.debug.internal.ui.sourcelookup.containers.WorkingSetSourceContainer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The browser for creating working set source containers.
 * 
 * @since 3.0
 */
public class WorkingSetSourceContainerBrowser implements ISourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public ISourceContainer[] createSourceContainers(Shell shell, ILaunchConfiguration configuration) {
		ArrayList containers = new ArrayList();		
		IWorkingSetSelectionDialog dialog = WorkbenchPlugin.getDefault().getWorkingSetManager().createWorkingSetSelectionDialog(shell,true);
		
		if(dialog.open() == Window.OK)
		{
			IWorkingSet[] selections = dialog.getSelection();
			if(selections != null)
			{				
				for(int i=0; i<selections.length; i++)
				{
					containers.add(new WorkingSetSourceContainer(selections[i]));					
				}	
			}		
		}
		
		return (ISourceContainer[])containers.toArray(new ISourceContainer[containers.size()]);
	}
	
}
