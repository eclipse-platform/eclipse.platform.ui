/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.progress;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * IElementCollector is a type that allows for the incremental
 * update of a collection of objects.
 */
public interface IElementCollector {

	/**
	 * Add element to the IElementCollector.
	 * @param element
	 * @param monitor
	 */
	public void add(Object element,IProgressMonitor monitor);

	/**
	 * Add elements to the IElementCollector.
	 * @param element
	 * @param monitor
	 */
	public void add(Object[] elements, IProgressMonitor monitor);

}
