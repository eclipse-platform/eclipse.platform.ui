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
package org.eclipse.ui.internal;

/**
 * Represents a boolean value that can be monitored for state changes.
 * The getState() method inherited from Model will always return a non-null
 * Boolean object.
 * 
 * @since 3.0
 */
public class BooleanModel extends Model {

    /**
     * Creates a new BooleanModel with the given initial state
     * 
     * @param initialState initial value of the model
     */
    public BooleanModel(boolean initialState) {
        super(new Boolean(initialState));
    }

    /**
     * Sets the value and notifies all change listeners
     * 
     * @param newValue new 
     */
    public void set(boolean newValue) {
        set(newValue, null);
    }

    /**
     * Sets the value and notifies all change listeners except
     * the listener that caused the change.
     * 
     * @param newValue new boolean value
     * @param origin change listener that caused the change, or null if
     * the change was not caused by a change listener.
     *  
     */
    public void set(boolean newValue, IChangeListener origin) {
        super.setState(new Boolean(newValue), origin);
    }

    /**
     * Returns the current value as a boolean
     * 
     * @return the current value
     */
    public boolean get() {
        return ((Boolean) getState()).booleanValue();
    }
}