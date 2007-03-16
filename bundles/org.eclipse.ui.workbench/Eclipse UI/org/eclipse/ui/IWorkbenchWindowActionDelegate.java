/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

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
