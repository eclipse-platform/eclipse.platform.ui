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
 * update of a collection of objects. This interface is not
 * intended to be implemented by other plug-ins.
 * @deprecated Use org.eclipse.ui.progress.IElementCollector
 * NOTE: This class will be deleted for 3.0
 * @since 3.0
 */
public interface IElementCollector {

	/**
	 * Add the element to the IElementCollector. Send any progress
	 * information to monitor.
	 * @param element The element being added
	 * @param monitor The monitor to send updates to.
	 */
	public void add(Object element,IProgressMonitor monitor);

	/**
	 * Add the elements to the IElementCollector. Send any progress
	 * information to monitor.
	 * @param elements The elements being added
	 * @param monitor The monitor to send updates to.
	 */
	public void add(Object[] elements, IProgressMonitor monitor);
	
	/**
	 * The element collection is done. Clean up any temporary
	 * state.
	 *
	 */
	public void done();

}
