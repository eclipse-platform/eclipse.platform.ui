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
 *     Matthew Hall - initial API and implementation (bug 260337)
 *     Matthew Hall - bug 283428
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 481928
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * Helper methods for binding observables to a {@link StructuredViewer} or
 * {@link AbstractTableViewer}.
 *
 * @since 1.3
 */
public class ViewerSupport {
	/**
	 * Binds the viewer to the specified input, using the specified label
	 * property to generate labels.
	 *
	 * @param viewer
	 *            the viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param labelProperty
	 *            the property to use for labels
	 */
	public static <E> void bind(StructuredViewer viewer, IObservableList<E> input,
			IValueProperty<? super E, ?> labelProperty) {
		@SuppressWarnings("unchecked")
		IValueProperty<? super E, ?>[] labelPropertyArray = new IValueProperty[] { labelProperty };
		bind(viewer, input, labelPropertyArray);
	}

	/**
	 * Binds the viewer to the specified input, using the specified label
	 * properties to generate labels.
	 *
	 * @param viewer
	 *            the viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param labelProperties
	 *            the respective properties to use for labels in each of the
	 *            viewer's columns
	 */
	@SafeVarargs
	public static <E> void bind(StructuredViewer viewer, IObservableList<E> input,
			IValueProperty<? super E, ?>... labelProperties) {
		ObservableListContentProvider<E> contentProvider = new ObservableListContentProvider<>();
		if (viewer.getInput() != null)
			viewer.setInput(null);
		viewer.setContentProvider(contentProvider);

		// Use <?, ?> in parameter type but cast to object to avoid
		// being too inconvenient to callers
		viewer.setLabelProvider(new ObservableMapLabelProvider(
				Properties.observeEach(contentProvider.getKnownElements(), labelProperties)));
		if (input != null)
			viewer.setInput(input);
	}

	/**
	 * Binds the viewer to the specified input, using the specified label
	 * property to generate labels.
	 *
	 * @param viewer
	 *            the viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param labelProperty
	 *            the property to use for labels
	 */
	public static <E> void bind(StructuredViewer viewer, IObservableSet<E> input,
			IValueProperty<? super E, ?> labelProperty) {
		@SuppressWarnings("unchecked")
		IValueProperty<? super E, ?>[] labelPropertyArray = new IValueProperty[] { labelProperty };
		bind(viewer, input, labelPropertyArray);
	}

	/**
	 * Binds the viewer to the specified input, using the specified label
	 * properties to generate labels.
	 *
	 * @param viewer
	 *            the viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param labelProperties
	 *            the respective properties to use for labels in each of the
	 *            viewer's columns
	 */
	@SafeVarargs
	public static <E> void bind(StructuredViewer viewer, IObservableSet<E> input,
			IValueProperty<? super E, ?>... labelProperties) {
		ObservableSetContentProvider<E> contentProvider = new ObservableSetContentProvider<>();
		if (viewer.getInput() != null)
			viewer.setInput(null);
		viewer.setContentProvider(contentProvider);

		viewer.setLabelProvider(new ObservableMapLabelProvider(
				Properties.observeEach(contentProvider.getKnownElements(), labelProperties)));
		if (input != null)
			viewer.setInput(input);
	}

	/**
	 * Binds the viewer to the specified input, using the specified children
	 * property to generate child nodes, and the specified label property to
	 * generate labels.
	 *
	 * @param viewer
	 *            the tree viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param childrenProperty
	 *            the property to use as the children of an element
	 * @param labelProperty
	 *            the property to use for labels
	 */
	public static <E> void bind(AbstractTreeViewer viewer, E input,
			IListProperty<? super E, ? extends E> childrenProperty, IValueProperty<? super E, ?> labelProperty) {
		@SuppressWarnings("unchecked")
		IValueProperty<? super E, ?>[] labelPropertyArray = new IValueProperty[] { labelProperty };
		bind(viewer, input, childrenProperty, labelPropertyArray);
	}

	/**
	 * Binds the viewer to the specified input, using the specified children
	 * property to generate child nodes, and the specified label properties to
	 * generate labels.
	 *
	 * @param viewer
	 *            the tree viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param childrenProperty
	 *            the property to use as the children of an element
	 * @param labelProperties
	 *            the respective properties to use for labels in each of the
	 *            viewer's columns
	 */
	@SafeVarargs
	public static <E> void bind(AbstractTreeViewer viewer, E input,
			IListProperty<? super E, ? extends E> childrenProperty, IValueProperty<? super E, ?>... labelProperties) {
		Realm realm = DisplayRealm.getRealm(viewer.getControl().getDisplay());

		ObservableListTreeContentProvider<? extends E> contentProvider = new ObservableListTreeContentProvider<>(
				childrenProperty.listFactory(realm), null);
		if (viewer.getInput() != null)
			viewer.setInput(null);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ObservableMapLabelProvider(
				Properties.observeEach(contentProvider.getKnownElements(), labelProperties)));
		if (input != null)
			viewer.setInput(input);
	}

	/**
	 * Binds the viewer to the specified input, using the specified children
	 * property to generate child nodes, and the specified label property to
	 * generate labels.
	 *
	 * @param viewer
	 *            the tree viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param childrenProperty
	 *            the property to use as the children of an element
	 * @param labelProperty
	 *            the property to use for labels
	 */
	public static <E> void bind(AbstractTreeViewer viewer, E input,
			ISetProperty<? super E, ? extends E> childrenProperty,
			IValueProperty<? super E, ?> labelProperty) {
		@SuppressWarnings("unchecked")
		IValueProperty<? super E, ?>[] labelPropertyArray = new IValueProperty[] { labelProperty };
		bind(viewer, input, childrenProperty, labelPropertyArray);
	}

	/**
	 * Binds the viewer to the specified input, using the specified children
	 * property to generate child nodes, and the specified label properties to
	 * generate labels.
	 *
	 * @param viewer
	 *            the tree viewer to set up
	 * @param input
	 *            the input to set on the viewer
	 * @param childrenProperty
	 *            the property to use as the children of an element
	 * @param labelProperties
	 *            the respective properties to use for labels in each of the
	 *            viewer's columns
	 */
	@SafeVarargs
	public static <E> void bind(AbstractTreeViewer viewer, E input,
			ISetProperty<? super E, ? extends E> childrenProperty, IValueProperty<? super E, ?>... labelProperties) {
		Realm realm = DisplayRealm.getRealm(viewer.getControl().getDisplay());

		ObservableSetTreeContentProvider<? extends E> contentProvider = new ObservableSetTreeContentProvider<>(
				childrenProperty.setFactory(realm), null);
		if (viewer.getInput() != null)
			viewer.setInput(null);
		viewer.setContentProvider(contentProvider);


		viewer.setLabelProvider(new ObservableMapLabelProvider(
				Properties.observeEach(contentProvider.getKnownElements(), labelProperties)));
		if (input != null)
			viewer.setInput(input);
	}
}
