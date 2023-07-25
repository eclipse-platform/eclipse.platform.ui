/*******************************************************************************
 * Copyright (c) 2023 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * Like {@link ArrayContentProvider} but for JFace tables using SWT.VIRTUAL.
 * Because of the way virtual tables work, sorting and filtering must be
 * performed on the model objects itself, the provider offers the follwoing
 * methods to do this:
 * <ul>
 * <li>{@link #filter(Predicate)}</li>
 * <li>{@link #sort(Comparator)}</li>
 * <li>{@link #filterAndSort(Predicate, Comparator)}</li>
 * </ul>
 *
 * @since 3.31
 *
 */
public class LazyArrayContentProvider implements IIndexableLazyContentProvider {

	private static final Object[] EMPTY_ARRAY = new Object[0];
	private Object[] objects = EMPTY_ARRAY;
	private Object[] filteredObjects;
	private TableViewer tableViewer;
	private boolean parallel;

	/**
	 * Create an instance of the {@link LazyArrayContentProvider} wich do not use
	 * parallel filtering and sort
	 */
	public LazyArrayContentProvider() {
		this(false);
	}

	/**
	 * Creates an instance of the {@link LazyArrayContentProvider} with the given
	 * parallel flag for sorting/filtering.
	 *
	 * @param parallel if <code>true</code> performs sorting and filtering in
	 *                 parallel, if <code>false</code> using only the current
	 *                 thread.
	 */
	public LazyArrayContentProvider(boolean parallel) {
		this.parallel = parallel;
	}

	@Override
	public void updateElement(int index) {
		if (tableViewer != null) {
			Object element = getCurrentObjects()[index];
			tableViewer.replace(element, index);
		}
	}

	private Object[] getCurrentObjects() {
		if (filteredObjects != null) {
			return filteredObjects;
		}
		return objects;
	}

	@Override
	public int findElement(Object element) {
		Object[] currentObjects = getCurrentObjects();
		for (int i = 0; i < currentObjects.length; i++) {
			Object object = currentObjects[i];
			if (element == object) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		filteredObjects = null;
		if (viewer instanceof TableViewer tableViewer) {
			this.tableViewer = tableViewer;
			if (newInput instanceof Collection<?> collection) {
				objects = collection.toArray();
			} else if (newInput instanceof Object[] objectArray) {
				objects = objectArray;
			} else {
				objects = EMPTY_ARRAY;
			}
			tableViewer.setItemCount(objects.length);
		} else {
			this.tableViewer = null;
			this.objects = EMPTY_ARRAY;
		}
	}

	/**
	 * Sort the current contents of the table
	 *
	 * @param <T>
	 * @param comparator the comparator for comapring objects
	 */
	public <T> void sort(Comparator<T> comparator) {
		filterAndSort(null, comparator);
	}

	/**
	 * Filter the current contents of the table
	 *
	 * @param <T>
	 * @param predicate the predicate for filtering
	 */
	public <T> void filter(Predicate<T> predicate) {
		filterAndSort(predicate, null);
	}

	/**
	 * Filter and sort the data with the given predicate and comparator.
	 *
	 * @param <T>
	 * @param predicate  the predicate for filtering, might be <code>null</code> if
	 *                   no filtering is desired.
	 * @param comparator the comparator for comapring objects, might be
	 *                   <code>null</code> if the original ordering should be
	 *                   retained.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void filterAndSort(Predicate<T> predicate, Comparator<T> comparator) {
		if (predicate == null && comparator == null) {
			return;
		}
		Stream<?> stream = Arrays.stream(getCurrentObjects());
		if (parallel) {
			stream = stream.parallel();
		}
		if (predicate != null) {
			stream = stream.filter((Predicate) predicate);
		}
		if (comparator != null) {
			stream = stream.sorted((Comparator) comparator);
		}
		filteredObjects = stream.toArray();
		if (tableViewer != null) {
			tableViewer.setItemCount(filteredObjects.length);
			tableViewer.refresh();
		}
	}

	/**
	 * resets any filtering or sorting of the data
	 */
	public void reset() {
		filteredObjects = null;
		if (tableViewer != null) {
			tableViewer.setItemCount(objects.length);
		}
	}

}
