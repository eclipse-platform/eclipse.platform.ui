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


package org.eclipse.debug.internal.core.memory;

import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.core.runtime.IConfigurationElement;


/**
 * Implementation of IMemoryRenderingInfo
 * This class is for holding rendering information about a type
 * of rendering.
 * @since 3.0
 */
public class MemoryRenderingInfo implements IMemoryRenderingInfo
{
	private String fRenderingId;			// id of the type of rendering
	private String fName;					// name of the type of rendering
	private Hashtable fProperties;			// list of properties for the rendering type
	private IConfigurationElement fElement;	// configuration element defining this rendering type
	
	private static final String VALUE = "value"; //$NON-NLS-1$
	
	public MemoryRenderingInfo(String renderingId, String name,IConfigurationElement element)
	{
		fRenderingId = renderingId;
		fName = name;
		fElement = element;
	}
	/**
	 * @return
	 */
	public String getRenderingId()
	{
		return fRenderingId;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return fName;
	}
	
	/**
	 * Add a property to the rendering type.
	 * @param propertyId
	 * @param value
	 */
	public void addProperty(String propertyId, IConfigurationElement element){
		if (fProperties == null){
			fProperties = new Hashtable();
		}
		
		if (propertyId != null && element != null)
		{	
			fProperties.put(propertyId, element);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingInfo#getProperty(java.lang.String)
	 */
	public String getProperty(String propertyId) {
		
		if (fProperties != null)
		{
			IConfigurationElement element = (IConfigurationElement)fProperties.get(propertyId);
			
			if (element != null)
			{
				String ret = element.getAttribute(VALUE);
				return ret;
			}
			
			return null;
		}
		
		return null;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingInfo#getElement()
	 */
	public IConfigurationElement getConfigElement() {
		return fElement;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingInfo#getPropertyElement(java.lang.String)
	 */
	public IConfigurationElement getPropertyConfigElement(String propertyId) {
		if (fProperties != null)
		{
			IConfigurationElement element = (IConfigurationElement)fProperties.get(propertyId);
			return element;
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingInfo#getAllProperties()
	 */
	public IConfigurationElement[] getAllProperties() {
		
		Enumeration enumeration = fProperties.elements();
		IConfigurationElement[] elements = new IConfigurationElement[fProperties.size()];
		
		int i=0;
		while (enumeration.hasMoreElements())
		{
			elements[i] = (IConfigurationElement)enumeration.nextElement(); 
			i++;
		}
		
		return elements;
	}
	

}
