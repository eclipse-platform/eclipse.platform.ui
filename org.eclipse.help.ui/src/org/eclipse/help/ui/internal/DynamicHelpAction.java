package org.eclipse.help.ui.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class DynamicHelpAction implements IWorkbenchWindowActionDelegate {

    public DynamicHelpAction() {
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }

    public void run(IAction action) {
        PlatformUI.getWorkbench().getHelpSystem().displayDynamicHelp();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }
}
