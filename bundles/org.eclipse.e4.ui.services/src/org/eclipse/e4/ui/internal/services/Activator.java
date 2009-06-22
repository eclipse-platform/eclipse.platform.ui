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
package org.eclipse.e4.ui.internal.services;

import org.eclipse.e4.ui.services.EHandlerService;

import org.eclipse.e4.ui.services.EContextService;

import java.util.Hashtable;

import org.eclipse.e4.core.services.context.IContextFunction;
import org.osgi.framework.*;

/**
 * 
 */
public class Activator implements BundleActivator {
	ServiceRegistration contextServiceReg, handlerServiceReg;

	/*(non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		//Register functions that will be used as factories for the handler and context services.
		//We must use this advanced technique because these service implementations need access
		//to the service they are registered with. More typically context services are registered directly as OSGi services.
		//Also note these services could be registered lazily using declarative services if needed
		Hashtable<String, String> props = new Hashtable<String, String>(4);
		props.put(IContextFunction.SERVICE_CONTEXT_KEY, EContextService.class.getName());
		contextServiceReg = context.registerService(IContextFunction.class.getName(), new ContextContextFunction(), props);
		props.put(IContextFunction.SERVICE_CONTEXT_KEY, EHandlerService.class.getName());
		handlerServiceReg = context.registerService(IContextFunction.class.getName(), new HandlerContextFunction(), props);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (contextServiceReg != null) {
			contextServiceReg.unregister();
			contextServiceReg = null;
		}
		if (handlerServiceReg != null) {
			handlerServiceReg.unregister();
			handlerServiceReg = null;
		}
	}

}
