/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.csm.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.ui.internal.csm.activities.api.ActivityNotDefinedException;
import org.eclipse.ui.internal.csm.activities.api.IActivity;
import org.eclipse.ui.internal.csm.activities.api.IActivityEvent;
import org.eclipse.ui.internal.csm.activities.api.IActivityListener;
import org.eclipse.ui.internal.util.Util;

final class Activity implements IActivity {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Activity.class.getName().hashCode();

	private boolean active;
	private SortedSet activityBindings;
	private IActivityEvent activityEvent;
	private List activityListeners;
	private boolean defined;
	private String description;
	private String id;
	private String name;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	Activity(String id) {	
		if (id == null)
			throw new NullPointerException();

		this.id = id;
	}

	public void addActivityListener(IActivityListener activityListener) {
		if (activityListener == null)
			throw new NullPointerException();
		
		if (activityListeners == null)
			activityListeners = new ArrayList();
		
		if (!activityListeners.contains(activityListener))
			activityListeners.add(activityListener);
	}

	public int compareTo(Object object) {
		Activity activity = (Activity) object;
		int compareTo = active == false ? (activity.active == true ? -1 : 0) : 1;

		if (compareTo == 0) {
			compareTo = defined == false ? (activity.defined == true ? -1 : 0) : 1;
			
			if (compareTo == 0) {
				compareTo = Util.compare(description, activity.description);
			
				if (compareTo == 0) {		
					compareTo = id.compareTo(activity.id);			
				
					if (compareTo == 0)
						compareTo = Util.compare(name, activity.name);
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Activity))
			return false;

		Activity activity = (Activity) object;	
		boolean equals = true;
		equals &= active == activity.active;
		equals &= defined == activity.defined;
		equals &= Util.equals(description, activity.description);
		equals &= id.equals(activity.id);
		equals &= Util.equals(name, activity.name);
		return equals;
	}

	public Set getActivityBindings() {
		// TODO 
		return null;
	}

	public String getDescription()
		throws ActivityNotDefinedException {
		if (!defined)
			throw new ActivityNotDefinedException();
			
		return description;	
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName()
		throws ActivityNotDefinedException {
		if (!defined)
			throw new ActivityNotDefinedException();

		return name;
	}	

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + (active ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + (defined ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + id.hashCode();
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public boolean isActive() {
		return active;
	}
	
	public boolean isDefined() {
		return defined;
	}

	public void removeActivityListener(IActivityListener activityListener) {
		if (activityListener == null)
			throw new NullPointerException();

		if (activityListeners != null)
			activityListeners.remove(activityListener);
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(active);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
	
	void fireActivityChanged() {
		if (activityListeners != null) {
			for (int i = 0; i < activityListeners.size(); i++) {
				if (activityEvent == null)
					activityEvent = new ActivityEvent(this);
							
				((IActivityListener) activityListeners.get(i)).activityChanged(activityEvent);
			}				
		}			
	}
	
	boolean setActive(boolean active) {
		if (active != this.active) {
			this.active = active;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}

	boolean setDefined(boolean defined) {
		if (defined != this.defined) {
			this.defined = defined;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}

	boolean setDescription(String description) {
		if (!Util.equals(description, this.description)) {
			this.description = description;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}

	boolean setName(String name) {
		if (!Util.equals(name, this.name)) {
			this.name = name;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}
}
