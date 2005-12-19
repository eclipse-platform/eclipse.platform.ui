/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.Comparator;

import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Sorter for use by Common Navigator
 */
public class ResourceModelSorter extends ResourceSorter implements Comparator {

	public ResourceModelSorter() {
		super(TYPE);
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return compare(null, o1, o2);
	}

}
