/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.set.DelegatingSetProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.internal.databinding.viewers.CheckableCheckedElementsProperty;
import org.eclipse.jface.internal.databinding.viewers.CheckboxTableViewerCheckedElementsProperty;
import org.eclipse.jface.internal.databinding.viewers.CheckboxTreeViewerCheckedElementsProperty;
import org.eclipse.jface.internal.databinding.viewers.SelectionProviderMultipleSelectionProperty;
import org.eclipse.jface.internal.databinding.viewers.SelectionProviderSingleSelectionProperty;
import org.eclipse.jface.internal.databinding.viewers.StructuredViewerFiltersProperty;
import org.eclipse.jface.internal.databinding.viewers.ViewerInputProperty;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * A factory for creating properties of JFace {@link Viewer viewers}.
 * 
 * @since 1.3
 */
public class ViewerProperties {
	/**
	 * Returns a set property for observing the checked elements of a
	 * {@link CheckboxTableViewer}, {@link CheckboxTreeViewer} or
	 * {@link ICheckable}.
	 * 
	 * @param elementType
	 *            the element type of the returned property
	 * 
	 * @return a set property for observing the checked elements of a
	 *         {@link CheckboxTableViewer}, {@link CheckboxTreeViewer} or
	 *         {@link ICheckable}.
	 */
	public static ISetProperty checkedElements(final Object elementType) {
		return new DelegatingSetProperty(elementType) {
			ISetProperty checkable = new CheckableCheckedElementsProperty(
					elementType);
			ISetProperty checkboxTableViewer = new CheckboxTableViewerCheckedElementsProperty(
					elementType);
			ISetProperty checkboxTreeViewer = new CheckboxTreeViewerCheckedElementsProperty(
					elementType);

			protected ISetProperty doGetDelegate(Object source) {
				if (source instanceof CheckboxTableViewer)
					return checkboxTableViewer;
				if (source instanceof CheckboxTreeViewer)
					return checkboxTreeViewer;
				return checkable;
			}
		};
	}

	/**
	 * Returns a value property for observing the input of a
	 * {@link StructuredViewer}.
	 * 
	 * @return a value property for observing the input of a
	 *         {@link StructuredViewer}.
	 */
	public static ISetProperty filters() {
		return new StructuredViewerFiltersProperty();
	}

	/**
	 * Returns a value property for observing the input of a {@link Viewer}.
	 * 
	 * @return a value property for observing the input of a {@link Viewer}.
	 */
	public static IValueProperty input() {
		return new ViewerInputProperty();
	}

	/**
	 * Returns a list property for observing the multiple selection of an
	 * {@link ISelectionProvider}.
	 * 
	 * @return a list property for observing the multiple selection of an
	 *         {@link ISelectionProvider}.
	 */
	public static IListProperty multipleSelection() {
		return new SelectionProviderMultipleSelectionProperty();
	}

	/**
	 * Returns a value property for observing the single selection of a
	 * {@link ISelectionProvider}.
	 * 
	 * @return a value property for observing the single selection of a
	 *         {@link ISelectionProvider}.
	 */
	public static IValueProperty singleSelection() {
		return new SelectionProviderSingleSelectionProperty();
	}
}
