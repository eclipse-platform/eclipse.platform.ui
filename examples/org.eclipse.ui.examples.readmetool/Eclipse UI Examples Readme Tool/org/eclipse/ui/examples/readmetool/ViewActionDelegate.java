package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This class is used to demonstrate view action extensions.
 * An extension should be defined in the readme plugin.xml.
 */
public class ViewActionDelegate implements IViewActionDelegate {
	public IViewPart view;
/**
 * Creates a new ViewActionDelegate.
 */
public ViewActionDelegate() {
	super();
}
/* (non-Javadoc)
 * Method declared on IViewActionDelegate
 */
public void init(IViewPart view) {
	this.view = view;
}
/* (non-Javadoc)
 * Method declared on IActionDelegate
 */
public void run(org.eclipse.jface.action.IAction action) {
	MessageDialog.openInformation(view.getSite().getShell(),
		MessageUtil.getString("Readme_Editor"),  //$NON-NLS-1$
		MessageUtil.getString("View_Action_executed")); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IActionDelegate
 */
public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {}
}
