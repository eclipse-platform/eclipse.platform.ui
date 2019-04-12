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

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Interface for a pulldown action that is contributed into the workbench window
 * tool bar. It extends <code>IWorkbenchWindowActionDelegate</code> and adds an
 * initialization method to define the menu creator for the action.
 */
public interface IWorkbenchWindowPulldownDelegate extends IWorkbenchWindowActionDelegate {
	/**
	 * Returns the menu for this pull down action. This method will only be called
	 * if the user opens the pull down menu for the action. Note that it is the
	 * responsibility of the implementor to properly dispose of any SWT menus
	 * created by this method.
	 *
	 * @return the menu
	 */
	Menu getMenu(Control parent);
}
