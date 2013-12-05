/*******************************************************************************
 * Copyright (c) 2013 Markus Alexander Kuppe and others. All rights reserved. 
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.di.internal.extensions;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.SynchronousBundleListener;

public class BundleContextObjectSupplier extends ExtendedObjectSupplier {

	/**
	 * A Map of Requestor to BundleListener. Each Requestor will only ever request its own bundle and thus there is a 1:1 relationship between R and BL.
	 */
	private final Map<IRequestor, BundleListener> requestor2listener = new HashMap<IRequestor, BundleListener>();

	private final BundleContext localBundleContext = FrameworkUtil.getBundle(BundleContextObjectSupplier.class).getBundleContext();

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		final Class<?> requestingObjectClass = requestor.getRequestingObjectClass();
		final Bundle bundle = FrameworkUtil.getBundle(requestingObjectClass);

		// Cannot use BundleListener as a BL can only be registered with a BC (which might be null)
		if (track) {
			if (!requestor2listener.containsKey(requestor)) {
				track(bundle, requestor);
			}
		} else {
			untrack(requestor);
		}

		final BundleContext bundleContext = bundle.getBundleContext();
		if (bundleContext != null) {
			return bundleContext;
		} else if (descriptor.getQualifier(Optional.class) != null) {
			// Do not have a bundle context but requestor has marked the parameter/field optional
			return null;
		}
		throw new InjectionException("Unable to inject BundleContext: " + bundle.getSymbolicName() + " bundle is not active or starting/stopping"); //$NON-NLS-1$  //$NON-NLS-2$
	}

	private void untrack(final IRequestor requestor) {
		synchronized (requestor2listener) {
			BundleListener l = requestor2listener.remove(requestor);
			localBundleContext.removeBundleListener(l);
		}
	}

	private void track(final Bundle bundle, final IRequestor requestor) {
		// A _synchronous_ BundleListener asserts that the BC is un-injected,
		// _before_ it becomes invalid (state-wise). 
		BundleListener listener = new SynchronousBundleListener() {
			public void bundleChanged(BundleEvent event) {
				if (event.getBundle().equals(bundle)) {
					if (requestor.isValid()) {
						requestor.resolveArguments(false);
						requestor.execute();
					}
				}
			}
		};
		synchronized (requestor2listener) {
			localBundleContext.addBundleListener(listener);
			requestor2listener.put(requestor, listener);
		}
	}
}
