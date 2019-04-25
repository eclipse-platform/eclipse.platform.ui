/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class StructuredViewerFiltersProperty<S extends StructuredViewer> extends ViewerSetProperty<S, ViewerFilter> {
	@Override
	public Object getElementType() {
		return ViewerFilter.class;
	}

	@Override
	protected Set<ViewerFilter> doGetSet(S source) {
		return new HashSet<>(Arrays.asList(source.getFilters()));
	}

	@Override
	public void doSetSet(S source, Set<ViewerFilter> set, SetDiff<ViewerFilter> diff) {
		doSetSet(source, set);
	}

	@Override
	protected void doSetSet(S source, Set<ViewerFilter> set) {
		source.getControl().setRedraw(false);
		try {
			source.setFilters(set.toArray(new ViewerFilter[set.size()]));
		} finally {
			source.getControl().setRedraw(true);
		}
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, SetDiff<ViewerFilter>> listener) {
		return null;
	}

	@Override
	public String toString() {
		return "StructuredViewer.filters{} <ViewerFilter>"; //$NON-NLS-1$
	}
}
