/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.di.osgi;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;
import org.eclipse.e4.core.internal.di.shared.CoreLogger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * This helper class assumes that availability of extended services
 * does not change.  
 */
public class ProviderHelper {

	static protected Map<String, ExtendedObjectSupplier> extendedSuppliers = new HashMap<String, ExtendedObjectSupplier>();

	static {
		// in case if any extended object supplier changes, clear the supplier cache
		BundleContext bundleContext = DIActivator.getDefault().getBundleContext();
		String filter = '(' + Constants.OBJECTCLASS + '=' + ExtendedObjectSupplier.SERVICE_NAME + ')';
		try {
			bundleContext.addServiceListener(new ServiceListener() {
				public void serviceChanged(ServiceEvent event) {
					synchronized (extendedSuppliers) {
						extendedSuppliers.clear();
					}
				}
			}, filter);
		} catch (InvalidSyntaxException e) {
			// should not happen - we tested the line above
			CoreLogger.logError("Invalid filter format in the provider helper", e); //$NON-NLS-1$
		}
	}

	static public ExtendedObjectSupplier findProvider(String qualifier, PrimaryObjectSupplier objectSupplier) {
		synchronized (extendedSuppliers) {
			if (extendedSuppliers.containsKey(qualifier))
				return extendedSuppliers.get(qualifier);
			BundleContext bundleContext = DIActivator.getDefault().getBundleContext();
			try {
				String filter = '(' + ExtendedObjectSupplier.SERVICE_CONTEXT_KEY + '=' + qualifier + ')';
				ServiceReference[] refs = bundleContext.getServiceReferences(ExtendedObjectSupplier.SERVICE_NAME, filter);
				if (refs != null && refs.length > 0) {
					ExtendedObjectSupplier supplier = (ExtendedObjectSupplier) bundleContext.getService(refs[0]);
					if (objectSupplier != null) {
						IInjector injector = InjectorFactory.getDefault();
						injector.inject(supplier, objectSupplier);
					}
					extendedSuppliers.put(qualifier, supplier);
					return supplier;
				}
			} catch (InvalidSyntaxException e) {
				// should not happen - we tested the line above
			}
			extendedSuppliers.put(qualifier, null);
			return null;
		}
	}
}
