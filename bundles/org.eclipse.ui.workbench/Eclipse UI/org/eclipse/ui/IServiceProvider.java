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
package org.eclipse.ui;

/**
 * <p>
 * A workbench component capable of providing services.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * 
 */
public interface IServiceProvider {
	/**
	 * <p>
	 * Returns the service of the given type. If the service does not currently
	 * exist for the component, then this returns <code>null</code>.
	 * </p>
	 * 
	 * @param type
	 *            The type of service to retrieve. The possible services
	 *            provided are defined in <code>IWorkbenchServices</code>.
	 * @return The given service, if available; <code>null</code> otherwise.
	 */
	public IService getService(int type);
}
