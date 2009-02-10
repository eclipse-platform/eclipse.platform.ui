/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 264286
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.jface.internal.databinding.viewers.SelectionProviderMultipleSelectionProperty;
import org.eclipse.jface.internal.databinding.viewers.SelectionProviderSingleSelectionProperty;
import org.eclipse.jface.internal.databinding.viewers.StructuredViewerFiltersProperty;
import org.eclipse.jface.internal.databinding.viewers.ViewerCheckedElementsProperty;
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
	public static IViewerSetProperty checkedElements(Object elementType) {
		return new ViewerCheckedElementsProperty(elementType);
	}

	/**
	 * Returns a value property for observing the input of a
	 * {@link StructuredViewer}.
	 * 
	 * @return a value property for observing the input of a
	 *         {@link StructuredViewer}.
	 */
	public static IViewerSetProperty filters() {
		return new StructuredViewerFiltersProperty();
	}

	/**
	 * Returns a value property for observing the input of a {@link Viewer}.
	 * 
	 * @return a value property for observing the input of a {@link Viewer}.
	 */
	public static IViewerValueProperty input() {
		return new ViewerInputProperty();
	}

	/**
	 * Returns a list property for observing the multiple selection of an
	 * {@link ISelectionProvider}.
	 * 
	 * @return a list property for observing the multiple selection of an
	 *         {@link ISelectionProvider}.
	 */
	public static IViewerListProperty multipleSelection() {
		return new SelectionProviderMultipleSelectionProperty();
	}

	/**
	 * Returns a value property for observing the single selection of a
	 * {@link ISelectionProvider}.
	 * 
	 * @return a value property for observing the single selection of a
	 *         {@link ISelectionProvider}.
	 */
	public static IViewerValueProperty singleSelection() {
		return new SelectionProviderSingleSelectionProperty();
	}
}
