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
package org.eclipse.ui.internal.presentations.util;

import org.eclipse.swt.graphics.Rectangle;

/**
 * @since 3.1
 */
public abstract class AbstractTabItem {
    public abstract Rectangle getBounds();
    public abstract void setInfo(PartInfo info);
    public abstract void dispose();
    public void setBusy(boolean busy) {}
    public void setBold(boolean bold) {}
    
    public abstract Object getData();
    public abstract void setData(Object data);
    
    public boolean isShowing() {
        return true;
    }
    
}
