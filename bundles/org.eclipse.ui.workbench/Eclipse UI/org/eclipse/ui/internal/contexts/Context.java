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

import java.text.Collator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.internal.util.Util;

final class Context implements IContext {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Context.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((IContext) left).getName(), ((IContext) right).getName());
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List contexts) {
		if (contexts == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = contexts.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IContext.class);
			IContext context = (IContext) object;
			sortedMap.put(context.getId(), context);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List contexts) {
		if (contexts == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = contexts.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IContext.class);			
			IContext context = (IContext) object;
			sortedMap.put(context.getName(), context);									
		}			
		
		return sortedMap;
	}

	private boolean active;
	private String description;
	private String id;
	private String name;
	private String parentId;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	Context(boolean active, String description, String id, String name, String parentId) {
		if (id == null || name == null)
			throw new NullPointerException();

		this.active = active;		
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
	}

	public int compareTo(Object object) {
		Context context = (Context) object;
		int compareTo = active == false ? (context.active == true ? -1 : 0) : 1;
		
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
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Context))
			return false;

		Context context = (Context) object;	
		boolean equals = true;
		equals &= active == context.active;
		equals &= Util.equals(description, context.description);
		equals &= id.equals(context.id);
		equals &= name.equals(context.name);
		equals &= Util.equals(parentId, context.parentId);
		return equals;
	}

	public String getDescription() {
		return description;	
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName() {
		return name;
	}	

	public String getParentId() {
		return parentId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + (active ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
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

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(active);
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
}


