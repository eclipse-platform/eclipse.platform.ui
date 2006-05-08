/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IViewLayout;

/**
 * Implementation of IViewLayout.
 * This is an API facade on the internal ViewLayoutRec.
 *  
 * @since 3.0
 */
public class ViewLayout implements IViewLayout {
    private ViewLayoutRec rec;

    public ViewLayout(PageLayout pageLayout, ViewLayoutRec rec) {
        Assert.isNotNull(pageLayout);
        Assert.isNotNull(rec);
        this.rec = rec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewLayout#getShowTitle()
     */
    public boolean getShowTitle() {
        return rec.showTitle;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewLayout#isCloseable()
     */
    public boolean isCloseable() {
        return rec.isCloseable;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewLayout#isMoveable()
     */
    public boolean isMoveable() {
        return rec.isMoveable;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewLayout#isStandalone()
     */
    public boolean isStandalone() {
        return rec.isStandalone;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewLayout#setCloseable(boolean)
     */
    public void setCloseable(boolean closeable) {
        rec.isCloseable = closeable;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewLayout#setMoveable(boolean)
     */
    public void setMoveable(boolean moveable) {
        rec.isMoveable = moveable;
    }

}
