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

package org.eclipse.ui.internal.csm.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.roles.IActivityBinding;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleEvent;
import org.eclipse.ui.roles.IRoleListener;
import org.eclipse.ui.roles.RoleNotDefinedException;

final class Role implements IRole {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Role.class.getName().hashCode();

	private IRoleEvent roleEvent;
	private List roleListeners;
	private boolean defined;
	private String description;
	private String id;
	private String name;
	private Set activityBindings;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient IActivityBinding[] activityBindingsAsArray;
	private transient String string;
	
	Role(String id) {	
		if (id == null)
			throw new NullPointerException();

		this.id = id;
	}

	public void addRoleListener(IRoleListener roleListener) {
		if (roleListener == null)
			throw new NullPointerException();
		
		if (roleListeners == null)
			roleListeners = new ArrayList();
		
		if (!roleListeners.contains(roleListener))
			roleListeners.add(roleListener);
	}

	public int compareTo(Object object) {
		Role role = (Role) object;
		int compareTo = Util.compare(defined, role.defined);

		if (compareTo == 0) {
			compareTo = Util.compare(description, role.description);

			if (compareTo == 0) {		
				compareTo = Util.compare(id, role.id);			
					
				if (compareTo == 0) {
					compareTo = Util.compare(name, role.name);

					if (compareTo == 0) 
						compareTo = Util.compare((Comparable[]) activityBindingsAsArray, (Comparable[]) role.activityBindingsAsArray); 
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Role))
			return false;

		Role role = (Role) object;	
		boolean equals = true;
		equals &= Util.equals(defined, role.defined);
		equals &= Util.equals(description, role.description);
		equals &= Util.equals(id, role.id);
		equals &= Util.equals(name, role.name);
		equals &= Util.equals(activityBindings, role.activityBindings);		
		return equals;
	}

	public Set getActivityBindings() {
		return activityBindings;
	}			
	
	public String getDescription()
		throws RoleNotDefinedException {
		if (!defined)
			throw new RoleNotDefinedException();
			
		return description;	
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName()
		throws RoleNotDefinedException {
		if (!defined)
			throw new RoleNotDefinedException();

		return name;
	}	
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);	
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityBindings);
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public boolean isDefined() {
		return defined;
	}

	public void removeRoleListener(IRoleListener roleListener) {
		if (roleListener == null)
			throw new NullPointerException();

		if (roleListeners != null)
			roleListeners.remove(roleListener);
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(activityBindings);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
	
	void fireRoleChanged() {
		if (roleListeners != null) {
			for (int i = 0; i < roleListeners.size(); i++) {
				if (roleEvent == null)
					roleEvent = new RoleEvent(this);
							
				((IRoleListener) roleListeners.get(i)).roleChanged(roleEvent);
			}				
		}			
	}
	
	boolean setActivityBindings(Set activityBindings) {
		activityBindings = Util.safeCopy(activityBindings, IActivityBinding.class);
		
		if (!Util.equals(activityBindings, this.activityBindings)) {
			this.activityBindings = activityBindings;
			this.activityBindingsAsArray = (IActivityBinding[]) this.activityBindings.toArray(new IActivityBinding[this.activityBindings.size()]);
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
