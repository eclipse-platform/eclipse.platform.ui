/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class ToggleLinkingAction extends ResourceNavigatorAction {


	/**
	 * Constructs a new action.
	 * 
	 * @param navigator the resource navigator
	 * @param label the label
	 */
    public ToggleLinkingAction(IResourceNavigator navigator, String label) {
        super(navigator, label);
		setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);
        setChecked(navigator.isLinkingEnabled());
    }

    /**
     * Runs the action.
     */
    public void run() {
        getNavigator().setLinkingEnabled(isChecked());
    }

}
