/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
/**
 * The ProgressUtil is a class that contains static utility methods used for
 * the progress API.
 */
class ProgressManagerUtil {
	private static String PROGRESS_VIEW_ID = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$
	private static String NEW_PROGRESS_ID = "org.eclipse.ui.views.NewProgressView";//$NON-NLS-1$
	/**
	 * Return a status for the exception.
	 * 
	 * @param exception
	 * @return
	 */
	static Status exceptionStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
				exception.getMessage(), exception);
	}
	/**
	 * Log the exception for debugging.
	 * 
	 * @param exception
	 */
	static void logException(Throwable exception) {
		Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(
				exceptionStatus(exception));
	}
	/**
	 * Sets the label provider for the viewer.
	 */
	static void initLabelProvider(ProgressTreeViewer viewer) {
		viewer.setLabelProvider(new ProgressLabelProvider());
	}
	/**
	 * Return a viewer sorter for looking at the jobs.
	 * 
	 * @return
	 */
	static ViewerSorter getProgressViewerSorter() {
		return new ViewerSorter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((Comparable) e1).compareTo(e2);
			}
		};
	}
	
	/**
	 * Open the progress view in the supplied window.
	 * @param window
	 */
	static void openProgressView(WorkbenchWindow window) {
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return;
		try {
			if (useNewProgress())
				page.showView(NEW_PROGRESS_ID);
			else
				page.showView(PROGRESS_VIEW_ID);
		} catch (PartInitException exception) {
			logException(exception);
		}
	}
	
	static boolean useNewProgress() {
		return WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean("USE_NEW_PROGRESS"); //$NON-NLS-1$
	}
	
	/**
	 * Return whether or not the progress view is missing.
	 * @param window
	 * @return true if there is no progress view.
	 */
	static boolean missingProgressView(WorkbenchWindow window){
		return WorkbenchPlugin.getDefault().getViewRegistry().find(PROGRESS_VIEW_ID) == null;
		
	}
}
