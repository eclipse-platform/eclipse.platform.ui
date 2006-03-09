/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.provisional;

import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Adapter for enabling and disabling step filters.
 * 
 * @since 3.2
 */
public interface IAsynchronousStepFilters {

	/**
	 * Asynchronously determines whether the given element supports step filters.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void supportsStepFilters(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously determines whether step filters are enabled.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void isStepFiltersEnabled(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously enables or disables step filters on the given element.
	 *
	 * @param element element
	 * @param enabled whether step filters should be enabled
	 * @param monitor request monitor
	 */
	public void setStepFiltersEnabled(Object element, boolean enabled, IAsynchronousRequestMonitor monitor);
}
