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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 3.0
 */
public class EmptyView extends ViewPart {

    /**
     * 
     */
    public EmptyView() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setContentDescription(java.lang.String)
     */
    public void setContentDescription(String description) {
        super.setContentDescription(description);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setPartName(java.lang.String)
     */
    public void setPartName(String partName) {
        super.setPartName(partName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        super.setTitle(title);
    }
}
