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

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Manages the state of the system property org.eclipse.debug.ui.sourceLookupDirectorActive
 * for use by the EditSourceLookupPathAction plugin.xml contribution. The visibility and
 * enablement of this action is controlled by the property. The action will be enabled/visible 
 * in the launch view if the locator associated with the current selection's launch 
 * is an AbstractSourceLookupDirector.
 * 
 * @since 3.0
 */

public class SourceLookupDirectorActiveManager implements ISelectionListener {
	
	private static SourceLookupDirectorActiveManager fManager;
	//the name of the property that will be set to true/false to trigger the action enablement/visibility
	private static final String fPropertyName = ".sourceLookupDirectorActive"; //$NON-NLS-1$
	
	public static void startup() {
		Runnable r = new Runnable() {
			public void run() {
				if (fManager == null) {
					fManager = new SourceLookupDirectorActiveManager();
					DebugUIPlugin.getActiveWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fManager);
				}				
			}
		};
		DebugUIPlugin.getStandardDisplay().asyncExec(r);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Object selectedObject;
		ISourceLocator locator = null;
		if (selection != null
				&& !selection.isEmpty()
				&& selection instanceof IStructuredSelection)
			selectedObject =
				((IStructuredSelection) selection).getFirstElement();
		else if (selection != null) {
			selectedObject = selection;
		} else {
			System.setProperty(DebugUIPlugin.getUniqueIdentifier() + fPropertyName, "false"); //$NON-NLS-1$
			return;
		} 
		if(selectedObject == null){
			System.setProperty(DebugUIPlugin.getUniqueIdentifier() + fPropertyName, "false"); //$NON-NLS-1$
			return;
		} 
		
		if (selectedObject instanceof ILaunch){
			if(((ILaunch) selectedObject).getLaunchConfiguration()!=null &&
					((ILaunch) selectedObject).getLaunchConfiguration().exists())
				locator = ((ILaunch) selectedObject).getSourceLocator();			
		} else if (selectedObject instanceof IDebugElement) {	
			if( ((IDebugElement) selectedObject).getLaunch().getLaunchConfiguration()!=null && 
					((IDebugElement) selectedObject).getLaunch().getLaunchConfiguration().exists())		
				locator = ((IDebugElement) selectedObject).getLaunch().getSourceLocator();		
		}
		if (locator != null && locator instanceof AbstractSourceLookupDirector){
			System.setProperty(DebugUIPlugin.getUniqueIdentifier() + fPropertyName, "true"); //$NON-NLS-1$
			return;
		} 				
		System.setProperty(DebugUIPlugin.getUniqueIdentifier() + fPropertyName, "false"); //$NON-NLS-1$
	}
	
	public static void cleanup() {
		if (fManager != null) {
			try{
				DebugUIPlugin.getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fManager);
			}catch(Exception e){}
			
			System.setProperty(DebugUIPlugin.getUniqueIdentifier() + fPropertyName, "false"); //$NON-NLS-1$
			fManager = null;
		}
	}
	
}

