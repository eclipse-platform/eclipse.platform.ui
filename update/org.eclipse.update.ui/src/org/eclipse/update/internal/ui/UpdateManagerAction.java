/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;

/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class UpdateManagerAction implements IWorkbenchWindowActionDelegate {
	/**
	 * The constructor.
	 */
	public UpdateManagerAction() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IWorkbenchWindow currentWindow =
			UpdateUI.getActiveWorkbenchWindow();
		IWorkbenchPage currentPage = currentWindow.getActivePage();

		if (currentPage != null
			&& currentPage.getPerspective().getId().equals(
				UpdatePerspective.PERSPECTIVE_ID)) {
			ensureCriticalViewsVisible(currentPage);
			return;
		}
		IWorkbenchWindow window =
			currentPage != null
				? currentPage.getWorkbenchWindow()
				: currentWindow;

		try {
			IWorkbenchPage updatePage =
				window.getWorkbench().showPerspective(
					UpdatePerspective.PERSPECTIVE_ID,
					window);
			ensureCriticalViewsVisible(updatePage);
		} catch (WorkbenchException e) {
			UpdateUI.logException(e, true);
		}
	}

	private void ensureCriticalViewsVisible(IWorkbenchPage page) {
		ensureViewVisible(page, UpdatePerspective.ID_ITEMS, false);
		ensureViewVisible(page, UpdatePerspective.ID_UPDATES, false);
		ensureViewVisible(page, UpdatePerspective.ID_DETAILS, true);
		ensureViewVisible(page, UpdatePerspective.ID_CONFIGURATION, true);
	}

	private void ensureViewVisible(IWorkbenchPage page, String viewId, boolean top) {
		try {
			IViewPart view = page.findView(viewId);
			if (view == null) {
				page.showView(viewId);
			}
			else if (top) {
				page.bringToTop(view);
			}
		} catch (PartInitException e) {
			UpdateUI.logException(e, true);
		}
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction arg0, ISelection arg1) {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow arg0) {
	}
}
