/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.databinding;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Factory methods for creating observables for Workbench objects
 * 
 * @since 3.5
 */
public class WorkbenchObservables {
	/**
	 * Returns an observable with values of the given target type. If the
	 * wrapped observable's value is of the target type, or can be adapted to
	 * the target type, this is taken as the value of the returned observable,
	 * otherwise <code>null</code>.
	 * 
	 * @param master
	 *            the observable whose value should be adapted
	 * @param adapter
	 *            the target type
	 * @return an observable with values of the given type, or <code>null</code>
	 *         if the current value of the given observable does not adapt to
	 *         the target type
	 */
	public static IObservableValue observeDetailAdaptedValue(
			IObservableValue master, Class adapter) {
		return observeDetailAdaptedValue(master, adapter, Platform
				.getAdapterManager());
	}

	/**
	 * Returns an observable with values of the given target type. If the
	 * wrapped observable's value is of the target type, or can be adapted to
	 * the target type, this is taken as the value of the returned observable,
	 * otherwise <code>null</code>.
	 * 
	 * @param master
	 *            the observable whose value should be adapted
	 * @param adapter
	 *            the target type
	 * @param adapterManager
	 *            the adapter manager used to adapt the master value
	 * @return an observable with values of the given type, or <code>null</code>
	 *         if the current value of the given observable does not adapt to
	 *         the target type
	 */
	static IObservableValue observeDetailAdaptedValue(
			IObservableValue master, Class adapter,
			IAdapterManager adapterManager) {
		return WorkbenchProperties.adaptedValue(adapter, adapterManager)
				.observeDetail(master);
	}

	/**
	 * Returns an observable value that tracks the post selection of a selection
	 * service obtained through the given service locator, and adapts the first
	 * element of that selection to the given target type.
	 * <p>
	 * This method can be used by view or editor implementers to tie into the
	 * selection service, for example as follows:
	 * 
	 * <pre>
	 * IObservableValue selection = WorkbenchObservables
	 * 		.observeAdaptedSingleSelection(getSite(), IResource.class);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param locator
	 *            a service locator with an available {@link ISelectionService}
	 * @param targetType
	 *            the target type
	 * @return an observable value whose value type is the given target type
	 */
	public static IObservableValue observeAdaptedSingleSelection(
			IServiceLocator locator, Class targetType) {
		ISelectionService selectionService = (ISelectionService) locator
				.getService(ISelectionService.class);
		Assert.isNotNull(selectionService);
		return WorkbenchProperties.singleSelection(null, true).value(
				WorkbenchProperties.adaptedValue(targetType)).observe(
				selectionService);
	}
}
