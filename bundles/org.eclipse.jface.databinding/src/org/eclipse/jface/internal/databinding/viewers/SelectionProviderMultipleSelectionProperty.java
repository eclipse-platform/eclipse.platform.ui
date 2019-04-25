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
 *     Matthew Hall - bugs 195222, 263413, 265561
 *     Ovidio Mallo - bug 270494
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerListProperty;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the list
 *
 * @since 3.3
 *
 */
public class SelectionProviderMultipleSelectionProperty<S extends ISelectionProvider, E>
		extends ViewerListProperty<S, E> {

	private final boolean isPostSelection;
	private final Object elementType;

	/**
	 * Constructor.
	 *
	 * @param isPostSelection Whether the post selection or the normal selection is
	 *                        to be observed.
	 * @param elementType     Element type of the property.
	 */
	public SelectionProviderMultipleSelectionProperty(boolean isPostSelection, Object elementType) {
		this.isPostSelection = isPostSelection;
		this.elementType = elementType;
	}

	/**
	 * Creates a property with a null value type.
	 *
	 * @param isPostSelection Whether the post selection or the normal selection is
	 *                        to be observed.
	 */
	public SelectionProviderMultipleSelectionProperty(boolean isPostSelection) {
		this(isPostSelection, null);
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected List<E> doGetList(ISelectionProvider source) {
		ISelection selection = source.getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toList();
		}
		return Collections.emptyList();
	}

	@Override
	protected void doSetList(S source, List<E> list, ListDiff<E> diff) {
		doSetList(source, list);
	}

	@Override
	protected void doSetList(S source, List<E> list) {
		source.setSelection(new StructuredSelection(list));
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ListDiff<E>> listener) {
		return new SelectionChangedListener<>(this, listener, isPostSelection);
	}

	@Override
	public String toString() {
		return isPostSelection ? "IPostSelectionProvider.postSelection[]" //$NON-NLS-1$
				: "ISelectionProvider.selection[]"; //$NON-NLS-1$
	}
}
