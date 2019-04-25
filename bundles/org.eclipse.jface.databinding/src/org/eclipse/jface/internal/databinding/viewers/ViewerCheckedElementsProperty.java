/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.property.set.DelegatingSetProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerSetProperty;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 3.3
 *
 */
public class ViewerCheckedElementsProperty<S, E> extends DelegatingSetProperty<S, E>
		implements IViewerSetProperty<S, E> {
	ISetProperty<S, E> checkable;
	ISetProperty<S, E> checkboxTableViewer;
	ISetProperty<S, E> checkboxTreeViewer;

	/**
	 * @param elementType
	 */
	@SuppressWarnings("unchecked")
	public ViewerCheckedElementsProperty(Object elementType) {
		super(elementType);
		checkable = (ISetProperty<S, E>) new CheckableCheckedElementsProperty<>(elementType);
		checkboxTableViewer = (ISetProperty<S, E>) new CheckboxTableViewerCheckedElementsProperty<>(elementType);
		checkboxTreeViewer = (ISetProperty<S, E>) new CheckboxTreeViewerCheckedElementsProperty<>(elementType);
	}

	@Override
	protected ISetProperty<S, E> doGetDelegate(S source) {
		if (source instanceof CheckboxTableViewer)
			return checkboxTableViewer;
		if (source instanceof CheckboxTreeViewer)
			return checkboxTreeViewer;
		return checkable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IViewerObservableSet<E> observe(Viewer viewer) {
		return (IViewerObservableSet<E>) observe(DisplayRealm.getRealm(viewer.getControl().getDisplay()),
				(S) viewer);
	}
}