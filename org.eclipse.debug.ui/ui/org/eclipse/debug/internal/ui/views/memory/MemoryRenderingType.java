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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.core.runtime.IConfigurationElement;


/**
 * Implementation of IMemoryRenderingType
 * This class is for holding rendering information about a type
 * of rendering.
 * @since 3.1
 */
public class MemoryRenderingType implements IMemoryRenderingType
{
	private String fRenderingId;			// id of the type of rendering
	private String fName;					// name of the type of rendering
	private Hashtable fProperties;			// list of properties for the rendering type
	private IConfigurationElement fElement;	// configuration element defining this rendering type
	private ArrayList fViewBindings;		// supported views
	
	private static final String VALUE = "value"; //$NON-NLS-1$
	
	public MemoryRenderingType(String renderingId, String name,IConfigurationElement element)
	{
		fRenderingId = renderingId;
		fName = name;
		fElement = element;
	}
	/**
	 * @return the rendering id
	 */
	public String getRenderingId()
	{
		return fRenderingId;
	}

	/**
	 * @return the name of the rendering type
	 */
	public String getName()
	{
		return fName;
	}
	
	/**
	 * Add a property to the rendering type.
	 * @param propertyId
	 * @param element
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
	
	/**
	 * @param viewIds of the views supported by this rendering type
	 */
	public void addViewBindings(String[] viewIds)
	{
		if (viewIds == null)
			return;
		
		if (fViewBindings == null)
			fViewBindings = new ArrayList();
		
		for (int i=0; i<viewIds.length; i++)
		{
			if (!fViewBindings.contains(viewIds[i]))
				fViewBindings.add(viewIds[i]);
		}
	}
	
	/**
	 * @return view ids supported by this rendering type
	 */
	public String[] getSupportedViewIds()
	{
		if (fViewBindings == null)
			return new String[0];
		
		return (String[])fViewBindings.toArray(new String[fViewBindings.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingType#getProperty(java.lang.String)
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
	 * @see org.eclipse.debug.ui.IMemoryRenderingType#getElement()
	 */
	public IConfigurationElement getConfigElement() {
		return fElement;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingType#getPropertyElement(java.lang.String)
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
	 * @see org.eclipse.debug.ui.IMemoryRenderingType#getAllProperties()
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
