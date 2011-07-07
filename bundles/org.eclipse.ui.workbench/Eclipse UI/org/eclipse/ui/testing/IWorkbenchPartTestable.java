/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.testing;

import org.eclipse.swt.widgets.Composite;

/**
 * This interface provides methods that allow introspection of workbench parts.
 * Instances may be obtained by calling
 * {@link org.eclipse.core.runtime.IAdaptable#getAdapter(Class)} on
 * {@link org.eclipse.ui.IWorkbenchPartSite}.
 * 
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @since 3.3
 */
public interface IWorkbenchPartTestable {

	/**
	 * Get the {@link org.eclipse.swt.widgets.Composite} provided to the parts
	 * {@link org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)}
	 * method.
	 * 
	 * @return the composite
	 */
	public Composite getControl();
}
