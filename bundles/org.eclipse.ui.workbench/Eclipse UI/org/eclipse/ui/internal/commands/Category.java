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
import java.util.List;

import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICategoryEvent;
import org.eclipse.ui.commands.ICategoryListener;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Category implements ICategory {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Category.class.getName().hashCode();

	private ICategoryEvent categoryEvent;
	private List categoryListeners;
	private boolean defined;
	private String description;
	private String id;
	private String name;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	Category(String id) {	
		if (id == null)
			throw new NullPointerException();

		this.id = id;
	}

	public void addCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null)
			throw new NullPointerException();
		
		if (categoryListeners == null)
			categoryListeners = new ArrayList();
		
		if (!categoryListeners.contains(categoryListener))
			categoryListeners.add(categoryListener);
	}

	public int compareTo(Object object) {
		Category category = (Category) object;
		int compareTo = defined == false ? (category.defined == true ? -1 : 0) : 1;
			
		if (compareTo == 0) {
			compareTo = Util.compare(description, category.description);
		
			if (compareTo == 0) {		
				compareTo = id.compareTo(category.id);			
			
				if (compareTo == 0)
					compareTo = name.compareTo(category.name);
			}
		}

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Category))
			return false;

		Category category = (Category) object;	
		boolean equals = true;
		equals &= defined == category.defined;
		equals &= Util.equals(description, category.description);
		equals &= id.equals(category.id);
		equals &= name.equals(category.name);
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

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + (defined ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + id.hashCode();
			hashCode = hashCode * HASH_FACTOR + name.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public boolean isDefined() {
		return defined;
	}

	public void removeCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null)
			throw new NullPointerException();

		if (categoryListeners != null) {
			categoryListeners.remove(categoryListener);
			
			if (categoryListeners.isEmpty())
				categoryListeners = null;
		}
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
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}

	void fireCategoryChanged() {
		if (categoryListeners != null) {
			for (int i = 0; i < categoryListeners.size(); i++) {
				if (categoryEvent == null)
					categoryEvent = new CategoryEvent(this);
							
				((ICategoryListener) categoryListeners.get(i)).categoryChanged(categoryEvent);
			}				
		}		
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
}
