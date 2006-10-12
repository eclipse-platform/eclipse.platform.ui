/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.help.internal.FilterableUAElement;
import org.eclipse.help.internal.dynamic.FilterResolver;

/**
 * <p>
 * This class provides the ability to filter out user assistance model elements that
 * support filtering (e.g. <code>IToc</code>, <code>ITopic</code>, ...).
 * Implementations that display such filterable elements should consult this class
 * before attempting to display them.
 * </p>
 * 
 * @since 3.2
 */
public class UAContentFilter {
	
	private static FilterResolver resolver;
	
	/**
	 * <p>
	 * Returns whether or not the given object should be filtered out. This applies
	 * to any user assistance component's elements where filters apply (e.g. help tocs,
	 * topics, intro elements, context help topics). If the element is <code>null</code>
	 * or is not filterable, this method returns <code>false</code>.
	 * </p>
	 * 
	 * @param element the element to check
	 * @return whether or not the element should be filtered out
	 */
	public static boolean isFiltered(Object element) {
		if (element instanceof FilterableUAElement) {
			Map filters = ((FilterableUAElement)element).getFilters();
			if (filters != null) {
				Iterator iter = filters.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry)iter.next();
					String name = (String)entry.getKey();
					String value = (String)entry.getValue();
					boolean not = (value.charAt(0) == '!');
					if (not) {
						value = value.substring(1);
					}
					if (resolver == null) {
						resolver = new FilterResolver();
					}
					if (resolver.isFiltered(name, value, not)) {
						return true;
					}
				}
			}
		}
		else if (element instanceof String) {
			if (resolver == null) {
				resolver = new FilterResolver();
			}
			resolver.isFiltered((String)element);
		}
		return false;
	}
}
