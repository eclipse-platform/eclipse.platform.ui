/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.core.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.property.IProperty;

/**
 * An IProperty extension interface providing access to details of bean
 * properties.
 * 
 * @since 1.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBeanProperty extends IProperty {
	/**
	 * Returns the property descriptor of the bean property being observed. This
	 * method returns null in the case of anonymous properties.
	 * 
	 * @return the property descriptor of the bean property being observed
	 */
	public PropertyDescriptor getPropertyDescriptor();
}
