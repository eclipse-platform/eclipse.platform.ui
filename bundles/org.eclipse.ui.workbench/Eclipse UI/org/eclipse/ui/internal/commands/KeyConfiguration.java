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

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.IKeyConfigurationEvent;
import org.eclipse.ui.commands.IKeyConfigurationListener;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class KeyConfiguration implements IKeyConfiguration {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyConfiguration.class.getName().hashCode();

	private boolean active;
	private IKeyConfigurationEvent keyConfigurationEvent;
	private List keyConfigurationListeners;
	private boolean defined;
	private String description;
	private String id;
	private String name;
	private String parentId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	KeyConfiguration(String id) {	
		if (id == null)
			throw new NullPointerException();

		this.id = id;
	}

	public void addKeyConfigurationListener(IKeyConfigurationListener keyConfigurationListener) {
		if (keyConfigurationListener == null)
			throw new NullPointerException();
		
		if (keyConfigurationListeners == null)
			keyConfigurationListeners = new ArrayList();
		
		if (!keyConfigurationListeners.contains(keyConfigurationListener))
			keyConfigurationListeners.add(keyConfigurationListener);
	}

	public int compareTo(Object object) {
		KeyConfiguration keyConfiguration = (KeyConfiguration) object;
		int compareTo = active == false ? (keyConfiguration.active == true ? -1 : 0) : 1;

		if (compareTo == 0) {
			compareTo = defined == false ? (keyConfiguration.defined == true ? -1 : 0) : 1;
			
			if (compareTo == 0) {
				compareTo = Util.compare(description, keyConfiguration.description);
			
				if (compareTo == 0) {		
					compareTo = id.compareTo(keyConfiguration.id);			
				
					if (compareTo == 0) {
						compareTo = name.compareTo(keyConfiguration.name);
						
						if (compareTo == 0)
							compareTo = Util.compare(parentId, keyConfiguration.parentId);		
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyConfiguration))
			return false;

		KeyConfiguration keyConfiguration = (KeyConfiguration) object;	
		boolean equals = true;
		equals &= active == keyConfiguration.active;
		equals &= defined == keyConfiguration.defined;
		equals &= Util.equals(description, keyConfiguration.description);
		equals &= id.equals(keyConfiguration.id);
		equals &= name.equals(keyConfiguration.name);
		equals &= Util.equals(parentId, keyConfiguration.parentId);
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

	public void removeKeyConfigurationListener(IKeyConfigurationListener keyConfigurationListener) {
		if (keyConfigurationListener == null)
			throw new NullPointerException();

		if (keyConfigurationListeners != null) {
			keyConfigurationListeners.remove(keyConfigurationListener);
			
			if (keyConfigurationListeners.isEmpty())
				keyConfigurationListeners = null;
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
	
	void fireKeyConfigurationChanged() {
		if (keyConfigurationListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(keyConfigurationListeners).iterator();			
			
			if (iterator.hasNext()) {
				if (keyConfigurationEvent == null)
					keyConfigurationEvent = new KeyConfigurationEvent(this);
				
				while (iterator.hasNext())	
					((IKeyConfigurationListener) iterator.next()).keyConfigurationChanged(keyConfigurationEvent);
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
