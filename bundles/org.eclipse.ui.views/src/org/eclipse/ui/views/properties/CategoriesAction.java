/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import org.eclipse.ui.PlatformUI;

/**
 * This action hides or shows categories in the <code>PropertySheetViewer</code>.
 */
/*package*/class CategoriesAction extends PropertySheetAction {
    /**
     * Creates the Categories action. This action is used to show
     * or hide categories properties.
     * @param viewer the viewer
     * @param name the name
     */
    public CategoriesAction(PropertySheetViewer viewer, String name) {
        super(viewer, name);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(this, IPropertiesHelpContextIds.CATEGORIES_ACTION);
    }

    /**
     * Toggles the display of categories for the properties.
     */
    public void run() {
        PropertySheetViewer ps = getPropertySheet();
        ps.deactivateCellEditor();
        if (isChecked()) {
            ps.showCategories();
        } else {
            ps.hideCategories();
        }
    }
}
