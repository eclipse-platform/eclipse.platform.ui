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
package org.eclipse.ui.internal;

/**
 * Represents a single integer that can send notifications when it changes.
 * IChangeListeners can be attached which will receive notifications whenever
 * the integer changes.
 */
public class IntModel extends Model {
    public IntModel(int initialValue) {
        super(new Integer(initialValue));
    }

    /**
     * Sets the value of the integer and notifies all
     * change listeners except for the one that caused the change.
     * 
     * @param newValue the new value of the integer
     */
    public void set(int newValue, IChangeListener source) {
        setState(new Integer(newValue), source);
    }

    /**
     * Sets the value of the integer and notifies all change listeners
     * of the change.
     * 
     * @param newValue the new value of the integer
     */
    public void set(int newValue) {
        setState(new Integer(newValue), null);
    }

    /**
     * Returns the value of the integer.
     * 
     * @return the value of the integer
     */
    public int get() {
        return ((Integer) getState()).intValue();
    }
}
