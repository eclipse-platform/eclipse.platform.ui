/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An Activity is a definition of a class of operations
 * within the workbench. It is defined with respect to 
 * a role.
 */
public class Activity {

	private String id;
	private String name;
	private String parent;
	boolean enabled;
    
    /**
     * Set of IActivityListeners
     */
    private Set listeners = new HashSet();

	/**
	 * Create a new activity with the suppled id and name.
	 * This will be a top level Activity with no parent.
	 * @param newId
	 * @param newName
	 */
	Activity(String newId, String newName) {
		id = newId;
		name = newName;
	}

	/**
	 * Create a new instance of activity with a parent.
	 * @param newId
	 * @param newName
	 * @param newParent
	 */
	Activity(String newId, String newName, String newParent) {
		this(newId, newName);
		parent = newParent;
	}

    /**
     * 
     * @param listener
     */
    public void addListener(IActivityListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * 
     * @param listener
     */
    public void removeListener(IActivityListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }        
    }

	/**
	 * Return the id of the receiver.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return the name of the receiver.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the id of the parent of the receiver.
	 * @return String
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * Return whether or not this activity is enabled.
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set the enabled state of this activity.  If this activity has a parent 
     * and the enabled state is true then the parent is also activated.  
     * TBD:  how should we do this?  Turning off enablement of a child shouldn't
     * effect the parent so this behaviour is lopsided. 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
        boolean fireEvent = false;
        if (this.enabled != enabled) {
            fireEvent = true;
        }
		this.enabled = enabled;        
//      if (enabled && parent != null) {
//          Activity parentActivity = RoleManager.getInstance().getActivity(parent);
//          if (parentActivity != null) {
//              parentActivity.setEnabled(enabled);
//          }
//      }
        if (fireEvent) {
            fireActivityEvent(new ActivityEvent(this));
        }
	}

	/**
     * Fire the given event to all listeners.
     * 
	 * @param event
	 */
	private void fireActivityEvent(ActivityEvent event) {
        Set listenersCopy;
        synchronized (listeners) {
            listenersCopy = new HashSet(listeners);
        }
        
		for (Iterator i = listenersCopy.iterator(); i.hasNext();) {
			IActivityListener listener = (IActivityListener) i.next();
            listener.activityChanged(event);
		}
	}
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getId();
	}
}
