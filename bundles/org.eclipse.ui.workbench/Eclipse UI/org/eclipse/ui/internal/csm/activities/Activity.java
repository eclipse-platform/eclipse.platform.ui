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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.activities.ActivityNotDefinedException;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityEvent;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.IPatternBinding;
import org.eclipse.ui.internal.util.Util;

final class Activity implements IActivity {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Activity.class.getName().hashCode();

	private boolean active;
	private IActivityEvent activityEvent;
	private List activityListeners;
	private boolean defined;
	private String description;
	private boolean enabled;
	private String id;
	private String name;
	private String parentId;
	private List patternBindings;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient IPatternBinding[] patternBindingsAsArray;
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
		int compareTo = Util.compare(active, activity.active);

		if (compareTo == 0) {
			compareTo = Util.compare(defined, activity.defined);
			
			if (compareTo == 0) {
				compareTo = Util.compare(description, activity.description);

				if (compareTo == 0) {
					compareTo = Util.compare(enabled, activity.enabled);
								
					if (compareTo == 0) {		
						compareTo = Util.compare(id, activity.id);			
					
						if (compareTo == 0) {
							compareTo = Util.compare(name, activity.name);

							if (compareTo == 0) {
								compareTo = Util.compare(parentId, activity.parentId);

								if (compareTo == 0) 
									compareTo = Util.compare((Comparable[]) patternBindingsAsArray, (Comparable[]) activity.patternBindingsAsArray); 
							}
						}
					}
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
		equals &= Util.equals(active, activity.active);
		equals &= Util.equals(defined, activity.defined);
		equals &= Util.equals(description, activity.description);
		equals &= Util.equals(enabled, activity.enabled);
		equals &= Util.equals(id, activity.id);
		equals &= Util.equals(name, activity.name);
		equals &= Util.equals(parentId, activity.parentId);
		equals &= Util.equals(patternBindings, activity.patternBindings);		
		return equals;
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

	public String getParentId()
		throws ActivityNotDefinedException {
		if (!defined)
			throw new ActivityNotDefinedException();

		return parentId;
	}			
	
	public List getPatternBindings() {
		return patternBindings;
	}		
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(active);			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);	
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(enabled);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(patternBindings);
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

	public boolean isEnabled() {
		return enabled;
	}

	public boolean match(String string) {
		boolean match = false;
			
		if (isDefined())
			for (Iterator iterator = patternBindings.iterator(); iterator.hasNext();) {
				IPatternBinding patternBinding = (IPatternBinding) iterator.next();
			
				if (patternBinding.isInclusive() && !match)
					match = patternBinding.getPattern().matcher(string).matches();
				else if (!patternBinding.isInclusive() && match)
					match = !patternBinding.getPattern().matcher(string).matches();
			}

		return match;
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
			stringBuffer.append(enabled);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(parentId);
			stringBuffer.append(',');
			stringBuffer.append(patternBindings);
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

	boolean setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
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

	boolean setParentId(String parentId) {
		if (!Util.equals(parentId, this.parentId)) {
			this.parentId = parentId;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}	
	
	boolean setPatternBindings(List patternBindings) {
		patternBindings = Util.safeCopy(patternBindings, IPatternBinding.class);
		
		if (!Util.equals(patternBindings, this.patternBindings)) {
			this.patternBindings = patternBindings;
			this.patternBindingsAsArray = (IPatternBinding[]) this.patternBindings.toArray(new IPatternBinding[this.patternBindings.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		
	
		return false;
	}	
}
