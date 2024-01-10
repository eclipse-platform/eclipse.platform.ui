/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.databinding.typed;

import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.databinding.ActivePageProperty;
import org.eclipse.ui.internal.databinding.ActivePartProperty;
import org.eclipse.ui.internal.databinding.ActiveWindowProperty;
import org.eclipse.ui.internal.databinding.AdaptedValueProperty;
import org.eclipse.ui.internal.databinding.EditorInputProperty;
import org.eclipse.ui.internal.databinding.MultiSelectionProperty;
import org.eclipse.ui.internal.databinding.SingleSelectionProperty;

/**
 * Factory methods for creating properties for the Workbench.
 *
 * <p>
 * Examples:
 * </p>
 *
 * <pre>
 * WorkbenchProperties.singleSelection().observe(getSite().getService(ISelectionService.class))
 * </pre>
 *
 * @since 3.117
 */
public class WorkbenchProperties {
	/**
	 * Returns a value property which observes the source object as the adapted
	 * type, using the platform adapter manager. If the source is of the target
	 * type, or can be adapted to the target type, this is used as the value of
	 * property, otherwise <code>null</code>.
	 *
	 * @param adapter the adapter class
	 * @return a value property which observes the source object as the adapted
	 *         type.
	 */
	public static <S, T> IValueProperty<S, T> adaptedValue(Class<T> adapter) {
		return adaptedValue(adapter, Platform.getAdapterManager());
	}

	/**
	 * Returns a value property which observes the source object as the adapted
	 * type. If the source object is of the target type, or can be adapted to the
	 * target type, this is used as the value of property, otherwise
	 * <code>null</code>.
	 *
	 * @param adapter        the adapter class
	 * @param adapterManager the adapter manager used to adapt source objects
	 * @return a value property which observes the source object as the adapted
	 *         type.
	 */
	public static <S, T> IValueProperty<S, T> adaptedValue(Class<T> adapter,
			final IAdapterManager adapterManager) {
		return new AdaptedValueProperty<>(adapter, adapterManager);
	}

	/**
	 * Returns a property for observing the first element of a structured selection
	 * as exposed by {@link ISelectionService}.
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, T> IValueProperty<S, T> singleSelection() {
		return singleSelection(null, false);
	}

	/**
	 * Returns a property for observing the first element of a structured selection
	 * as exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, T> IValueProperty<S, T> singleSelection(String partId,
			boolean postSelection) {
		return (IValueProperty<S, T>) singleSelection(partId, postSelection, Object.class);
	}

	/**
	 * Returns a property for observing the first element of a structured selection
	 * as exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 * @param valueType     value type of the selection
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, T> IValueProperty<S, T> singleSelection(String partId,
			boolean postSelection, Class<T> valueType) {
		return new SingleSelectionProperty<>(partId, postSelection, valueType);
	}

	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection() {
		return (IListProperty<S, E>) multipleSelection(Object.class);
	}

	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @param elementType element type of the selection
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection(Class<E> elementType) {
		return multipleSelection(null, false, elementType);
	}


	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection(String partId,
			boolean postSelection) {
		return (IListProperty<S, E>) multipleSelection(partId, postSelection, Object.class);
	}

	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 * @param elementType   type of selection elements
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection(String partId,
			boolean postSelection, Class<E> elementType) {
		return new MultiSelectionProperty<>(partId, postSelection, elementType);
	}

	/**
	 * Returns a property for observing the active window of a workbench. The value
	 * is null if there is no active window.
	 *
	 * @return the property
	 * @see IWorkbench#getActiveWorkbenchWindow
	 * @see IWorkbench#addWindowListener
	 * @since 3.120
	 */
	public static <S extends IWorkbench> IValueProperty<S, IWorkbenchWindow> activeWindow() {
		return new ActiveWindowProperty<>();
	}

	/**
	 * Returns a property for observing the active page of a workbench window. The
	 * value is null if there is no active page.
	 *
	 * @return the property
	 * @see IWorkbenchWindow#getActivePage
	 * @see IWorkbenchWindow#addPageListener
	 * @since 3.120
	 */
	public static <S extends IPageService> IValueProperty<S, IWorkbenchPage> activePage() {
		return new ActivePageProperty<>();
	}

	/**
	 * Returns a property for observing the active part reference of a part service.
	 * The value is null if there is no active part.
	 *
	 * @return the property
	 * @see IPartService#getActivePart
	 * @see IPartService#addPartListener
	 * @since 3.120
	 */
	public static <S extends IPartService> IValueProperty<S, IWorkbenchPartReference> activePartReference() {
		return new ActivePartProperty<>();
	}

	/**
	 * Returns a property for observing the active part reference of a part service,
	 * casted to {@link IEditorReference}. The value is null if the active part is
	 * not an {@code IEditorReference}. Note that this value is different from
	 * {@link IWorkbenchPage#getActiveEditor}.
	 *
	 * @return the property
	 * @see IPartService#getActivePart
	 * @see IPartService#addPartListener
	 * @since 3.120
	 */
	public static <S extends IPartService> IValueProperty<S, IEditorReference> activePartAsEditorReference() {
		return WorkbenchProperties.<S>activePartReference().value(Properties.convertedValue(IEditorReference.class,
						part -> part instanceof IEditorReference ? (IEditorReference) part : null));
	}

	/**
	 * Returns a property for observing the editor input an editor part.
	 *
	 * @return the property
	 * @see IEditorPart#getEditorInput
	 * @see IEditorPart#addPropertyListener
	 * @since 3.120
	 */
	public static <S extends IEditorPart> IValueProperty<S, IEditorInput> editorInput() {
		return new EditorInputProperty<>();
	}
}
