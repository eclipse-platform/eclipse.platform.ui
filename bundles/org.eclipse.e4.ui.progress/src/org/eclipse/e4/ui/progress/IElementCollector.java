/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.progress;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * IElementCollector is a type that allows for the incremental update of a
 * collection of objects. This used for updating trees incrementally with a
 * progress monitor so that the update can be reported.
 * <p>
 * See {@code org.eclipse.ui.progress.IDeferredWorkbenchAdapter}<br>
 * See {@code org.eclipse.ui.progress.DeferredTreeContentManager}
 *
 * @since 3.0
 */
public interface IElementCollector {
	/**
	 * Add the element to the IElementCollector. Send any progress information
	 * to monitor.
	 *
	 * @param element
	 *            The element being added
	 * @param monitor
	 *            The monitor to send updates to.
	 */
	public void add(Object element, IProgressMonitor monitor);

	/**
	 * Add the elements to the IElementCollector. Send any progress information
	 * to monitor.
	 *
	 * @param elements
	 *            The elements being added
	 * @param monitor
	 *            The monitor to send updates to.
	 */
	public void add(Object[] elements, IProgressMonitor monitor);

	/**
	 * The element collection is done. Clean up any temporary state.
	 */
	public void done();
}
