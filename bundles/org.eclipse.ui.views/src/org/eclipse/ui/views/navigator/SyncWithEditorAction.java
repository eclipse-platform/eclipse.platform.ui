package org.eclipse.ui.views.navigator;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * @author lynne
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SyncWithEditorAction extends ResourceNavigatorAction implements IPartListener {

/**
 * Creates the action.
 * 
 * @param navigator the resource navigator
 * @param label the label for the action
 */
public SyncWithEditorAction (IResourceNavigator navigator, String label) {
	super(navigator, label);
	WorkbenchHelp.setHelp(this, INavigatorHelpContextIds.SYNC_WITH_EDITOR_ACTION);
	setEnabled(false);
}
public void partActivated(IWorkbenchPart part) {
}
public void partBroughtToTop(IWorkbenchPart part) {
}
public void partClosed(IWorkbenchPart part) {
	// only enable this action if an editor is opened
	int editorCnt = getWorkbenchWindow().getActivePage().getEditorReferences().length;
	setEnabled(editorCnt > 0);
}
public void partDeactivated(IWorkbenchPart part) {		
}
public void partOpened(IWorkbenchPart part) {
	// only enable this action if an editor is opened
	int editorCnt = getWorkbenchWindow().getActivePage().getEditorReferences().length;
	setEnabled(editorCnt > 0);
}
/*
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {
		IWorkbenchPage page= getWorkbenchWindow().getActivePage();	
		IEditorPart activeEditor = page.getActiveEditor();
		if (activeEditor != null) {
			try {
				IViewPart view= page.showView(IPageLayout.ID_RES_NAV);
				ISelection selection= new StructuredSelection(activeEditor.getEditorInput());
				((ISetSelectionTarget)view).selectReveal(selection);
			} catch(PartInitException e) {
				ErrorDialog.openError(
					page.getWorkbenchWindow().getShell(),
					ResourceNavigatorMessages.getString("SyncWithEditor.errorMessage"), //$NON-NLS-1$
					e.getMessage(),
					e.getStatus());
					}
		}
}

}
