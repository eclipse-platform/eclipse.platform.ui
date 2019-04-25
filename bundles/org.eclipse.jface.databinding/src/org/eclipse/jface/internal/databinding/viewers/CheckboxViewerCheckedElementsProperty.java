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
 *     Matthew Hall - bugs 195222, 259380, 263413, 265561
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerSetProperty;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 3.3
 *
 */
public abstract class CheckboxViewerCheckedElementsProperty<S extends ICheckable, E> extends ViewerSetProperty<S, E> {
	private final Object elementType;

	/**
	 * @param elementType
	 */
	public CheckboxViewerCheckedElementsProperty(Object elementType) {
		this.elementType = elementType;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	protected final Set<E> createElementSet(StructuredViewer viewer) {
		return ViewerElementSet.withComparer(viewer.getComparer());
	}

	@Override
	protected void doUpdateSet(S source, SetDiff<E> diff) {
		for (E e : diff.getAdditions())
			source.setChecked(e, true);
		for (E e : diff.getRemovals())
			source.setChecked(e, false);
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, SetDiff<E>> listener) {
		return new CheckStateListener(this, listener);
	}

	private class CheckStateListener extends NativePropertyListener<S, SetDiff<E>> implements ICheckStateListener {
		private CheckStateListener(IProperty property, ISimplePropertyListener<S, SetDiff<E>> listener) {
			super(property, listener);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			E element = (E) event.getElement();
			boolean checked = event.getChecked();
			Set<E> elementSet = createElementSet((StructuredViewer) event.getCheckable());
			elementSet.add(element);
			Set<E> additions = checked ? elementSet : Collections.emptySet();
			Set<E> removals = checked ? Collections.emptySet() : elementSet;
			SetDiff<E> diff = Diffs.createSetDiff(additions, removals);
			fireChange((S) event.getSource(), diff);
		}

		@Override
		public void doAddTo(S source) {
			source.addCheckStateListener(this);
		}

		@Override
		public void doRemoveFrom(S source) {
			source.removeCheckStateListener(this);
		}
	}

	@Override
	public String toString() {
		String s = "ICheckable.checkedElements{}"; //$NON-NLS-1$
		if (elementType != null)
			s += " <" + elementType + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
