/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.osgi.framework.BundleContext;

public class RefactoringCorePlugin extends Plugin {
	
	private static RefactoringCorePlugin fgDefault;
	
	public RefactoringCorePlugin() {
		fgDefault= this;
	}

	public static RefactoringCorePlugin getDefault() {
		return fgDefault;
	}
	
	public static String getPluginId() {
		return "org.eclipse.ltk.core.refactoring"; //$NON-NLS-1$
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(), 
			IRefactoringCoreStatusCodes.INTERNAL_ERROR, 
			RefactoringCoreMessages.getString("RefactoringCorePlugin.internal_error"),  //$NON-NLS-1$
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static void logRemovedListener(Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(), 
			IRefactoringCoreStatusCodes.INTERNAL_ERROR, 
			RefactoringCoreMessages.getString("RefactoringCorePlugin.listener_removed"),  //$NON-NLS-1$
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static void logRemovedParticipant(ParticipantDescriptor descriptor, Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(), 
			IRefactoringCoreStatusCodes.INTERNAL_ERROR, 
			RefactoringCoreMessages.getFormattedString(
				"RefactoringCorePlugin.participant_removed",  //$NON-NLS-1$
				descriptor.getId()),
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IRefactoringCoreStatusCodes.INTERNAL_ERROR, message, null));
	}
	
	/**
	 * {@inheritDoc}
	 */
	/**
	 * {@inheritDoc}
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		SaveListener.getInstance().startup();
	}
	
	/**
	 * {@inheritDoc}
	 */
	/**
	 * {@inheritDoc}
	 */
	public void stop(BundleContext context) throws Exception {
		SaveListener.getInstance().shutdown();
		super.stop(context);
	}
}
