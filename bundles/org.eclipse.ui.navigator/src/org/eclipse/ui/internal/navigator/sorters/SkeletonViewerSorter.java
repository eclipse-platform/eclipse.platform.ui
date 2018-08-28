/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.navigator.sorters;

import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @since 3.2
 *
 */
public class SkeletonViewerSorter extends ViewerSorter {

	/** The singleton instance. */
	public static final ViewerSorter INSTANCE = new SkeletonViewerSorter();

	private SkeletonViewerSorter() {}

}
