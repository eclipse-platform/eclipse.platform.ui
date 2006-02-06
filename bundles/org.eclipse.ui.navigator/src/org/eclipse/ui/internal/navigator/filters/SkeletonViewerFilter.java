/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * 
 * A no-op viewer filter used to prevent null return values from
 * {@link CommonFilterDescriptor#createFilter()}.
 * 
 * @since 3.2
 * 
 */
public class SkeletonViewerFilter extends ViewerFilter {

	/**
	 * The singleton instance.
	 */
	public static final SkeletonViewerFilter INSTANCE = new SkeletonViewerFilter();

	private SkeletonViewerFilter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		return true;
	}

}
