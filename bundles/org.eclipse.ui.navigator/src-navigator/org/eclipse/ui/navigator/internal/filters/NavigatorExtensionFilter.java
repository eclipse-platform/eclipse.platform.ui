/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorExtensionFilter;



/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class NavigatorExtensionFilter implements INavigatorExtensionFilter {

	/**
	 *  
	 */
	public NavigatorExtensionFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.filters.INavigatorExtensionFilter#select(org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorExtensionSite,
	 *      java.lang.Object, java.lang.Object[])
	 */
	public Object[] select(CommonViewer aViewer, Object aParentElement, Object[] theElements) {
		List results = new ArrayList();
		for (int i = 0; i < theElements.length; i++)
			if (select(aViewer, aParentElement, theElements[i]))
				results.add(theElements[i]);
		return results.toArray();
	}

}