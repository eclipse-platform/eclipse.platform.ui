package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
/**
 * The <code>SystemSummaryActionDelegate</code> opens
 * a <code>SystemSummaryEditor</code> to display information
 * about the Eclipse instance in which this action is invoked.
 */
public class SystemSummaryActionDelegate implements IWorkbenchWindowActionDelegate {
	/*
	 * The parent window of this action delegate.
	 */
	private IWorkbenchWindow fWindow;
	
	/**
	 * Opens a <code>SystemSummaryEditor</code>.
	 * 
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IWorkbenchPage page= fWindow.getActivePage();
		try {
			page.openEditor(new SystemSummaryEditorInput(), SystemSummaryEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow = null;
	}
}

