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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.internal.presentations.util.PartInfo;
import org.eclipse.ui.internal.presentations.util.WidgetTabItem;
import org.eclipse.ui.internal.util.Util;

/**
 * @since 3.1
 */
public class NativeTabItem extends WidgetTabItem {
    
    public NativeTabItem(NativeTabFolder parent, int index) {
        super(new TabItem(parent.getTabFolder(), SWT.NONE, index));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabItem#getBounds()
     */
    public Rectangle getBounds() {
        return new Rectangle(0,0,0,0);
    }

    public void setInfo(PartInfo info) {
        TabItem widget = (TabItem) getWidget();
        
        if (!Util.equals(widget.getText(), info.name)) {
            widget.setText(info.name);
        }
        String oldToolTip = Util.safeString(widget.getToolTipText());
        
        if (!Util.equals(info.toolTip, oldToolTip)) {
            String toolTip = info.toolTip;
            if (toolTip.length() == 0) {
                toolTip = null;
            }
            widget.setToolTipText(toolTip);
        }
    }    
}
