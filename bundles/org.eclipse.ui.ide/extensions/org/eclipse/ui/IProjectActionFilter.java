/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * Describes the public attributes for a project and the acceptable values
 * each may have.  
 * <p>
 * A popup menu extension may use these constants to describe its object target.  
 * Each identifies an attribute name or possible value.  
 * <p>
 * Clients are not expected to implement this interface.
 * </p>
 *
 * @see org.eclipse.ui.IActionFilter
 */
public interface IProjectActionFilter extends IResourceActionFilter {

    /**
     * An attribute indicating the project nature (value <code>"nature"</code>).
     * The attribute value in xml is unconstrained.
     */
    public static final String NATURE = "nature"; //$NON-NLS-1$

    /**
     * An attribute indicating whether the project is open (value <code>"open"</code>).
     * The attribute value in xml must be one of <code>"true" or "false"</code>.
     */
    public static final String OPEN = "open"; //$NON-NLS-1$

}
