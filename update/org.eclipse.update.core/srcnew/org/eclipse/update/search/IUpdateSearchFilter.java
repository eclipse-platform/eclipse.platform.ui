/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.search;

import org.eclipse.update.core.IFeature;

/**
 * Classes that implement this interface can be used to filter the
 * results of the update search.
 */
public interface IUpdateSearchFilter {
/**
 * Tests a feature according to this filter criteria.
 * @param match the feature to test
 * @return <samp>true</samp> if the feature has been accepted, <samp>false</samp> otherwise.
 */
	boolean accept(IFeature match);
}
