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

package org.eclipse.ui.views.navigator;

import org.eclipse.ui.IWorkbenchCommandConstants;

/**
 * This action toggles whether this navigator links its selection to the active
 * editor.
 *
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 *
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
public class ToggleLinkingAction extends ResourceNavigatorAction {

	/**
	 * Constructs a new action.
	 *
	 * @param navigator the resource navigator
	 * @param label     the label
	 */
	public ToggleLinkingAction(IResourceNavigator navigator, String label) {
		super(navigator, label);
		setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);
		setChecked(navigator.isLinkingEnabled());
	}

	/**
	 * Runs the action.
	 */
	@Override
	public void run() {
		getNavigator().setLinkingEnabled(isChecked());
	}

}
