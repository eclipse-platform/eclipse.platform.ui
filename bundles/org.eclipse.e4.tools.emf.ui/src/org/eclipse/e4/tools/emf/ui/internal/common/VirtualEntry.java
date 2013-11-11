/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.list.IListProperty;

public abstract class VirtualEntry<M> {
	private String id;
	private Object originalParent;
	private String label;
	private IObservableList list;
	private IListProperty property;

	public VirtualEntry(String id, IListProperty property, Object originalParent, String label) {
		this.id = id;
		this.originalParent = originalParent;
		this.label = label;
		this.property = property;
		this.list = new WritableList();
		final IObservableList origList = property.observe(originalParent);
		list.addAll(cleanedList(origList));

		final IListChangeListener listener = new IListChangeListener() {

			@Override
			public void handleListChange(ListChangeEvent event) {
				if (!VirtualEntry.this.list.isDisposed()) {
					List<Object> clean = cleanedList(event.getObservableList());
					ListDiff diff = Diffs.computeListDiff(VirtualEntry.this.list, clean);
					diff.applyTo(VirtualEntry.this.list);
				}
			}
		};

		origList.addListChangeListener(listener);
	}

	public IListProperty getProperty() {
		return property;
	}

	@SuppressWarnings("unchecked")
	private List<Object> cleanedList(IObservableList list) {
		List<Object> l = new ArrayList<Object>(list.size());

		for (Object o : list) {
			if (accepted((M) o)) {
				l.add(o);
			}
		}

		return l;
	}

	protected abstract boolean accepted(M o);

	public IObservableList getList() {
		return list;
	}

	public Object getOriginalParent() {
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
