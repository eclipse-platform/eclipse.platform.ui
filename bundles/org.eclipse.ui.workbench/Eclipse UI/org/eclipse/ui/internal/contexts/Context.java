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
import org.eclipse.ui.contexts.IContextDefinition;
import org.eclipse.ui.internal.util.Util;

final class Context implements IContext {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Context.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((IContext) left).getContextDefinition().getName(), ((IContext) right).getContextDefinition().getName());
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
			sortedMap.put(context.getContextDefinition().getId(), context);									
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
			sortedMap.put(context.getContextDefinition().getName(), context);									
		}			
		
		return sortedMap;
	}

	private boolean active;
	private IContextDefinition contextDefinition;
	
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	Context(boolean active, IContextDefinition contextDefinition) {
		if (contextDefinition == null)
			throw new NullPointerException();
		
		this.active = active;
		this.contextDefinition = contextDefinition;
	}
	
	public int compareTo(Object object) {
		Context context = (Context) object;
		int compareTo = active == false ? (context.active == true ? -1 : 0) : 1;
		
		if (compareTo == 0)		
			compareTo = contextDefinition.compareTo(context.contextDefinition);
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Context))
			return false;

		Context context = (Context) object;
		boolean equals = true;
		equals &= active == context.active;
		equals &= contextDefinition.equals(context.contextDefinition);
		return equals;
	}

	public boolean getActive() {
		return active;
	}

	public IContextDefinition getContextDefinition() {
		return contextDefinition;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + (active ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());
			hashCode = hashCode * HASH_FACTOR + contextDefinition.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(active);
			stringBuffer.append(',');
			stringBuffer.append(contextDefinition);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
}
