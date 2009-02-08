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
package org.eclipse.e4.internal.core.services.osgi;

import org.osgi.framework.ServiceRegistration;

import org.eclipse.e4.core.services.osgi.IServiceAliasRegistry;

import org.osgi.framework.BundleContext;

import org.osgi.framework.BundleActivator;


/**
 * Bundle activator for e4 core services bundle.
 */
public class Activator implements BundleActivator {
	

	private ServiceRegistration aliasRegistration;

	public void start(BundleContext context) throws Exception {
		aliasRegistration = context.registerService(IServiceAliasRegistry.SERVICE_NAME, new ServiceAliasRegistryImpl(), null);
		
	}

	public void stop(BundleContext context) throws Exception {
		aliasRegistration.unregister();
	}

}
