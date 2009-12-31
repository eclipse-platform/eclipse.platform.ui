/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.registry.UIExtensionTracker;

/**
 * @since 3.5
 *
 */
public class WorkbenchWindow implements IWorkbenchWindow {

	private final MWindow model;
	private WorkbenchPage page;
	private UIExtensionTracker tracker;

	// /**
	// * @param perspectiveId
	// * @param input
	// */
	// public WorkbenchWindow(String perspectiveId, IAdaptable input) {
	//
	// }

	/**
	 * @param activeWindow
	 */
	public WorkbenchWindow(MWindow model) {
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#close()
	 */
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getActivePage()
	 */
	public IWorkbenchPage getActivePage() {
		if (page == null) {
			page = new WorkbenchPage(this, model, null);
		}
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getPages()
	 */
	public IWorkbenchPage[] getPages() {
		return new IWorkbenchPage[] { getActivePage() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getPartService()
	 */
	public IPartService getPartService() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getSelectionService()
	 */
	public ISelectionService getSelectionService() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getShell()
	 */
	public Shell getShell() {
		return ((Control) model.getWidget()).getShell();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getWorkbench()
	 */
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#isApplicationMenu(java.lang.String)
	 */
	public boolean isApplicationMenu(String menuId) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#openPage(java.lang.String, org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage openPage(String perspectiveId, IAdaptable input)
			throws WorkbenchException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#openPage(org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#setActivePage(org.eclipse.ui.IWorkbenchPage)
	 */
	public void setActivePage(IWorkbenchPage page) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getWorkbench().getDisplay());
		}
		return tracker;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#addPageListener(org.eclipse.ui.IPageListener)
	 */
	public void addPageListener(IPageListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#addPerspectiveListener(org.eclipse.ui.IPerspectiveListener)
	 */
	public void addPerspectiveListener(IPerspectiveListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#removePageListener(org.eclipse.ui.IPageListener)
	 */
	public void removePageListener(IPageListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#removePerspectiveListener(org.eclipse.ui.IPerspectiveListener)
	 */
	public void removePerspectiveListener(IPerspectiveListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
	 */
	public Object getService(Class api) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
	 */
	public boolean hasService(Class api) {
		// TODO Auto-generated method stub
		return false;
	}

}
