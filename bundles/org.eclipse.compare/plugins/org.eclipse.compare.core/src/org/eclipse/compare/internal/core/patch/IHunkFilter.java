/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.core.patch;

/**
 * Filter that is used to determine if a hunk should be applied or not
 */
public interface IHunkFilter {
	
	public static final String HUNK_FILTER_PROPERTY = "org.eclipse.compare.core.hunkFilter"; //$NON-NLS-1$
	
	/**
	 * Returns true if the given hunk should be applied
	 * @param hunk the hunk
	 * @return true if the given hunk should be applied
	 */
	public boolean select(Hunk hunk);

}
