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
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.layout.SizeCache;

/**
 * This invisible control forces some target control to have the
 * same size and position.
 * 
 * @since 3.0
 */
public class ProxyControl {
    private Composite control;

    private SizeCache target;

    public ProxyControl(Composite parent) {
        control = new Composite(parent, SWT.NONE);
        control.setVisible(false);

        control.setLayout(new Layout() {
            protected void layout(Composite composite, boolean flushCache) {
                //ProxyControl.this.layout();				
            }

            protected Point computeSize(Composite composite, int wHint,
                    int hHint, boolean flushCache) {
                if (target == null) {
                    return new Point(0, 0);
                }

                return target.computeSize(wHint, hHint);
            }
        });
    }

    /**
     * Sets the control whose position will be managed by this proxy
     * 
     * @param target the control, or null if none
     */
    public void setTarget(SizeCache target) {
        if (this.target != target) {
            this.target = target;
        }
    }

    /**
     * Returns the target control (the control whose size is being managed)
     * 
     * @return the target control (or null)
     */
    public Control getTargetControl() {
        if (target == null) {
            return null;
        }

        return target.getControl();
    }

    /**
     * Returns the proxy control
     * 
     * @return the proxy control (not null)
     */
    public Control getControl() {
        return control;
    }

    /**
     * Layout the target control
     */
    public void layout() {
        if (getTargetControl() == null) {
            return;
        }

        Rectangle parentBounds = DragUtil.getDisplayBounds(control.getParent());

        // Compute the clipped bounds for the control (display coordinates)
        Rectangle bounds = control.getBounds();
        bounds.x += parentBounds.x;
        bounds.y += parentBounds.y;
        bounds = bounds.intersection(parentBounds);

        Rectangle targetBounds = Geometry.toControl(getTargetControl()
                .getParent(), bounds);

        getTargetControl().setBounds(targetBounds);
    }

    /**
     * Destroys this object. Should be the last method called on this object. No further methods should
     * be invoked after calling this.
     */
    public void dispose() {
        if (control == null) {
            return;
        }
        this.target = null;
        control.dispose();
        control = null;
    }
}