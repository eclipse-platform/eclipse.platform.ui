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

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;

public class RefactoringCorePlugin extends Plugin {
	
	private static RefactoringCorePlugin fgDefault;
	
	public RefactoringCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgDefault= this;
	}

	public static RefactoringCorePlugin getDefault() {
		return fgDefault;
	}
	
	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
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
}
