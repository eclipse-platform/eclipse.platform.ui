package org.eclipse.ui.tutorials.rcp.part3.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tutorials.rcp.part3.Messages;

public class AboutAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;

    public AboutAction() {
    }

    public void run(IAction action) {
        MessageDialog.openInformation(window.getShell(),
            Messages.getString("AboutTitle"), //$NON-NLS-1$
            Messages.getString("AboutMessage")); //$NON-NLS-1$
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}