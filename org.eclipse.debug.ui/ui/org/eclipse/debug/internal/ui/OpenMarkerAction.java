package org.eclipse.debug.internal.ui;


/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Open a marker 
 */
public class OpenMarkerAction extends SelectionProviderAction {

	public OpenMarkerAction(ISelectionProvider selectionProvider, String label) {
		super(selectionProvider, label);
		setEnabled(getStructuredSelection().size() == 1);
	}

	/**
	 * @see IAction
	 */
	public void run() {
		IStructuredSelection selection= (IStructuredSelection) getStructuredSelection();
		//Get the selected marker
		if (selection.size() != 1) {
			return; //Single selection only
		}
		Object object= selection.getFirstElement();
		IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		IWorkbenchPage page= dwindow.getActivePage();
		try {
			page.openEditor((IMarker)object);
		} catch (PartInitException e) {
			DebugUIUtils.logError(e);
		}
	}

	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		if (sel.size() == 1) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}
