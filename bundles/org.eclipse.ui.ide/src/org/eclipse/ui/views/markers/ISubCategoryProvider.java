package org.eclipse.ui.views.markers;
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

/**
 * ICategoryProvider is the interface for types that can provide a category
 * value for marker views.
 * @since 3.2
 * <strong>NOTE:</strong> this API is experimental and subject to change 
 * during the 3.2 development cycle.
 *
 */
public interface ISubCategoryProvider {
	
	/**
	 * Provide the name of the category for the marker. Return <code>null</code> 
	 * if no category can be determined. 
	 * @param marker
	 * @return String or  <code>null</code> 
	 */
	public String categoryFor(IMarker marker);

}
