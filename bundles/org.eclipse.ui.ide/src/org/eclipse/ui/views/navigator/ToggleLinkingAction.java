/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.navigator;

/**
 * This action toggles whether this navigator links its selection to the active
 * editor.
 * 
 * @since 2.1
 */
public class ToggleLinkingAction extends ResourceNavigatorAction {

    /**
     * Constructs a new action.
     */
    public ToggleLinkingAction(IResourceNavigator navigator, String label) {
        super(navigator, label);
        setChecked(navigator.isLinkingEnabled());
    }

    /**
     * Runs the action.
     */
    public void run() {
        getNavigator().setLinkingEnabled(isChecked());
    }

}