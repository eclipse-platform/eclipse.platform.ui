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
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.presentations.newapi.AbstractTabItem;
import org.eclipse.ui.internal.presentations.newapi.PartInfo;

/**
 * @since 3.1
 */
public class EmptyTabItem extends AbstractTabItem {

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#getBounds()
     */
    public Rectangle getBounds() {
        return new Rectangle(0,0,0,0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#setInfo(org.eclipse.ui.internal.presentations.newapi.PartInfo)
     */
    public void setInfo(PartInfo info) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#dispose()
     */
    public void dispose() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#getData()
     */
    public Object getData() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabItem#setData(java.lang.Object)
     */
    public void setData(Object data) {

    }
}
