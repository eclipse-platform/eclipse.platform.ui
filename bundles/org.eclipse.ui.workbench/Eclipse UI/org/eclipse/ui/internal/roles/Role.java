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

package org.eclipse.ui.internal.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.roles.IActivityBinding;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleEvent;
import org.eclipse.ui.roles.IRoleListener;
import org.eclipse.ui.roles.NotDefinedException;

final class Role implements IRole {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Role.class.getName().hashCode();

	private Set activityBindings;
	private boolean defined;
	private String description;
	private String id;
	private String name;
	private List roleListeners;
	private RoleManager roleManager;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient IActivityBinding[] activityBindingsAsArray;
	private transient String string;
	
	Role(RoleManager roleManager, String id) {	
		if (roleManager == null || id == null)
			throw new NullPointerException();

		this.roleManager = roleManager;
		this.id = id;
	}

	public void addRoleListener(IRoleListener roleListener) {
		if (roleListener == null)
			throw new NullPointerException();
		
		if (roleListeners == null)
			roleListeners = new ArrayList();
		
		if (!roleListeners.contains(roleListener))
			roleListeners.add(roleListener);
		
		roleManager.getRolesWithListeners().add(this);		
	}

	public int compareTo(Object object) {
		Role castedObject = (Role) object;		
		int compareTo = Util.compare((Comparable[]) activityBindingsAsArray, (Comparable[]) castedObject.activityBindingsAsArray);
		
		if (compareTo == 0) {
			compareTo = Util.compare(defined, castedObject.defined);
	
			if (compareTo == 0) {
				compareTo = Util.compare(description, castedObject.description);
	
				if (compareTo == 0) {		
					compareTo = Util.compare(id, castedObject.id);			
						
					if (compareTo == 0)
						compareTo = Util.compare(name, castedObject.name);
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Role))
			return false;

		Role castedObject = (Role) object;	
		boolean equals = true;
		equals &= Util.equals(activityBindings, castedObject.activityBindings);		
		equals &= Util.equals(defined, castedObject.defined);
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		return equals;
	}

	public Set getActivityBindings() {
		return activityBindings;
	}			
	
	public String getDescription()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();
			
		return description;	
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return name;
	}	
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityBindings);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);	
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
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
		
		if (roleListeners.isEmpty())
			roleManager.getRolesWithListeners().remove(this);		
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(activityBindings);
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
	
	void fireRoleChanged(IRoleEvent roleEvent) {
		if (roleEvent == null)
			throw new NullPointerException();
		
		if (roleListeners != null)
			for (int i = 0; i < roleListeners.size(); i++)
				((IRoleListener) roleListeners.get(i)).roleChanged(roleEvent);
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
