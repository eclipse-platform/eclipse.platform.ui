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

import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class FilterDescriptor implements IFilterMatcherDescriptor {
	private String id;
	private String name;
	private String description;
	private String argumentType;
	private boolean isFirst = false;
	private IConfigurationElement element;

	public FilterDescriptor(IConfigurationElement element) throws CoreException {
		this(element, true);
	}

	public FilterDescriptor(IConfigurationElement element, boolean instantiateFactory) throws CoreException {
		id = element.getAttribute("id"); //$NON-NLS-1$
		name = element.getAttribute("name"); //$NON-NLS-1$
		description = element.getAttribute("description"); //$NON-NLS-1$
		argumentType = element.getAttribute("argumentType"); //$NON-NLS-1$
		if (argumentType == null)
			argumentType = IFilterMatcherDescriptor.ARGUMENT_TYPE_NONE;
		this.element = element;
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
	public AbstractFileInfoMatcher createFilter() {
		try {
			return (AbstractFileInfoMatcher) element.createExecutableExtension("class"); //$NON-NLS-1$
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IFilterDescriptor#isFirstOrdering()
	 */
	public boolean isFirstOrdering() {
		return isFirst;
	}
}