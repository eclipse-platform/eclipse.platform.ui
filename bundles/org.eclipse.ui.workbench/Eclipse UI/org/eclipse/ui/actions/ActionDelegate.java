package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * Abstract base implementation of <code>IActionDelegate</code>.
 * <p>
 * Subclasses must implement <code>run</code> to do the action's work, and may
 * reimplement <code>selectionChanged</code> to react to selection changes
 * in the workbench.
 * </p>
 */
public abstract class ActionDelegate implements IActionDelegate {
/* (non-Javadoc)
 * Method declared on IActionBar.
 */
public abstract void run(IAction action);
/**
 * The <code>ActionDelegate</code> implementation of this <code>IAction</code>
 * method does nothing. Subclasses may reimplement.
 */
public void selectionChanged(IAction action, ISelection selection) {
}
}
