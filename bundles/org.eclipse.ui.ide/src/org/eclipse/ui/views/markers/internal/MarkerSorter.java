/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.Comparator;

import org.eclipse.jface.viewers.TreeViewer;

abstract class MarkerSorter implements Comparator {

	/**
	 * Sort the array of markers in lastMarkers in place.
	 * 
	 * @param viewer
	 * @param markers
	 */
	public abstract void sort(TreeViewer viewer, MarkerList markers);
}
