/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;


/**
 * @since 3.1
 */
public class EditorTestHelper {

	public static final String TEXT_EDITOR_ID= "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$

	public static final String COMPILATION_UNIT_EDITOR_ID= "org.eclipse.jdt.ui.CompilationUnitEditor"; //$NON-NLS-1$

	public static final String RESOURCE_PERSPECTIVE_ID= "org.eclipse.ui.resourcePerspective"; //$NON-NLS-1$

	public static final String JAVA_PERSPECTIVE_ID= "org.eclipse.jdt.ui.JavaPerspective"; //$NON-NLS-1$

	public static final String OUTLINE_VIEW_ID= "org.eclipse.ui.views.ContentOutline"; //$NON-NLS-1$

	public static final String PACKAGE_EXPLORER_VIEW_ID= "org.eclipse.jdt.ui.PackageExplorer"; //$NON-NLS-1$

	public static final String NAVIGATOR_VIEW_ID= "org.eclipse.ui.views.ResourceNavigator"; //$NON-NLS-1$

	public static final String INTRO_VIEW_ID= "org.eclipse.ui.internal.introview"; //$NON-NLS-1$

	public static void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site= editor.getSite()) != null && (page= site.getPage()) != null)
			page.closeEditor(editor, false);
	}

	public static void closeAllEditors() {
		IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int j= 0; j < pages.length; j++) {
				IEditorReference[] editorReferences= pages[j].getEditorReferences();
				for (int k= 0; k < editorReferences.length; k++)
					closeEditor(editorReferences[k].getEditor(false));
			}
		}
	}

	/**
	 * Runs the event queue on the current display until it is empty.
	 */
	public static void runEventQueue() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null)
			runEventQueue(window.getShell());
	}

	public static void runEventQueue(IWorkbenchPart part) {
		runEventQueue(part.getSite().getShell());
	}

	public static void runEventQueue(Shell shell) {
		runEventQueue(shell.getDisplay());
	}

	public static void runEventQueue(Display display) {
		while (display.readAndDispatch()) {}
	}

	/**
	 * Runs the event queue on the current display and lets it sleep until the
	 * timeout elapses.
	 *
	 * @param millis the timeout in milliseconds
	 */
	public static void runEventQueue(long millis) {
		runEventQueue(getActiveDisplay(), millis);
	}

	public static void runEventQueue(IWorkbenchPart part, long millis) {
		runEventQueue(part.getSite().getShell(), millis);
	}

	public static void runEventQueue(Shell shell, long millis) {
		runEventQueue(shell.getDisplay(), millis);
	}

	public static void runEventQueue(Display display, long minTime) {
		if (display != null) {
			DisplayHelper.sleep(display, minTime);
		} else {
			sleep((int) minTime);
		}
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public static void forceFocus() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] wbWindows= PlatformUI.getWorkbench().getWorkbenchWindows();
			if (wbWindows.length == 0)
				return;
			window= wbWindows[0];
		}
		Shell shell= window.getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.forceActive();
			shell.forceFocus();
		}
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		return window != null ? window.getActivePage() : null;
	}

	public static Display getActiveDisplay() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		return window != null ? window.getShell().getDisplay() : null;
	}

	public static void joinBackgroundActivities() {
		// Join Building
		boolean interrupted= true;
		while (interrupted) {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				interrupted= false;
			} catch (InterruptedException e) {
				interrupted= true;
			}
		}
		// Join jobs
		joinJobs(0, 0, 500);
	}

	public static boolean joinJobs(long minTime, long maxTime, long intervalTime) {
		runEventQueue(minTime);

		DisplayHelper helper= new DisplayHelper() {
			public boolean condition() {
				return allJobsQuiet();
			}
		};
		boolean quiet= helper.waitForCondition(getActiveDisplay(), maxTime > 0 ? maxTime : Long.MAX_VALUE, intervalTime);
		return quiet;
	}

	public static void sleep(int intervalTime) {
		try {
			Thread.sleep(intervalTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static boolean allJobsQuiet() {
		IJobManager jobManager= Job.getJobManager();
		Job[] jobs= jobManager.find(null);
		for (int i= 0; i < jobs.length; i++) {
			Job job= jobs[i];
			int state= job.getState();
			if (state == Job.RUNNING || state == Job.WAITING) {
				return false;
			}
		}
		return true;
	}

	public static boolean isViewShown(String viewId) {
		return getActivePage().findViewReference(viewId) != null;
	}

	public static boolean showView(String viewId, boolean show) throws PartInitException {
		IWorkbenchPage activePage= getActivePage();
		IViewReference view= activePage.findViewReference(viewId);
		boolean shown= view != null;
		if (shown != show)
			if (show)
				activePage.showView(viewId);
			else
				activePage.hideView(view);
		return shown;
	}

	public static void bringToTop() {
		getActiveWorkbenchWindow().getShell().forceActive();
	}

	public static String showPerspective(String perspective) throws WorkbenchException {
		String shownPerspective= getActivePage().getPerspective().getId();
		if (!perspective.equals(shownPerspective)) {
			IWorkbench workbench= PlatformUI.getWorkbench();
			IWorkbenchWindow activeWindow= workbench.getActiveWorkbenchWindow();
			workbench.showPerspective(perspective, activeWindow);
		}
		return shownPerspective;
	}



	public static IFile[] findFiles(IResource resource) throws CoreException {
		List files= new ArrayList();
		findFiles(resource, files);
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}

	private static void findFiles(IResource resource, List files) throws CoreException {
		if (resource instanceof IFile) {
			files.add(resource);
			return;
		}
		if (resource instanceof IContainer) {
			IResource[] resources= ((IContainer) resource).members();
			for (int i= 0; i < resources.length; i++)
				findFiles(resources[i], files);
		}
	}

}
