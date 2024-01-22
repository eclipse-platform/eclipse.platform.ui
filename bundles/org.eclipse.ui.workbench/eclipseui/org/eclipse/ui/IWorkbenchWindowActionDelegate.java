/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Interface for an action that is contributed into the workbench window menu or
 * tool bar. It extends <code>IActionDelegate</code> and adds an initialization
 * method for connecting the delegate to the workbench window it should work
 * with.
 */
public interface IWorkbenchWindowActionDelegate extends IActionDelegate {
	/**
	 * Disposes this action delegate. The implementor should unhook any references
	 * to itself so that garbage collection can occur.
	 */
	void dispose();

	/**
	 * Initializes this action delegate with the workbench window it will work in.
	 *
	 * @param window the window that provides the context for this delegate
	 */
	void init(IWorkbenchWindow window);
}
