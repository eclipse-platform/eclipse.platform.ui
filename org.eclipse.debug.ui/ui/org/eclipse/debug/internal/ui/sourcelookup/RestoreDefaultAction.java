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
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The action for adding the default container to the list.
 * 
 * @since 3.0
 */
public class RestoreDefaultAction extends SourceContainerAction {
	
	public RestoreDefaultAction() {
		super(SourceLookupUIMessages.getString("sourceTab.defaultButton")); //$NON-NLS-1$
	}
	/**
	 * @see IAction#run()
	 */
	public void run() {		
		ISourceContainer[] containers = new ISourceContainer[1];
		containers[0] = new DefaultSourceContainer();
		getViewer().addEntries(containers);
	}
	
	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		//disable if selection is empty, default already present, or non-root node selected
		ISourceContainer[] containers = getViewer().getEntries();
		if(containers != null){
			for(int i=0; i< containers.length; i++)
			{
				if(containers[i] instanceof DefaultSourceContainer)
					return false;
			}
		}		
		return selection.isEmpty()|| getViewer().getTree().getSelection()[0].getParentItem()==null;	
	}
	
}
