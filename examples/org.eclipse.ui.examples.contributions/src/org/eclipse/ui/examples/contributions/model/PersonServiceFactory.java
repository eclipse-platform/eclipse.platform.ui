/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.model;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Supply the person service to the IServiceLocator framework.
 * 
 * @since 3.4
 */
public class PersonServiceFactory extends AbstractServiceFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.AbstractServiceFactory#create(java.lang.Class,
	 *      org.eclipse.ui.services.IServiceLocator,
	 *      org.eclipse.ui.services.IServiceLocator)
	 */
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if (!IPersonService.class.equals(serviceInterface)) {
			return null;
		}
		Object parentService = parentLocator.getService(IPersonService.class);
		if (parentService == null) {
			// the global level person service implementation
			return new PersonService(locator);
		}
		return new PersonServiceSlave(locator, (IPersonService) parentService);
	}

}
