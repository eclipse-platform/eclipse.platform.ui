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
package org.eclipse.ui.internal.dnd;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * Contains static methods for manipulating SWT controls
 * 
 * @since 3.0
 */
public class SwtUtil {

    private SwtUtil() {

    }

    /**
     * Returns true if the given control is null or has been disposed
     * 
     * @param toTest the control to test
     * @return false if it is safe to invoke methods on the given control
     */
    public static boolean isDisposed(Control toTest) {
        return toTest == null || toTest.isDisposed();
    }

    /**
     * Returns the control that is covering the given control, or null if none.
     * 
     * @param toTest control to test
     * @return a control that obscures the test control or null if none
     */
    public static Control controlThatCovers(Control toTest) {
        return controlThatCovers(toTest, DragUtil.getDisplayBounds(toTest));
    }
    
    private static Control controlThatCovers(Control toTest, Rectangle testRegion) {
        Composite parent = toTest.getParent();
        
        if (parent == null || toTest instanceof Shell) {
            return null;
        }
       
        Control[] children = parent.getChildren();
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            
            if (control == toTest) {
                break;
            }
            
            if (!control.isVisible()) {
                continue;
            }
            
            Rectangle nextBounds = DragUtil.getDisplayBounds(control);
            
            if (nextBounds.intersects(testRegion)) {
                return control;
            }
        }
        
        return controlThatCovers(parent, testRegion);
    }
    
    /**
     * Determines if one control is a child of another. Returns true iff the second
     * argument is a child of the first (or the same object).
     * 
     * @param potentialParent
     * @param childToTest
     * @return
     */
    public static boolean isChild(Control potentialParent, Control childToTest) {
        if (childToTest == null) {
            return false;
        }

        if (childToTest == potentialParent) {
            return true;
        }

        return isChild(potentialParent, childToTest.getParent());
    }
    
    public static boolean isFocusAncestor(Control potentialParent) {
        Assert.isNotNull(potentialParent);
        Control focusControl = Display.getCurrent().getFocusControl();
        if (focusControl == null) {
            return false;
        }
        return isChild(potentialParent, focusControl);
    }

    /**
     * Finds and returns the most specific SWT control at the given location. 
     * (Note: this does a DFS on the SWT widget hierarchy, which is slow).
     * 
     * @param displayToSearch
     * @param locationToFind
     * @return
     */
    public static Control findControl(Display displayToSearch,
            Point locationToFind) {
        Shell[] shells = displayToSearch.getShells();

        return findControl(shells, locationToFind);
    }

    /**
     * Searches the given list of controls for a control containing the given point.
     * If the array contains any composites, those composites will be recursively
     * searched to find the most specific child that contains the point.
     * 
     * @param toSearch an array of composites 
     * @param locationToFind a point (in display coordinates)
     * @return the most specific Control that overlaps the given point, or null if none
     */
    public static Control findControl(Control[] toSearch, Point locationToFind) {
        for (int idx = toSearch.length - 1; idx >= 0; idx--) {
            Control next = toSearch[idx];

            if (!next.isDisposed() && next.isVisible()) {

                Rectangle bounds = DragUtil.getDisplayBounds(next);

                if (bounds.contains(locationToFind)) {
                    if (next instanceof Composite) {
                        Control result = findControl((Composite) next,
                                locationToFind);

                        if (result != null) {
                            return result;
                        }
                    }

                    return next;
                }
            }
        }

        return null;
    }

    public static Control[] getAncestors(Control theControl) {
        return getAncestors(theControl, 1);
    }
    
    private static Control[] getAncestors(Control theControl, int children) {
        Control[] result;
        
        if (theControl.getParent() == null) {
            result = new Control[children]; 
        } else {
            result = getAncestors(theControl.getParent(), children + 1);
        }
        
        result[result.length - children] = theControl;
        
        return result;
    }
    
    public static Control findCommonAncestor(Control control1, Control control2) {
        Control[] control1Ancestors = getAncestors(control1);
        Control[] control2Ancestors = getAncestors(control2);
        
        Control mostSpecific = null;
        
        for (int idx = 0; idx < Math.min(control1Ancestors.length, control2Ancestors.length); idx++) {
            Control control1Ancestor = control1Ancestors[idx];
            if (control1Ancestor == control2Ancestors[idx]) {
                mostSpecific = control1Ancestor;
            } else {
                break;
            }
        }
        
        return mostSpecific;
    }
    
    /**
     * Finds the control in the given location
     * 
     * @param toSearch
     * @param locationToFind location (in display coordinates) 
     * @return
     */
    public static Control findControl(Composite toSearch, Point locationToFind) {
        Control[] children = toSearch.getChildren();

        return findControl(children, locationToFind);
    }

    /**
     * 
     * Returns true iff the given rectangle is located in the client area of any
     * monitor.
     * 
     * @param someRectangle a rectangle in display coordinates (not null)
     * @return true iff the given point can be seen on any monitor
     */
    public static boolean intersectsAnyMonitor(Display display,
            Rectangle someRectangle) {
        Monitor[] monitors = display.getMonitors();
    
        for (int idx = 0; idx < monitors.length; idx++) {
            Monitor mon = monitors[idx];
    
            if (mon.getClientArea().intersects(someRectangle)) {
                return true;
            }
        }
    
        return false;
    }

}
