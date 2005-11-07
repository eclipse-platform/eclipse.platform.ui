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
package org.eclipse.debug.ui.contexts;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 *
 * @since 3.2
 */
public interface IDebugContextListener {
	
	/**
	 * Notification the given selection contains the active context in the 
	 * specified part.
	 * 
	 * @param selection selection containing active context
	 * @param part workbench part or <code>null</code>
	 */
	public void contextActivated(ISelection selection, IWorkbenchPart part);

}
