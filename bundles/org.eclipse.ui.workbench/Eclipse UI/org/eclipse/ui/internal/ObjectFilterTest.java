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
package org.eclipse.ui.internal;


import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.IActionFilter;

/**
 * An ObjectFilterTest is used to read an object filter from XML,
 * and evaluate the results for a given object.
 */
public class ObjectFilterTest {
	private HashMap filterElements;

	/**
	 * Create a new object filter. 
	 */	
	public ObjectFilterTest() {
	}
	
	/**
	 * Add a filter element to the test.  This element must contain
	 * a name value filter pair, as defined by the 
	 * <code>org.eclipse.ui.actionFilters</code> extension point.
	 */
	public boolean addFilterElement(IConfigurationElement element) {
		String name = element.getAttribute("name");//$NON-NLS-1$
		if (name == null)
			return false;
		
		// Get positive property.
		String value = element.getAttribute("value");//$NON-NLS-1$
		if (value == null)
			return false;
		if (filterElements==null) 
			filterElements = new HashMap();
		filterElements.put(name, value);
		return true;
	}
	
	/**
	 * Returns whether the object filter correctly matches a
	 * given object.  The results will be <code>true</code> if there is
	 * a filter match.  
	 * <p>
	 * If <code>adaptable is true</code>, the result will also be
	 * <code>rue</code> if the object is a wrapper for a resource, and
	 * the resource produces a filter match.
	 * </p>
	 * 
	 * @param object the object to examine
	 * @returns <code>true</code> if there is a filter match.  
	 */
	public boolean matches(Object object, boolean adaptable) {
		// Optimize it.
		if (filterElements == null)
			return true;
			
		// Try out the object.
		if (this.preciselyMatches(object))
			return true;
			
		// If not adaptable, or the object is a resource, just return.
		if (!adaptable)
			return false;
		if (object instanceof IResource)
			return false;
			
		// Try out the underlying resource.
		IResource res = null;
		if (object instanceof IAdaptable)
			res = (IResource)((IAdaptable)object).getAdapter(IResource.class);
		if (res == null)
			return false;
		return this.preciselyMatches(res);
	}

	/**
	 * Returns whether the object filter correctly matches a given object.
	 */
	private boolean preciselyMatches(Object object) {
		// Get the action filter.
		IActionFilter filter = null;
		if (object instanceof IActionFilter)
			filter = (IActionFilter)object;
		else if (object instanceof IAdaptable)
			filter = (IActionFilter)((IAdaptable)object).getAdapter(IActionFilter.class);
		if (filter == null)
			return false;
			
		// Run the action filter.
		Iterator iter = filterElements.keySet().iterator();
		while (iter.hasNext()) {
			String name = (String)iter.next();
			String value = (String)filterElements.get(name);
			if (!filter.testAttribute(object, name, value))
				return false;
		}
		return true;
	}
}


