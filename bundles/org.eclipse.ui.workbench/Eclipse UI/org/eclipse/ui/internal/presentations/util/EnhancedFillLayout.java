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

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * @since 3.1
 */
public class EnhancedFillLayout extends Layout {

	public int xmargin = 0;
	public int ymargin = 0;
	
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        int resultX = 1;
        int resultY = 1;
        
        Control[] children = composite.getChildren();
        
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            
            Point sz = control.computeSize(wHint, hHint, flushCache);
            
            resultX = Math.max(resultX, sz.x + 2 * xmargin);
            resultY = Math.max(resultY, sz.y + 2 * ymargin);
        }
        
        return new Point(resultX, resultY);
    }

    protected void layout(Composite composite, boolean flushCache) {
        Control[] children = composite.getChildren();
        
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            Rectangle area = composite.getClientArea();
            Geometry.expand(area, -xmargin, -xmargin, -ymargin, -ymargin );
            control.setBounds(area);
        }                
    }
}
