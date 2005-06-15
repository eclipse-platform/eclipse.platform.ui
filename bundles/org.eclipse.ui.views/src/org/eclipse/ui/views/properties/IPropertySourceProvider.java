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
package org.eclipse.ui.views.properties;

/**
 * Interface used by {@link org.eclipse.ui.views.properties.PropertySheetEntry}
 * to obtain an {@link org.eclipse.ui.views.properties.IPropertySource} for a
 * given object.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IPropertySourceProvider {
	
	/**
	 * Returns a property source for the given object.
	 * 
	 * @param object
	 *            the object
	 * @return the property source for the object passed in (maybe
	 *         <code>null</code>)
	 */
	public IPropertySource getPropertySource(Object object);
}
