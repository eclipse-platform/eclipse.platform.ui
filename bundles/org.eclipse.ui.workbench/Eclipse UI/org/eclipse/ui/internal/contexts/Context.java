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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextEvent;
import org.eclipse.ui.contexts.IContextListener;
import org.eclipse.ui.contexts.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Context implements IContext {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Context.class.getName().hashCode();

	private boolean active;
	private IContextEvent contextEvent;
	private List contextListeners;
	private boolean defined;
	private String description;
	private String id;
	private String name;
	private String parentId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	Context(String id) {	
		if (id == null)
			throw new NullPointerException();

		this.id = id;
	}

	public void addContextListener(IContextListener contextListener) {
		if (contextListener == null)
			throw new NullPointerException();
		
		if (contextListeners == null)
			contextListeners = new ArrayList();
		
		if (!contextListeners.contains(contextListener))
			contextListeners.add(contextListener);
	}

	public int compareTo(Object object) {
		Context context = (Context) object;
		int compareTo = active == false ? (context.active == true ? -1 : 0) : 1;

		if (compareTo == 0) {
			compareTo = defined == false ? (context.defined == true ? -1 : 0) : 1;
			
			if (compareTo == 0) {
				compareTo = Util.compare(description, context.description);
			
				if (compareTo == 0) {		
					compareTo = id.compareTo(context.id);			
				
					if (compareTo == 0) {
						compareTo = name.compareTo(context.name);
						
						if (compareTo == 0)
							compareTo = Util.compare(parentId, context.parentId);		
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Context))
			return false;

		Context context = (Context) object;	
		boolean equals = true;
		equals &= active == context.active;
		equals &= defined == context.defined;
		equals &= Util.equals(description, context.description);
		equals &= id.equals(context.id);
		equals &= name.equals(context.name);
		equals &= Util.equals(parentId, context.parentId);
		return equals;
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

	public String getParentId()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return parentId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + (active ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + (defined ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + id.hashCode();
			hashCode = hashCode * HASH_FACTOR + name.hashCode();
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
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

	public void removeContextListener(IContextListener contextListener) {
		if (contextListener == null)
			throw new NullPointerException();

		if (contextListeners != null) {
			contextListeners.remove(contextListener);
			
			if (contextListeners.isEmpty())
				contextListeners = null;
		}
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
			stringBuffer.append(',');
			stringBuffer.append(parentId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
	
	void fireContextChanged() {
		if (contextListeners != null) {
			for (int i = 0; i < contextListeners.size(); i++) {
				if (contextEvent == null)
					contextEvent = new ContextEvent(this);
							
				((IContextListener) contextListeners.get(i)).contextChanged(contextEvent);
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
		if (name == null)
			throw new NullPointerException();
		
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
}
