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
 *     Matthew Hall - bug 264286
 *     Ovidio Mallo - bug 270494
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers.typed;

import org.eclipse.jface.databinding.viewers.IViewerListProperty;
import org.eclipse.jface.databinding.viewers.IViewerSetProperty;
import org.eclipse.jface.databinding.viewers.IViewerValueProperty;
import org.eclipse.jface.internal.databinding.viewers.SelectionProviderMultipleSelectionProperty;
import org.eclipse.jface.internal.databinding.viewers.SelectionProviderSingleSelectionProperty;
import org.eclipse.jface.internal.databinding.viewers.StructuredViewerFiltersProperty;
import org.eclipse.jface.internal.databinding.viewers.ViewerCheckedElementsProperty;
import org.eclipse.jface.internal.databinding.viewers.ViewerInputProperty;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A factory for creating properties of JFace {@link Viewer viewers}.
 * <p>
 * This class is a new version of the deprecated class with the same name in the
 * parent package. The difference is that this class returns typed property
 * objects. This class is located in its own package to be able to coexist with
 * the old version while having the same name.
 *
 * @since 1.9
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
	public static <S extends ICheckable, E> IViewerSetProperty<S, E> checkedElements(Object elementType) {
		return new ViewerCheckedElementsProperty<>(elementType);
	}

	/**
	 * A version of {@link #checkedElements(Object)} which casts the property to the
	 * argument element type. (Note that the type arguments of the property might
	 * not be checked at runtime.)
	 *
	 * @param elementType the element type of the returned property
	 *
	 * @return a set property for observing the checked elements of a
	 *         {@link CheckboxTableViewer}, {@link CheckboxTreeViewer} or
	 *         {@link ICheckable}.
	 * @since 1.9
	 */
	public static <S extends ICheckable, T> IViewerSetProperty<S, T> checkedElements(Class<T> elementType) {
		return new ViewerCheckedElementsProperty<>(elementType);
	}

	/**
	 * Returns a value property for observing the filters of a
	 * {@link StructuredViewer}.
	 *
	 * @return a value property for observing the filters of a
	 *         {@link StructuredViewer}.
	 */
	public static <S extends StructuredViewer> IViewerSetProperty<S, ViewerFilter> filters() {
		return new StructuredViewerFiltersProperty<>();
	}

	/**
	 * Returns a value property for observing the input of a {@link Viewer}.
	 *
	 * @return a value property for observing the input of a {@link Viewer}.
	 */
	public static <S extends Viewer, E> IViewerValueProperty<S, E> input() {
		return new ViewerInputProperty<>(null);
	}

	/**
	 * An alternative version of {@link #input()} which casts the property to the
	 * argument element type. (Note that the type arguments of the property might
	 * not be checked at runtime.)
	 *
	 * @param inputType the value type of the returned property
	 *
	 * @return a value property for observing the input of a {@link Viewer}.
	 *
	 * @since 1.9
	 */
	public static <S extends Viewer, T> IViewerValueProperty<S, T> input(Class<T> inputType) {
		return new ViewerInputProperty<>(inputType);
	}

	/**
	 * Returns a list property for observing the multiple selection of an
	 * {@link ISelectionProvider}.
	 *
	 * @return a list property for observing the multiple selection of an
	 *         {@link ISelectionProvider}.
	 */
	public static <S extends ISelectionProvider, E> IViewerListProperty<S, E> multipleSelection() {
		return new SelectionProviderMultipleSelectionProperty<>(false, null);
	}

	/**
	 * An alternative version of {@link #multipleSelection()} which casts the
	 * property to the argument element type. (Note that the type arguments of the
	 * property might not be checked at runtime.)
	 *
	 * @param elementType the element type of the returned property
	 *
	 * @return a list property for observing the multiple selection of an
	 *         {@link ISelectionProvider}.
	 *
	 * @since 1.9
	 */
	public static <S extends ISelectionProvider, T> IViewerListProperty<S, T> multipleSelection(Class<T> elementType) {
		return new SelectionProviderMultipleSelectionProperty<>(false, elementType);
	}


	/**
	 * Returns a list property for observing the multiple <i>post</i> selection
	 * of an {@link IPostSelectionProvider}.
	 *
	 * @return a list property for observing the multiple <i>post</i> selection
	 *         of an {@link IPostSelectionProvider}.
	 *
	 * @since 1.4
	 */
	public static <S extends ISelectionProvider, E> IViewerListProperty<S, E> multiplePostSelection() {
		return new SelectionProviderMultipleSelectionProperty<>(true, null);
	}

	/**
	 * An alternative version of {@link #multiplePostSelection()} which casts the
	 * property to the argument element type. (Note that the type arguments of the
	 * property might not be checked at runtime.)
	 *
	 * @param elementType the element type of the returned property
	 *
	 * @return a list property for observing the multiple <i>post</i> selection of
	 *         an {@link IPostSelectionProvider}.
	 *
	 * @since 1.9
	 */
	public static <S extends ISelectionProvider, T> IViewerListProperty<S, T> multiplePostSelection(
			Class<T> elementType) {
		return new SelectionProviderMultipleSelectionProperty<>(true, elementType);
	}

	/**
	 * Returns a value property for observing the single selection of a
	 * {@link ISelectionProvider}.
	 *
	 * @return a value property for observing the single selection of a
	 *         {@link ISelectionProvider}.
	 */
	public static <S extends ISelectionProvider, E> IViewerValueProperty<S, E> singleSelection() {
		return new SelectionProviderSingleSelectionProperty<>(false, null);
	}

	/**
	 * An alternative version of {@link #singleSelection()} which casts the property
	 * to the argument element type. (Note that the type arguments of the property
	 * might not be checked at runtime.)
	 *
	 * @param elementType the element type of the returned property
	 *
	 * @return a value property for observing the single selection of a
	 *         {@link ISelectionProvider}.
	 *
	 * @since 1.9
	 */
	public static <S extends ISelectionProvider, T> IViewerValueProperty<S, T> singleSelection(Class<T> elementType) {
		return new SelectionProviderSingleSelectionProperty<>(false, elementType);
	}

	/**
	 * Returns a value property for observing the single <i>post</i> selection
	 * of a {@link IPostSelectionProvider}.
	 *
	 * @return a value property for observing the single <i>post</i> selection
	 *         of a {@link IPostSelectionProvider}.
	 *
	 * @since 1.4
	 */
	public static <S extends ISelectionProvider, E> IViewerValueProperty<S, E> singlePostSelection() {
		return new SelectionProviderSingleSelectionProperty<>(true, null);
	}

	/**
	 * An alternative version of {@link #singlePostSelection()} which casts the
	 * property to the argument element type. (Note that the type arguments of the
	 * property might not be checked at runtime.)
	 *
	 * @param elementType the element type of the returned property
	 *
	 * @return a value property for observing the single <i>post</i> selection of a
	 *         {@link IPostSelectionProvider}.
	 *
	 * @since 1.9
	 */
	public static <S extends ISelectionProvider, T> IViewerValueProperty<S, T> singlePostSelection(
			Class<T> elementType) {
		return new SelectionProviderSingleSelectionProperty<>(true, elementType);
	}
}
