/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IFilterDescriptor;

import org.eclipse.core.resources.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @since 3.6
 */
public class FilterDescriptor implements IFilterDescriptor {
	private String id;
	private String name;
	private String description;
	private String argumentType;
	private IFileInfoFilterFactory factory;
	private boolean isFirst = false;
	
	public FilterDescriptor(IConfigurationElement element) throws CoreException {
		this(element, true);
	}
	
	public FilterDescriptor(IConfigurationElement element, boolean instantiateFactory) throws CoreException {
		id = element.getDeclaringExtension().getUniqueIdentifier();
		name = element.getAttribute("name"); //$NON-NLS-1$
		description = element.getAttribute("description"); //$NON-NLS-1$
		argumentType = element.getAttribute("argumentType"); //$NON-NLS-1$
		if (argumentType == null)
			argumentType = IFilterDescriptor.ARGUMENT_TYPE_NONE;
		if (instantiateFactory)
			factory = (IFileInfoFilterFactory ) element.createExecutableExtension("class"); //$NON-NLS-1$
		String ordering = element.getAttribute("ordering"); //$NON-NLS-1$
		if (ordering != null)
			isFirst = ordering.equals("first"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFilterDescriptor#getId()
	 */
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFilterDescriptor#getName()
	 */
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFilterDescriptor#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFilterDescriptor#getArgumentType()
	 */
	public String getArgumentType() {
		return argumentType;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFilterDescriptor#getFactory()
	 */
	public IFileInfoFilterFactory getFactory() {
		return factory;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFilterDescriptor#isFirstOrdering()
	 */
	public boolean isFirstOrdering() {
		return isFirst;
	}
}