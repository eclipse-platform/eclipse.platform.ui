/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.team.examples.pessimistic.ui;
 
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.examples.pessimistic.PessimisticFilesystemProvider;
import org.eclipse.team.examples.pessimistic.PessimisticFilesystemProviderPlugin;

public class DisconnectAction extends PessimisticProviderAction {
	/**
	 * Collects the selected resources, extracts the projects selected
	 * and disconnects the projects from their respective providers.
	 * 
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (PessimisticFilesystemProviderPlugin.getInstance().isDebugging())
			System.out.println("Disconnect");
		
		IResource[] resources= getSelectedResources();
		if (resources == null || resources.length == 0)
			return;
		final Set projects= new HashSet(resources.length);
		for(int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (resource.getType() == IResource.PROJECT) {
				projects.add(resource.getProject());
			}
		}
		if (!projects.isEmpty()) {
			IRunnableWithProgress runnable= new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
					IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor)
							throws CoreException {
							for (Iterator i= projects.iterator(); i.hasNext();) {
								IProject project= (IProject) i.next();
								PessimisticFilesystemProvider provider= getProvider(project);
								if (provider != null) {
									try {
										RepositoryProvider.unmap(project);	
									} catch (TeamException e) {
										PessimisticFilesystemProviderPlugin.getInstance().logError(e, "Could not unmap " + project);
									}						
								}
							}
						}				
					};
					try {
						ResourcesPlugin.getWorkspace().run(runnable, monitor);
					} catch (CoreException e) {
						PessimisticFilesystemProviderPlugin.getInstance().logError(e, "Problem during unmap runnable");	
					}
					
				}
			};
			runWithProgressDialog(runnable);
		}
	}
	
	/**
	 * Answers <code>true</code> if and only if the resource is a 
	 * project and is controlled by the pessimistic filesystem provider.
	 * 
	 * @see PessimisticProviderAction#shouldEnableFor(IResource)
	 */
	protected boolean shouldEnableFor(IResource resource) {
		if (resource.getType() == IResource.PROJECT) {
			PessimisticFilesystemProvider provider= getProvider(resource);
			if (provider == null)
				return false;
			return true;
		}
		return false;
	}
}
