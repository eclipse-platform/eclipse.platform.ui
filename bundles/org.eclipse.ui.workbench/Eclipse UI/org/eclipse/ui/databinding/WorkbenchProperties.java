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
package org.eclipse.ui.databinding;

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.ISelectionService;

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
 * @noreference
 * @deprecated This class will be removed in a future release. See
 *             https://bugs.eclipse.org/bugs/show_bug.cgi?id=546822 for more
 *             information. It has been replaced by the class
 *             {@link org.eclipse.ui.databinding.typed.WorkbenchProperties}.
 *             That class creates typed property objects, while this class
 *             creates raw property objects.
 *
 * @since 3.5
 */
@Deprecated
@SuppressWarnings("rawtypes")
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
	public static IValueProperty adaptedValue(Class adapter) {
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
	static IValueProperty adaptedValue(final Class<?> adapter, final IAdapterManager adapterManager) {
		return org.eclipse.ui.databinding.typed.WorkbenchProperties.adaptedValue(adapter, adapterManager);
	}

	/**
	 * Returns a property for observing the first element of a structured selection
	 * as exposed by {@link ISelectionService}.
	 *
	 * @return an observable value
	 */
	public static IValueProperty singleSelection() {
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
	public static IValueProperty singleSelection(String partId, boolean postSelection) {
		return org.eclipse.ui.databinding.typed.WorkbenchProperties.singleSelection(partId, postSelection);
	}

	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @return an observable value
	 */
	public static IListProperty multipleSelection() {
		return multipleSelection(null, false);
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
	public static IListProperty multipleSelection(String partId, boolean postSelection) {
		return org.eclipse.ui.databinding.typed.WorkbenchProperties.multipleSelection(partId, postSelection);
	}
}
