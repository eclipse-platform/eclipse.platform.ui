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

import org.eclipse.jface.action.IAction;

/**
 * Interface for an action that is contributed into an editor-activated menu or
 * tool bar. It extends <code>IActionDelegate</code> and adds a method for
 * connecting the delegate to the editor it should work with. Since there is
 * always only one action delegate per editor type, this method supplies the
 * link to the currently active editor instance.
 */
public interface IEditorActionDelegate extends IActionDelegate {
	/**
	 * Sets the active editor for the delegate. Implementors should disconnect from
	 * the old editor, connect to the new editor, and update the action to reflect
	 * the new editor.
	 *
	 * @param action       the action proxy that handles presentation portion of the
	 *                     action
	 * @param targetEditor the new editor target
	 */
	void setActiveEditor(IAction action, IEditorPart targetEditor);
}
