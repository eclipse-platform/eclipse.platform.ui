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
 *     Matthew Hall - bug 259380
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckable;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 3.3
 */
public class CheckboxTreeViewerCheckedElementsProperty<S extends ICheckable, E>
		extends CheckboxViewerCheckedElementsProperty<S, E> {
	/**
	 * @param elementType
	 */
	public CheckboxTreeViewerCheckedElementsProperty(Object elementType) {
		super(elementType);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Set<E> doGetSet(S source) {
		CheckboxTreeViewer viewer = (CheckboxTreeViewer) source;
		Set<E> set = createElementSet(viewer);
		set.addAll((List<E>) Arrays.asList(viewer.getCheckedElements()));
		return set;
	}

	@Override
	protected void doSetSet(S source, Set<E> set, SetDiff<E> diff) {
		doSetSet(source, set);
	}

	@Override
	protected void doSetSet(S source, Set<E> set) {
		CheckboxTreeViewer viewer = (CheckboxTreeViewer) source;
		viewer.setCheckedElements(set.toArray());
	}

	@Override
	public String toString() {
		String s = "CheckboxTreeViewer.checkedElements{}"; //$NON-NLS-1$
		if (getElementType() != null)
			s += " <" + getElementType() + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
