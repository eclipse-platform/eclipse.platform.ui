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
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ltk.ui.refactoring.IRefactoringUIStatusCodes;

public class RefactoringUIPlugin extends AbstractUIPlugin {
	
	private static RefactoringUIPlugin fgDefault;
	
	public RefactoringUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgDefault= this;
	}

	public static RefactoringUIPlugin getDefault() {
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
			IRefactoringUIStatusCodes.INTERNAL_ERROR, 
			RefactoringUIMessages.getString("RefactoringUIPlugin.internal_error"),  //$NON-NLS-1$
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	
	public static void logRemovedListener(Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(), 
			IRefactoringUIStatusCodes.INTERNAL_ERROR, 
			RefactoringUIMessages.getString("RefactoringUIPlugin.listener_removed"),  //$NON-NLS-1$
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
