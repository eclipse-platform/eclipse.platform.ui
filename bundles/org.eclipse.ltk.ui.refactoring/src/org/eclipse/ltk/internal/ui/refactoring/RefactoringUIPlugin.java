/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.ui.PlatformUI;
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

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		RefactoringCore.internalSetQueryFactory(new UIQueryFactory(RefactoringCore.getQueryFactory()));
	}

	@Override
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
		List<IEditorPart> result= new ArrayList<>(0);
		IWorkbench workbench= PlatformUI.getWorkbench();
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference reference : page.getEditorReferences()) {
					IEditorPart editor= reference.getEditor(false);
					if (editor != null)
						result.add(editor);
				}
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
	}

	@Override
	protected ImageRegistry createImageRegistry() {
		return RefactoringPluginImages.getImageRegistry();
	}
}
