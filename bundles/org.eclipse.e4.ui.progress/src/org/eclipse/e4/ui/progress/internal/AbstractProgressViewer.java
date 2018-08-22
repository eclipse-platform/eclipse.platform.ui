/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.jface.viewers.StructuredViewer;

/**
 * The AbstractProgressViewer is the abstract superclass of the viewers that
 * show progress.
 *
 */
public abstract class AbstractProgressViewer extends StructuredViewer {

	/**
	 * Add the elements to the receiver.
	 * @param elements
	 */
	public abstract void add(Object[] elements);

	/**
	 * Remove the elements from the receiver.
	 * @param elements
	 */
	public abstract void remove(Object[] elements);
}
