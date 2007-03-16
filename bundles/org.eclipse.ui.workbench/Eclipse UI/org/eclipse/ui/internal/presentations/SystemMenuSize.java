/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.PartPane;

public class SystemMenuSize extends ContributionItem {

    private PartPane partPane;

    public SystemMenuSize(PartPane pane) {
        setPane(pane);
    }

    public void setPane(PartPane pane) {
        partPane = pane;
    }

    public void dispose() {
        partPane = null;
    }

    public void fill(Menu menu, int index) {
        if (partPane != null) {
            partPane.addSizeMenuItem(menu, index);
        }
    }

    public boolean isDynamic() {
        return true;
    }
}
