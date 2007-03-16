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
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.internal.util.Util;

/**
 * @since 3.0
 */
public class OverriddenTitleView extends EmptyView {

    String overriddenTitle = "OverriddenTitle";

    /**
     * 
     */
    public OverriddenTitleView() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitle()
     */
    public String getTitle() {
        return overriddenTitle;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setTitle(java.lang.String)
     */
    public void customSetTitle(String title) {
        overriddenTitle = Util.safeString(title);

        firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
    }

}
