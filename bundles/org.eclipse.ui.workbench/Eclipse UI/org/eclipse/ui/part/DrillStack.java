/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import java.util.Stack;

/* (non-Javadoc)
 * A <code>DrillStack</code> manages a stack of DrillFrames.
 * This class is not intended for use beyond the package.
 */
/* package */class DrillStack {
    Stack fStack = null;

    /**
     * Allocates a new DrillStack.
     */
    public DrillStack() {
        reset();
    }

    /**
     * Adds a drill frame to the stack.
     *
     * @param oRecord the new drill frame
     */
    public DrillFrame add(DrillFrame oRecord) {
        fStack.push(oRecord);
        return oRecord;
    }

    /**
     * Returns true if backward navigation is possible.  This is only true
     * if the stack size is greater than 0.
     *
     * @return true if backward navigation is possible
     */
    public boolean canGoBack() {
        return (fStack.size() > 0);
    }

    /**
     * Returns true if "go home" is possible.  This is only true
     * if the stack size is greater than 0.
     *
     * @return true if "go home" is possible
     */
    public boolean canGoHome() {
        return (fStack.size() > 0);
    }

    /**
     * Navigate backwards one record.
     */
    public DrillFrame goBack() {
        DrillFrame aFrame = (DrillFrame) fStack.pop();
        return aFrame;
    }

    /**
     * Navigate to the home record.
     */
    public DrillFrame goHome() {
        DrillFrame aFrame = (DrillFrame) fStack.elementAt(0);
        reset();
        return aFrame;
    }

    /**
     * Clears the navigation stack.
     */
    public void reset() {
        fStack = new Stack();
    }

    /**
     * Returns the stack size.
     *
     * @return the stack size
     */
    public int size() {
        return fStack.size();
    }

    /**
     * Returns the top element on the stack.
     *
     * @return the top element on the stack
     */
    public DrillFrame top() {
        return (DrillFrame) fStack.peek();
    }
}
