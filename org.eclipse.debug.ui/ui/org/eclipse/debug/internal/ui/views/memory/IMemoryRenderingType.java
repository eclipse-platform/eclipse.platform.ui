/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.core.runtime.IConfigurationElement;


/**
 * Store information about a type of rendering
 * e.g.  raw memory, ascii, ebcdic, integer, etc.
 * Extension point org.eclipse.debug.ui.memoryRenderingType is
 * provided to allow plugins to contribute additional rendering
 * types.
 * @since 3.1
 */
public interface IMemoryRenderingType
{
	/**
	 * @return the name of the rendering type.
	 * Name will be used to label the view tab of the rendering
	 */
	public String getName();
	
	/**
	 * @return the id of this rendering
	 */
	public String getRenderingId();
	
	/**
	 * Given the property id, get rendering specific property
	 * Return null if the property is nto available.
	 * @param propertyId
	 * @return the value of the given property
	 */
	public String getProperty(String propertyId);
	
	/**
	 * @return the configuration element of the rendering
	 */
	public IConfigurationElement getConfigElement();
	
	/**
	 * @param propertyId
	 * @return the configuration element of the property
	 */
	public IConfigurationElement getPropertyConfigElement(String propertyId);
	
	/**
	 * @return the configuration element of all the properties
	 */
	public IConfigurationElement[] getAllProperties();
	
	/**
	 * @return view ids supported by this rendering type
	 */
	public String[] getSupportedViewIds();
}
