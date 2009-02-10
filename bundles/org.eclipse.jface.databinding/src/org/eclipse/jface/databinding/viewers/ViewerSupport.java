/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 260337)
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
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
	public static void bind(StructuredViewer viewer, IObservableList input,
			IValueProperty labelProperty) {
		bind(viewer, input, new IValueProperty[] { labelProperty });
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
	public static void bind(StructuredViewer viewer, IObservableList input,
			IValueProperty[] labelProperties) {
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ObservableMapLabelProvider(Properties
				.observeEach(contentProvider.getKnownElements(),
						labelProperties)));
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
	public static void bind(StructuredViewer viewer, IObservableSet input,
			IValueProperty labelProperty) {
		bind(viewer, input, new IValueProperty[] { labelProperty });
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
	public static void bind(StructuredViewer viewer, IObservableSet input,
			IValueProperty[] labelProperties) {
		ObservableSetContentProvider contentProvider = new ObservableSetContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ObservableMapLabelProvider(Properties
				.observeEach(contentProvider.getKnownElements(),
						labelProperties)));
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
	public static void bind(AbstractTreeViewer viewer, Object input,
			IListProperty childrenProperty, IValueProperty labelProperty) {
		bind(viewer, input, childrenProperty,
				new IValueProperty[] { labelProperty });
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
	public static void bind(AbstractTreeViewer viewer, Object input,
			IListProperty childrenProperty, IValueProperty[] labelProperties) {
		Realm realm = SWTObservables.getRealm(viewer.getControl().getDisplay());
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				childrenProperty.listFactory(realm), null);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ObservableMapLabelProvider(Properties
				.observeEach(contentProvider.getKnownElements(),
						labelProperties)));
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
	public static void bind(AbstractTreeViewer viewer, Object input,
			ISetProperty childrenProperty, IValueProperty labelProperty) {
		bind(viewer, input, childrenProperty,
				new IValueProperty[] { labelProperty });
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
	public static void bind(AbstractTreeViewer viewer, Object input,
			ISetProperty childrenProperty, IValueProperty[] labelProperties) {
		Realm realm = SWTObservables.getRealm(viewer.getControl().getDisplay());
		ObservableSetTreeContentProvider contentProvider = new ObservableSetTreeContentProvider(
				childrenProperty.setFactory(realm), null);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ObservableMapLabelProvider(Properties
				.observeEach(contentProvider.getKnownElements(),
						labelProperties)));
		viewer.setInput(input);
	}
}
