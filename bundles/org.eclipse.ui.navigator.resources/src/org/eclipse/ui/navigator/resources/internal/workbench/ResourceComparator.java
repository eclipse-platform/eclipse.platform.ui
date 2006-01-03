/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ui.navigator.resources.internal.workbench;

import java.util.Comparator;

import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2 
 *
 */
class ResourceComparator implements Comparator {

	private ResourceSorter sorter = new ResourceSorter(ResourceSorter.NAME);

	/**
	 * The following compare will sort items based on their type (in the order of: ROOT, PROJECT,
	 * FOLDER, FILE) and then based on their String representation
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return this.sorter.compare(null, o1, o2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {

		return obj instanceof ResourceComparator;
	}
}