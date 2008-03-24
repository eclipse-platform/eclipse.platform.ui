/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A simple service locator creator.
 * 
 * @since 3.4
 */
public class ServiceLocatorCreator implements IServiceLocatorCreator {

	public IServiceLocator createServiceLocator(IServiceLocator parent,
			AbstractServiceFactory factory) {
		return new ServiceLocator(parent, factory);
	}
}
