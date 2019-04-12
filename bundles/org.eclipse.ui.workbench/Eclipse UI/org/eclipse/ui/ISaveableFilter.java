/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui;

/**
 * A filter for selecting Saveables.
 * 
 * @see IWorkbench#saveAll(org.eclipse.jface.window.IShellProvider,
 *      org.eclipse.jface.operation.IRunnableContext, ISaveableFilter, boolean)
 * @since 3.3
 */
public interface ISaveableFilter {

	/**
	 * Indicate whether the given saveable matches this filter.
	 * 
	 * @param saveable        the saveable being tested
	 * @param containingParts the parts that contain the saveable. This list may
	 *                        contain zero or more parts.
	 * @return whether the given saveable matches this filter
	 */
	boolean select(Saveable saveable, IWorkbenchPart[] containingParts);

}
