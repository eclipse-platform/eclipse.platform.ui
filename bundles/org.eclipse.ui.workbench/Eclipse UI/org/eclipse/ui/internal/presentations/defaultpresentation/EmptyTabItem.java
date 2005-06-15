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
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.presentations.util.AbstractTabItem;
import org.eclipse.ui.internal.presentations.util.PartInfo;

/**
 * @since 3.1
 */
public class EmptyTabItem extends AbstractTabItem {

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabItem#getBounds()
     */
    public Rectangle getBounds() {
        return new Rectangle(0,0,0,0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabItem#setInfo(org.eclipse.ui.internal.presentations.util.PartInfo)
     */
    public void setInfo(PartInfo info) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabItem#dispose()
     */
    public void dispose() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabItem#getData()
     */
    public Object getData() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabItem#setData(java.lang.Object)
     */
    public void setData(Object data) {

    }
}
