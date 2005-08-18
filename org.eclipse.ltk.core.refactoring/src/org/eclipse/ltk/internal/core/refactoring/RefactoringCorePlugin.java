/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.IUndoManager;

import org.osgi.framework.BundleContext;

public class RefactoringCorePlugin extends Plugin {
	
	private static RefactoringCorePlugin fgDefault;
	private static IUndoManager fgUndoManager= null;
	
	private static IUndoContext fRefactoringUndoContext;
	
	public RefactoringCorePlugin() {
		fgDefault= this;
	}

	public static RefactoringCorePlugin getDefault() {
		return fgDefault;
	}
	
	public static String getPluginId() {
		return "org.eclipse.ltk.core.refactoring"; //$NON-NLS-1$
	}
	
	public static IUndoContext getUndoContext() {
		if (fRefactoringUndoContext == null) {
			fRefactoringUndoContext= new RefactoringUndoContext();
			IUndoContext workspaceContext= (IUndoContext)ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
			if (workspaceContext instanceof ObjectUndoContext) {
				((ObjectUndoContext)workspaceContext).addMatch(fRefactoringUndoContext);
			}
			IOperationHistory operationHistory= OperationHistoryFactory.getOperationHistory();
			operationHistory.setLimit(fRefactoringUndoContext, 5);
		}
		return fRefactoringUndoContext;
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(), 
			IRefactoringCoreStatusCodes.INTERNAL_ERROR, 
			RefactoringCoreMessages.RefactoringCorePlugin_internal_error,  
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static void logRemovedListener(Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(), 
			IRefactoringCoreStatusCodes.INTERNAL_ERROR, 
			RefactoringCoreMessages.RefactoringCorePlugin_listener_removed,  
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static void logRemovedParticipant(ParticipantDescriptor descriptor, Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(), 
			IRefactoringCoreStatusCodes.INTERNAL_ERROR, 
			Messages.format(
				RefactoringCoreMessages.RefactoringCorePlugin_participant_removed,  
				descriptor.getId()),
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IRefactoringCoreStatusCodes.INTERNAL_ERROR, message, null));
	}
	
	public static IUndoManager getUndoManager() {
		if (fgUndoManager == null)
			fgUndoManager= createUndoManager();
		return fgUndoManager;
	}
	
	public void stop(BundleContext context) throws Exception {
		if (fRefactoringUndoContext != null) {
			IUndoContext workspaceContext= (IUndoContext)ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
			if (workspaceContext instanceof ObjectUndoContext) {
				((ObjectUndoContext)workspaceContext).removeMatch(fRefactoringUndoContext);
			}
		}
		if (fgUndoManager != null)
			fgUndoManager.shutdown();
		super.stop(context);
	}
	
	/**
	 * Creates a new empty undo manager.
	 * 
	 * @return a new undo manager
	 */
	private static IUndoManager createUndoManager() {
		return new UndoManager2();
	}
}