/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Extends <code>DefaultViewPresentation</code> with support for standalone
 * parts.  A standalone part cannot be docked together with other parts in
 * the same folder, and can optionally have its title hidden.
 */
public class DefaultStandalonePresentation extends DefaultViewPresentation {

    /**
     * Constructs a new <code>DefaultStandalonePresentation</code>.
     * 
     * @param parent the parent composite
     * @param newSite the site for interacting with the workbench
     * @param showTitle <code>true</code> iff the part's title should be shown.
     */
    public DefaultStandalonePresentation(Composite parent,
            IStackPresentationSite newSite, boolean showTitle) {
        super(parent, newSite);
        PaneFolder folder = getTabFolder();
        folder.setSingleTab(true);
        if (!showTitle) {
            folder.hideTitle();
        }
    }

}