/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

/**
 * Extension to the standard <code>IPropertySource</code> interface.
 * <p>
 * This interface provides extended API to <code>IPropertySource</code> to
 * allow an easier indication of properties that have a default value and can be
 * resetted.
 * </p>
 * 
 * @since 3.0
 * @see org.eclipse.ui.views.properties.IPropertySource
 */
public interface IPropertySource2 extends IPropertySource {

    /**
     * Returns whether the value of the property with the specified id is
     * resettable to a default value.
     * 
     * @param id
     *            the id of the property
     * @return <code>true</code> if the property with the specified id has a
     *         meaningful default value to which it can be resetted to, and
     *         <code>false</code> otherwise
     * @see IPropertySource#resetPropertyValue(Object)
     * @see IPropertySource#isPropertySet(Object)
     */
    boolean isPropertyResettable(Object id);
}