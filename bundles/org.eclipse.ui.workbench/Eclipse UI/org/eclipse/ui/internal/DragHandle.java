/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class DragHandle extends Composite implements PaintListener {
    
    Cursor dragCursor;
    Image handleImage;
    ImageDescriptor descriptor;
    private boolean isHorizontal;

    private static int margin = 2;
    
public DragHandle(Composite parent) {
    super(parent, SWT.NONE);
    
    dragCursor = new Cursor(parent.getDisplay(),
            SWT.CURSOR_SIZEALL);
    
    addPaintListener(this);
    
    descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(WorkbenchPlugin.PI_WORKBENCH, "icons/misc/handle.gif");  //$NON-NLS-1$
    
    handleImage = new Image(parent.getDisplay(), 4, 4);
    GC context = new GC(handleImage);
    context.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    context.drawPoint(0,0);
    context.drawPoint(2,0);
    context.drawPoint(3,0);
    context.drawPoint(3,1);
    context.drawPoint(0,2);
    context.drawPoint(3,2);
    context.drawPoint(0,3);
    context.drawPoint(1,3);
    context.drawPoint(2,3);
    context.drawPoint(3,3);
    
    context.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
    context.drawPoint(1,0);
    context.drawPoint(0,1);
    
    context.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
    context.drawPoint(1,1);
    
    context.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
    context.drawPoint(1,2);
    context.drawPoint(2,1);
    context.drawPoint(2,2);
    
    context.dispose();
    
    setCursor(dragCursor);
}

public void paintControl(PaintEvent e) {
    Point size = getSize();
    
    if (handleImage != null) {
        Rectangle ibounds = handleImage.getBounds();
        
        
        int x = ((size.x - 2 * margin) % ibounds.width) / 2 + margin;
        int y = ((size.y - 2 * margin) % ibounds.height) / 2 + margin;

        for (;;) {
            e.gc.drawImage(handleImage, x, y);
            if (isHorizontal) {
                x += ibounds.width;
                if (x + ibounds.width > size.x - margin) {
					break;
				}
            } else {
                y += ibounds.height;
                if (y + ibounds.height > size.y - margin) {
					break;
				}
            }
        }
    }
}
public Point computeSize(int wHint, int hHint, boolean changed) {
    Point result = new Point(wHint, hHint);
    
    Rectangle ibounds = handleImage.getBounds();
    
    if (wHint == SWT.DEFAULT) {
        result.x = ibounds.width + 2 * margin;
    }
    
    if (hHint == SWT.DEFAULT) {
        result.y = ibounds.height + 2 * margin;
    }
    
    return result;
}

public void setHorizontal(boolean isHorizontal) {
    this.isHorizontal = isHorizontal;
}

public void dispose() {
    if (isDisposed()) {
        return;
    }
    super.dispose();
    dragCursor.dispose();
    handleImage.dispose();
    JFaceResources.getResources().destroyImage(descriptor);
}
}
