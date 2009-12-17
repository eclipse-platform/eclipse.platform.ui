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

import org.eclipse.jface.action.IAction;

/**
 * Interface for an object action that is contributed into a popup menu
 * for a view or editor.  It extends <code>IActionDelegate</code>
 * and adds an initialization method for connecting the delegate to the 
 * part it should work with.
 */
public interface IObjectActionDelegate extends IActionDelegate {
    /**
	 * Sets the active part for the delegate. The active part is commonly used
	 * to get a working context for the action, such as the shell for any dialog
	 * which is needed.
	 * <p>
	 * This method will be called every time the action appears in a popup menu.
	 * The targetPart may change with each invocation.
	 * </p>
	 * 
	 * @param action
	 *            the action proxy that handles presentation portion of the
	 *            action; must not be <code>null</code>.
	 * @param targetPart
	 *            the new part target; must not be <code>null</code>.
	 */
    public void setActivePart(IAction action, IWorkbenchPart targetPart);
}
