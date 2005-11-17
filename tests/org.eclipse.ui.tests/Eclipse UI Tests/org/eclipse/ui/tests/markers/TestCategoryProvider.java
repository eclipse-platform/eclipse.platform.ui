package org.eclipse.ui.tests.markers;
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.ISubCategoryProvider;

/**
 * TestCategoryProvider is the test for categories.
 *
 */
public class TestCategoryProvider implements ISubCategoryProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.ICategoryProvider#categoryFor(org.eclipse.core.resources.IMarker)
	 */
	public String categoryFor(IMarker marker) {
		return marker.getAttribute("category", "No category");
	}

}
