/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.contexts;

/**
 * An instance of this interface provides support for managing contexts at the
 * <code>IWorkbench</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IWorkbench#getAdaptable
 */
public interface IWorkbenchContextSupport {

	/**
	 * Returns the compound context activation service for the workbench.
	 * 
	 * @return the compound context activation service for the workbench.
	 *         Guaranteed not to be <code>null</code>.
	 */
	ICompoundContextActivationService getCompoundContextActivationService();

	/**
	 * Returns the context manager for the workbench.
	 * 
	 * @return the context manager for the workbench. Guaranteed not to be
	 *         <code>null</code>.
	 */
	IContextManager getContextManager();
}
