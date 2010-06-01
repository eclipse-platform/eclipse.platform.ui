/*******************************************************************************
 * Copyright (c) 2009, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 265727)
 ******************************************************************************/

package org.eclipse.core.databinding.property.list;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.MultiList;
import org.eclipse.core.internal.databinding.property.PropertyObservableUtil;

/**
 * A list property for observing multiple list properties in sequence in a
 * combined list.
 * 
 * @since 1.2
 */
public class MultiListProperty extends ListProperty {
	private IListProperty[] properties;
	private Object elementType;

	/**
	 * Constructs a MultiListProperty for observing the specified list
	 * properties in sequence
	 * 
	 * @param properties
	 *            the list properties
	 */
	public MultiListProperty(IListProperty[] properties) {
		this(properties, null);
	}

	/**
	 * Constructs a MultiListProperty for observing the specified list
	 * properties in sequence.
	 * 
	 * @param properties
	 *            the list properties
	 * @param elementType
	 *            the element type of the MultiListProperty
	 */
	public MultiListProperty(IListProperty[] properties, Object elementType) {
		this.properties = properties;
		this.elementType = elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected List doGetList(Object source) {
		List list = new ArrayList();
		for (int i = 0; i < properties.length; i++)
			list.addAll(properties[i].getList(source));
		return list;
	}

	protected void doUpdateList(final Object source, ListDiff diff) {
		diff.accept(new ListDiffVisitor() {
			public void handleAdd(int index, Object element) {
				throw new UnsupportedOperationException();
			}

			public void handleMove(int oldIndex, int newIndex, Object element) {
				throw new UnsupportedOperationException();
			}

			public void handleReplace(int index, Object oldElement,
					Object newElement) {
				int offset = 0;
				for (int i = 0; i < properties.length; i++) {
					List subList = properties[i].getList(source);
					if (index - offset < subList.size()) {
						int subListIndex = index - offset;
						ListDiffEntry[] entries = new ListDiffEntry[] {
								Diffs.createListDiffEntry(subListIndex, false,
										oldElement),
								Diffs.createListDiffEntry(subListIndex, true,
										newElement) };
						ListDiff diff = Diffs.createListDiff(entries);
						properties[i].updateList(source, diff);
						return;
					}
					offset += subList.size();
				}
				throw new IndexOutOfBoundsException("index: " + index //$NON-NLS-1$
						+ ", size: " + offset); //$NON-NLS-1$
			}

			public void handleRemove(int index, Object element) {
				int offset = 0;
				for (int i = 0; i < properties.length; i++) {
					List subList = properties[i].getList(source);
					int subListIndex = index - offset;
					if (subListIndex < subList.size()) {
						ListDiff diff = Diffs.createListDiff(Diffs
								.createListDiffEntry(subListIndex, false,
										element));
						properties[i].updateList(source, diff);
						return;
					}
					offset += subList.size();
				}
				throw new IndexOutOfBoundsException("index: " + index //$NON-NLS-1$
						+ ", size: " + offset); //$NON-NLS-1$
			}
		});
	}

	public IObservableList observe(Realm realm, Object source) {
		IObservableList[] lists = new IObservableList[properties.length];
		for (int i = 0; i < lists.length; i++)
			lists[i] = properties[i].observe(realm, source);
		IObservableList multiList = new MultiList(lists, elementType);

		for (int i = 0; i < lists.length; i++)
			PropertyObservableUtil.cascadeDispose(multiList, lists[i]);

		return multiList;
	}
}
