package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IAction;

/**
 * Interface for an object action that is contributed into a popup menu
 * for a view or editor.  It extends <code>IActionDelegate</code>
 * and adds an initialization method for connecting the delegate to the 
 * part it should work with.
 */
public interface IObjectActionDelegate extends IActionDelegate {
/**
 * Sets the active part for the delegate.  
 * The active part is commonly used to get a working context for the action, such
 * as the shell for any dialog which is needed.
 * <p>
 * This method will be called every time the action appears in a popup menu.  The
 * targetPart may change with each invocation.
 * </p>
 *
 * @param action the action proxy that handles presentation portion of the action
 * @param targetPart the new part target
 */
public void setActivePart(IAction action, IWorkbenchPart targetPart);
}
