/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.interfaces.IFocusable;

/**
 * @since 3.1
 */
public class SiteComposite extends Composite {
    private IFocusable focusHandler = null;
    
    public SiteComposite(Composite parent) {
        super(parent, SWT.NONE);
    }
    
    public void setFocusable(IFocusable f) {
        this.focusHandler = f;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Composite#setFocus()
     */
    public boolean setFocus() {
        if (focusHandler != null && focusHandler.setFocus()) {
            return true;
        }
        
        return super.setFocus();
    }
}
