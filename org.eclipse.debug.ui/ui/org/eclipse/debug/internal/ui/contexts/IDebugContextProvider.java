/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides a debug context.
 * 
 * @since 3.2
 */
public interface IDebugContextProvider {
	
	/**
	 * Returns the part associated with this provider.
	 * 
	 * @return
	 */
	public IWorkbenchPart getPart();
	
	public void addDebugContextListener(IDebugContextListener listener);
	public void removeDebugContextListener(IDebugContextListener listener);
	
	public ISelection getActiveContext();

}
