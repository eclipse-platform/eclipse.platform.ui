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
 * Adapter for stepping.
 * 
 * @since 3.2
 */
public interface IAsynchronousStepAdapter {

	/**
	 * Asynchronously determines whether the given element can perform a step into.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canStepInto(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously determines whether the given element can perform a step over.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canStepOver(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously determines whether the given element can perform a step return.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canStepReturn(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously determines whether the given element is stepping.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void isStepping(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously performs a step into.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void stepInto(Object element, IAsynchronousRequestMonitor monitor);
	/**
	 * Asynchronously performs a step over.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void stepOver(Object element, IAsynchronousRequestMonitor monitor);
	/**
	 * Asynchronously performs a step return.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void stepReturn(Object element, IAsynchronousRequestMonitor monitor);	
}
