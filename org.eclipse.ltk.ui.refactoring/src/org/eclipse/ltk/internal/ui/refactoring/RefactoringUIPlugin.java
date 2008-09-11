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
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.ui.refactoring.IRefactoringUIStatusCodes;

public class RefactoringUIPlugin extends AbstractUIPlugin {

	private static RefactoringUIPlugin fgDefault;

	public RefactoringUIPlugin() {
		fgDefault= this;
	}

	public static RefactoringUIPlugin getDefault() {
		return fgDefault;
	}

	public static String getPluginId() {
		return "org.eclipse.ltk.ui.refactoring"; //$NON-NLS-1$
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		RefactoringCore.internalSetQueryFactory(new UIQueryFactory(RefactoringCore.getQueryFactory()));
	}

	public void stop(BundleContext context) throws Exception {
		RefactoringCore.internalSetQueryFactory(null);
		super.stop(context);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(),
			IRefactoringUIStatusCodes.INTERNAL_ERROR,
			RefactoringUIMessages.RefactoringUIPlugin_internal_error,
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IRefactoringUIStatusCodes.INTERNAL_ERROR, message, null));
	}

	public static void logRemovedListener(Throwable t) {
		IStatus status= new Status(
			IStatus.ERROR, getPluginId(),
			IRefactoringUIStatusCodes.INTERNAL_ERROR,
			RefactoringUIMessages.RefactoringUIPlugin_listener_removed,
			t);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static IEditorPart[] getInstanciatedEditors() {
		List result= new ArrayList(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int windowIndex= 0; windowIndex < windows.length; windowIndex++) {
			IWorkbenchPage[] pages= windows[windowIndex].getPages();
			for (int pageIndex= 0; pageIndex < pages.length; pageIndex++) {
				IEditorReference[] references= pages[pageIndex].getEditorReferences();
				for (int refIndex= 0; refIndex < references.length; refIndex++) {
					IEditorPart editor= references[refIndex].getEditor(false);
					if (editor != null)
						result.add(editor);
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}

	protected ImageRegistry createImageRegistry() {
		return RefactoringPluginImages.getImageRegistry();
	}
}
