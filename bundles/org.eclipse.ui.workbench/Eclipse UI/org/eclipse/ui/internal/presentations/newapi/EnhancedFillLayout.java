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
package org.eclipse.ui.internal.presentations.newapi;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * @since 3.1
 */
public class EnhancedFillLayout extends Layout {

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        int resultX = 1;
        int resultY = 1;
        
        Control[] children = composite.getChildren();
        
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            
            Point sz = control.computeSize(wHint, hHint, flushCache);
            
            resultX = Math.max(resultX, sz.x);
            resultY = Math.max(resultY, sz.y);
        }
        
        return new Point(resultX, resultY);
    }

    protected void layout(Composite composite, boolean flushCache) {
        Control[] children = composite.getChildren();
        
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];

            control.setBounds(composite.getClientArea());
        }                
    }
}
