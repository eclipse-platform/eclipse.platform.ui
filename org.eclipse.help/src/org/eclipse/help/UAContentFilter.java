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
public abstract class UAContentFilter {
	
	private static UAContentFilter filterInternal;
	
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
		if (filterInternal != null) {
			return filterInternal.isFilteredInternal(element);
		}
		return false;
	}
	
	/*
	 * Internal; do not use.
	 */
	public abstract boolean isFilteredInternal(Object element);
	
	/*
	 * Internal; do not use.
	 */
	public static void setContentFilterInternal(UAContentFilter filterInternal) {
		UAContentFilter.filterInternal = filterInternal;
	}
}
