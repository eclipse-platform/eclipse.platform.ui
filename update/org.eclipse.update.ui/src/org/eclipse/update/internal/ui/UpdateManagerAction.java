package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.internal.ui.*;
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
			UpdateUIPlugin.getActiveWorkbenchWindow();
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
			//IAdaptable input = UpdateUIPlugin.getWorkspace();
			IWorkbenchPage updatePage =
				window.getWorkbench().showPerspective(
					UpdatePerspective.PERSPECTIVE_ID,
					window);
			ensureCriticalViewsVisible(updatePage);
		} catch (WorkbenchException e) {
			UpdateUIPlugin.logException(e, true);
		}
	}

	private void ensureCriticalViewsVisible(IWorkbenchPage page) {
		ensureViewVisible(page, UpdatePerspective.ID_CONFIGURATION);
		ensureViewVisible(page, UpdatePerspective.ID_UPDATES);
		ensureViewVisible(page, UpdatePerspective.ID_DETAILS);
	}

	private void ensureViewVisible(IWorkbenchPage page, String viewId) {
		try {
			if (page.findView(viewId) == null) {
				page.showView(viewId);
			}
		} catch (PartInitException e) {
			UpdateUIPlugin.logException(e, true);
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
