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
package org.eclipse.ui.navigator.resources.internal.workbench;

import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class ResourceExtensionContentProvider extends WorkbenchContentProvider {

	/**
	 *  
	 */
	public ResourceExtensionContentProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {
		return super.getChildren(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		return super.getChildren(element);
	}

}