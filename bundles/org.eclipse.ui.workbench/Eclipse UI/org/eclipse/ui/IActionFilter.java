/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * An adapter which performs action filtering.
 * <p>
 * Within the workbench a plugin may extend the actions which appear in the
 * context menu for any object.  The visibility of each action extension is controlled
 * by action filtering.  By default, the workbench will filter each action extension using 
 * the <code>objectClass</code> and optional <code>nameFilter</code> attributes defined
 * in xml.  If the action extension passes this test the action will be added to the 
 * context menu for the object. 
 * </p>
 * <p>
 * In some situations the object class and name are not enough to describe the intended
 * target action.  In those situations an action extension may define one or more 
 * <code>filter</code> sub-elements.  Each one of these elements describes one attribute of 
 * the action target using a <code>name value</code> pair.  The attributes for an object 
 * are type specific and beyond the domain of the workbench itself, so the workbench 
 * will delegate filtering at this level to an <code>IActionFilter</code>.  This is a 
 * filtering strategy which is provided by the selection itself and may perform type 
 * specific filtering.
 * </p>
 * <p>
 * The workbench will retrieve the filter from the selected object by testing to see
 * if it implements <code>IActionFilter</code>.  If that fails, the workbench will ask for
 * a filter through through the <code>IAdaptable</code> mechanism.  If a filter is
 * found the workbench will pass each name value pair to the filter to determine if it 
 * matches the state of the selected object.  If so, or there is no filter, the action 
 * will be added to the context menu for the object. 
 * </p>
 * <p>
 * Clients that implement this filter mechanism are strongly encouraged to extend this
 * interface to provide a list of attribute names and possible values that are
 * considered public for other clients to reference.
 * </p>
 *
 * @see org.eclipse.core.runtime.IAdaptable
 */
public interface IActionFilter {
    /**
     * Returns whether the specific attribute matches the state of the target
     * object.
     *
     * @param target the target object
     * @param name the attribute name
     * @param value the attribute value
     * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
     */
    public boolean testAttribute(Object target, String name, String value);
}
