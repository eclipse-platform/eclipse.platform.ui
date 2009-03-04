/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222, 263413, 265561
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerSetProperty;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @since 3.3
 * 
 */
public class StructuredViewerFiltersProperty extends ViewerSetProperty {
	public Object getElementType() {
		return ViewerFilter.class;
	}

	protected Set doGetSet(Object source) {
		return new HashSet(Arrays.asList(((StructuredViewer) source)
				.getFilters()));
	}

	public void doSetSet(Object source, Set set, SetDiff diff) {
		StructuredViewer viewer = (StructuredViewer) source;
		viewer.getControl().setRedraw(false);
		try {
			viewer.setFilters((ViewerFilter[]) set.toArray(new ViewerFilter[set
					.size()]));
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}

	public String toString() {
		return "StructuredViewer.filters{} <ViewerFilter>"; //$NON-NLS-1$
	}
}
