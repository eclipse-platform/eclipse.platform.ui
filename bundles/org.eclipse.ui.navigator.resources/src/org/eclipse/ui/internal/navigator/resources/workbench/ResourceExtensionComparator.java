/*******************************************************************************
 * Copyright (c) 2006, 2023 IBM Corporation and others.
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

package org.eclipse.ui.internal.navigator.resources.workbench;

import java.text.Collator;

// Sadly, there is nothing that can be done about these warnings, as
// the INavigatorSorterService has a method that returns a ViewerSorter, so
// we can't convert this to a ViewerComparator.
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * @since 3.2
 */
public class ResourceExtensionComparator extends ResourceComparator {

	private Collator icuCollator;

	/**
	 * Construct a sorter that uses the name of the resource as its sorting
	 * criteria.
	 */
	public ResourceExtensionComparator() {
		super(ResourceComparator.NAME);
		icuCollator = Collator.getInstance();
	}

	@Override
	protected int compareNames(IResource resource1, IResource resource2) {
		return icuCollator.compare(resource1.getName(), resource2.getName());
	}


}
