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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents an object with state that will notifies a set of listeners
 * whenever its state changes 
 */
public class Model {

    /**
     * Current state of the model
     */
    private Object state;

    /**
     * Set of objects that will be notified about state changes
     */
    private List views = new ArrayList(1);

    /**
     * Creates a new model with the given initial state.
     * 
     * @param initialState
     */
    public Model(Object initialState) {
        state = initialState;
    }

    /**
     * Returns the current state of the model.
     * 
     * @return the current model state
     */
    public Object getState() {
        return state;
    }

    /**
     * Sets the current state of the model.
     * 
     * @param newState the new state of the model
     * @param toOmit change listener that should be omitted from change notifications (or null if all
     * listeners should be notified). 
     */
    public void setState(Object newState, IChangeListener toOmit) {
        if (areEqual(newState, state)) {
            return;
        }

        state = newState;

        Iterator iter = views.iterator();
        while (iter.hasNext()) {
            IChangeListener next = (IChangeListener) iter.next();

            if (next != toOmit) {
                next.update(true);
            }
        }
    }

    private boolean areEqual(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        if (o2 == null) {
            return false;
        }

        return o1.equals(o2);
    }

    /**
     * Adds the given listener to the set of listeners that will be
     * notified when the state changes. 
     * 
     * @param toAdd
     */
    public void addChangeListener(IChangeListener changeListener) {
        changeListener.update(false);
        views.add(changeListener);
    }

    /**
     * Stops this model from sending change events from the given listener.
     * 
     * @param toRemove
     */
    public void removeChangeListener(IChangeListener changeListener) {
        views.remove(changeListener);
    }

}
