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

package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * Manages the installation and deinstallation of global actions for
 * the welcome editor.
 */
public class WelcomeEditorActionContributor extends EditorActionBarContributor {
	/**
	 * The <code>WelcomeEditorActionContributor</code> implementation of this
	 * <code>IEditorActionBarContributor</code> method installs the global
	 * action handler for the given editor.
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {
		IActionBars actionBars = getActionBars();
		if (actionBars != null) {
			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					((WelcomeEditor) part).getCopyAction());
		}
	}
}
