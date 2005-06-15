/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
     *         meaningful default value to which it can be resetted, and
     *         <code>false</code> otherwise
     * @see IPropertySource#resetPropertyValue(Object)
     * @see IPropertySource#isPropertySet(Object)
     */
    boolean isPropertyResettable(Object id);

    /**
     * <code>IPropertySource2</code> overrides the specification of this <code>IPropertySource</code> 
     * method to return <code>true</code> instead of <code>false</code> if the specified 
     * property does not have a meaningful default value.
     * <code>isPropertyResettable</code> will only be called if <code>isPropertySet</code> returns
     * <code>true</code>.
     * <p> 
     * Returns whether the value of the property with the given id has changed
     * from its default value. Returns <code>false</code> if this source does
     * not have the specified property.
     * </p>
     * <p>
     * If the notion of default value is not meaningful for the specified
     * property then <code>true</code> is returned.
     * </p>
     * 
     * @param id
     *            the id of the property
     * @return <code>true</code> if the value of the specified property has
     *         changed from its original default value, <code>true</code> if
     *         the specified property does not have a meaningful default value,
     *         and <code>false</code> if this source does not have the
     *         specified property
     * @see IPropertySource2#isPropertyResettable(Object)
     * @see #resetPropertyValue(Object)
     * @since 3.1
     */
    public boolean isPropertySet(Object id);
}
