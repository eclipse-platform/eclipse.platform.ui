/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.workbench;

import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * TODO - This refers to the deprecated ResourceSorter, however we are stuck with this
 * for the time being because the CommonSorter extension point uses a ViewerSorter.
 * We should provide an option for a ViewerComparator and then we can remove this
 * class.
 * 
 * @since 3.2
 * 
 */
public class ResourceExtensionSorter extends ResourceSorter {

	/**
	 * Construct a sorter that uses the name of the resource as its sorting
	 * criteria.
	 * 
	 */
	public ResourceExtensionSorter() {
		super(ResourceSorter.NAME);
	}
}
