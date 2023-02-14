/*******************************************************************************
 * Copyright (c) 2010, 2017 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.list.IListProperty;

/**
 * @param <P> type of the parent object
 * @param <E> type of the child elements of the parent
 */
public class VirtualEntry<P, E> {
	private String id;
	private P originalParent;
	private String label;
	private IObservableList<E> list;
	private IListProperty<? super P, E> property;

	public VirtualEntry(String id, IListProperty<? super P, E> property, P originalParent, String label) {
		this.id = id;
		this.originalParent = originalParent;
		this.label = label;
		this.property = property;
		this.list = new WritableList<>();
		final IObservableList<E> origList = property.observe(originalParent);
		list.addAll(cleanedList(origList));

		final IListChangeListener<E> listener = event -> {
			if (!VirtualEntry.this.list.isDisposed()) {
				List<E> clean = cleanedList(event.getObservableList());
				ListDiff<E> diff = Diffs.computeListDiff(VirtualEntry.this.list, clean);
				diff.applyTo(VirtualEntry.this.list);
			}
		};

		origList.addListChangeListener(listener);
	}

	public IListProperty<? super P, E> getProperty() {
		return property;
	}

	private List<E> cleanedList(IObservableList<? extends E> list) {
		List<E> l = new ArrayList<>(list.size());

		for (E o : list) {
			if (accepted(o)) {
				l.add(o);
			}
		}

		return l;
	}

	/**
	 * Can be overridden to filter the child elements.
	 */
	protected boolean accepted(E o) {
		return true;
	}

	public IObservableList<E> getList() {
		return list;
	}

	public P getOriginalParent() {
		return originalParent;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return label;
	}
}
