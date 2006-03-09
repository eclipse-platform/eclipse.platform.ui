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
 * Adapter for suspending and resuming.
 * 
 * @since 3.2
 */
public interface IAsynchronousSuspendResumeAdapter {

	/**
	 * Asynchronously determines whether the given element can perform a resume.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canResume(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously determines whether the given element can be suspended.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canSuspend(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously determines whether the given element is suspended.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void isSuspended(Object element, IBooleanRequestMonitor monitor);
	/**
	 * Asynchronously resumes the given element.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void resume(Object element, IAsynchronousRequestMonitor monitor);
	/**
	 * Asynchronously suspends the given element.
	 *
	 * @param element element
	 * @param monitor request monitor
	 */
	public void suspend(Object element, IAsynchronousRequestMonitor monitor);
	
}
