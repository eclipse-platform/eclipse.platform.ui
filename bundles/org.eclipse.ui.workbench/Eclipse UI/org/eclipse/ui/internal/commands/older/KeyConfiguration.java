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

package org.eclipse.ui.internal.commands.older;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.commands.api.older.IKeyConfiguration;
import org.eclipse.ui.internal.commands.api.older.IKeyConfigurationEvent;
import org.eclipse.ui.internal.commands.api.older.IKeyConfigurationListener;
import org.eclipse.ui.internal.commands.api.older.NotDefinedException;
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
		KeyConfiguration castedObject = (KeyConfiguration) object;
		int compareTo = active == false ? (castedObject.active == true ? -1 : 0) : 1;

		if (compareTo == 0) {
			compareTo = defined == false ? (castedObject.defined == true ? -1 : 0) : 1;
			
			if (compareTo == 0) {
				compareTo = Util.compare(description, castedObject.description);
			
				if (compareTo == 0) {		
					compareTo = id.compareTo(castedObject.id);			
				
					if (compareTo == 0) {
						compareTo = Util.compare(name, castedObject.name);
						
						if (compareTo == 0)
							compareTo = Util.compare(parentId, castedObject.parentId);		
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyConfiguration))
			return false;

		KeyConfiguration castedObject = (KeyConfiguration) object;	
		boolean equals = true;
		equals &= active == castedObject.active;
		equals &= defined == castedObject.defined;
		equals &= Util.equals(description, castedObject.description);
		equals &= id.equals(castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		equals &= Util.equals(parentId, castedObject.parentId);
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
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
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

		if (keyConfigurationListeners != null)
			keyConfigurationListeners.remove(keyConfigurationListener);
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
			for (int i = 0; i < keyConfigurationListeners.size(); i++) {
				if (keyConfigurationEvent == null)
					keyConfigurationEvent = new KeyConfigurationEvent(this);
							
				((IKeyConfigurationListener) keyConfigurationListeners.get(i)).keyConfigurationChanged(keyConfigurationEvent);
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
