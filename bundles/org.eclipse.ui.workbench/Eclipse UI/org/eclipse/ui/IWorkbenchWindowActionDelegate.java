package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Interface for an action that is contributed into the workbench window menu 
 * or tool bar. It extends <code>IActionDelegate</code> and adds an
 * initialization method for connecting the delegate to the workbench window it
 * should work with.
 */
public interface IWorkbenchWindowActionDelegate extends IActionDelegate {
/**
 * Disposes this action delegate.  The implementor should unhook any references
 * to itself so that garbage collection can occur.
 */
public void dispose();
/**
 * Initializes this action delegate with the workbench window it will work in.
 *
 * @param window the window that provides the context for this delegate
 */
public void init(IWorkbenchWindow window);
}
