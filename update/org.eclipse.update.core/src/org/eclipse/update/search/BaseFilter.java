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

import org.eclipse.update.core.*;

/**
 * Base filter class
 * 
 * @see UpdateSearchRequest
 * @see IUpdateSearchFilter
 */
public class BaseFilter implements IUpdateSearchFilter {
	public boolean accept(IFeature match) {
		return true;
	}
	public boolean accept(IFeatureReference match) {
		return true;
	}	
}
