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
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * IElementCollector is a type that allows for the incremental
 * update of a collection of objects. 
 * @see DeferredTreeContentManager
 * @since 3.0
 */
public interface IElementCollector {

	/**
	 * Add the supplied elements to the viewer and report the 
	 * progress to the monitor.
	 * @param element The element to add to the viewer
	 * @param monitor The progress monitor that can be informed of
	 * the update.
	 */
	public void add(Object element,IProgressMonitor monitor);

	/**
	 * Add the supplied elements to the viewer and report the 
	 * progress to the monitor.
	 * @param elements The elements to add to the viewer
	 * @param monitor The progress monitor that can be informed of
	 * the update.
	 */
	public void add(Object[] elements, IProgressMonitor monitor);
	
	/**
	 * The element collection is done. Clean up any temporary
	 * state.
	 */
	public void done();

}
