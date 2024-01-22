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
package org.eclipse.ui.part;

import org.eclipse.ui.IEditorPart;

/**
 * Abstract base class for managing the installation/deinstallation of global
 * actions for multi-page editors.
 * <p>
 * Subclasses must implement <code>setActivePage</code>, and may reimplement any
 * of the following methods:
 * </p>
 * <ul>
 * <li><code>contributeToMenu</code> - reimplement to contribute to menu</li>
 * <li><code>contributeToToolBar</code> - reimplement to contribute to tool
 * bar</li>
 * <li><code>contributeToStatusLine</code> - reimplement to contribute to status
 * line</li>
 * </ul>
 */
public abstract class MultiPageEditorActionBarContributor extends EditorActionBarContributor {
	/**
	 * Creates a multi-page editor action contributor.
	 */
	protected MultiPageEditorActionBarContributor() {
		super();
	}

	/*
	 * Registers the contributor with the multi-page editor for future editor action
	 * redirection when the active page is changed, and sets the active page.
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {
		IEditorPart activeNestedEditor = null;
		if (part instanceof MultiPageEditorPart) {
			activeNestedEditor = ((MultiPageEditorPart) part).getActiveEditor();
		}
		setActivePage(activeNestedEditor);
	}

	/**
	 * Sets the active page of the the multi-page editor to be the given editor.
	 * Redirect actions to the given editor if actions are not already being sent to
	 * it.
	 * <p>
	 * This method is called whenever the page changes. Subclasses must implement
	 * this method to redirect actions to the given editor (if not already directed
	 * to it).
	 * </p>
	 *
	 * @param activeEditor the new active editor, or <code>null</code> if there is
	 *                     no active page, or if the active page does not have a
	 *                     corresponding editor
	 */
	public abstract void setActivePage(IEditorPart activeEditor);
}
