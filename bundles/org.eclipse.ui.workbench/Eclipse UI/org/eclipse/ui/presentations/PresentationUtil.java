/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.presentations;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.dnd.DragUtil;

/**
 * Contains various utility methods for Presentation authors
 * 
 * @since 3.0
 */
public class PresentationUtil {
    private static Point anchor;

    private final static int HYSTERESIS = 16;
    
    private static int initialMouseButton = 0;

    private final static String LISTENER_ID = PresentationUtil.class.getName()
            + ".dragListener"; //$NON-NLS-1$

    private static Event dragEvent;

    private static Listener currentListener = null;

    private static Control dragSource;

    private static Listener dragListener = new Listener() {
        public void handleEvent(Event event) {
            dragEvent = event;
            if (dragSource != event.widget) {
                dragSource = null;
                currentListener = null;
            }
        }
    };

    /**
     * Returns whether the mouse has moved enough to warrant
     * opening a tracker.
     */
    private static boolean hasMovedEnough(Event event) {
        return Geometry.distanceSquared(DragUtil.getEventLoc(event), anchor) >= HYSTERESIS
                * HYSTERESIS;
    }

    private static Listener moveListener = new Listener() {
        public void handleEvent(Event event) {
            handleMouseMove(event);
        }
    };

    private static Listener clickListener = new Listener() {
        public void handleEvent(Event e) {
            handleMouseClick(e);
        }
    };

    private static Listener mouseDownListener = new Listener() {
        public void handleEvent(Event event) {
            if (event.widget instanceof Control) {
            	// Remember the button that started the drag so we
            	// can forward it on the call to the 'externalDragListener'
            	initialMouseButton = event.button;
            	
                dragSource = (Control) event.widget;
                currentListener = (Listener) dragSource.getData(LISTENER_ID);
                anchor = DragUtil.getEventLoc(event);

                if (dragEvent != null && (dragEvent.widget != dragSource)) {
                    dragEvent = null;
                }
            }
        }
    };

    private static void handleMouseClick(Event event) {
        cancelDrag();
    }

    private static void handleMouseMove(Event e) {
        if (currentListener != null && dragEvent != null && hasMovedEnough(e)) {
            if (dragSource != null && !dragSource.isDisposed()
                    && dragSource == e.widget) {
                Event de = dragEvent;
                
                // cache the current value so we can restore it later
                int originalMouseButton = de.button;
                
                // Update the button field so that the drag listener
                // can detect whether or not it's a 'right button' drag
                de.button = initialMouseButton;
                
                Listener l = currentListener;
                cancelDrag();
                l.handleEvent(de);
                
                // Restore the event's state so that other listeners see 
                // the original values
                de.button = originalMouseButton;
            } else {
                cancelDrag();
            }
        }
    }

    private static void cancelDrag() {
        currentListener = null;
        dragEvent = null;
        dragSource = null;

        initialMouseButton = 0;
    }

    /**
     * Adds a drag listener to the given control. The behavior is very similar
     * to control.addListener(SWT.DragDetect, dragListener), however the listener
     * attached by this method is less sensitive. The drag event is only fired
     * once the user moves the cursor more than HYSTERESIS pixels. 
     * <p>
     * This is useful for registering a listener that will trigger an editor or
     * view drag, since an overly sensitive drag listener can cause users to accidentally
     * drag views when trying to select a tab.</p>
     * <p>
     * Currently, only one such drag listener can be registered at a time. </p> 
     * 
     * @param control the control containing the drag listener
     * @param externalDragListener the drag listener to attach
     */
    public static void addDragListener(Control control,
            Listener externalDragListener) {
        control.addListener(SWT.DragDetect, dragListener);
        control.addListener(SWT.MouseUp, clickListener);
        control.addListener(SWT.MouseDoubleClick, clickListener);
        control.addListener(SWT.MouseDown, mouseDownListener);
        control.addListener(SWT.MouseMove, moveListener);
        control.setData(LISTENER_ID, externalDragListener);
    }

    /**
     * Removes a drag listener that was previously attached using addDragListener
     * 
     * @param control the control containing the drag listener
     * @param externalDragListener the drag listener to remove
     */
    public static void removeDragListener(Control control,
            Listener externalDragListener) {
        control.removeListener(SWT.DragDetect, dragListener);
        control.removeListener(SWT.MouseUp, clickListener);
        control.removeListener(SWT.MouseDoubleClick, clickListener);
        control.removeListener(SWT.MouseDown, mouseDownListener);
        control.removeListener(SWT.MouseMove, moveListener);
        control.setData(LISTENER_ID, null);
        if (externalDragListener == currentListener) {
            cancelDrag();
        }
    }

}
