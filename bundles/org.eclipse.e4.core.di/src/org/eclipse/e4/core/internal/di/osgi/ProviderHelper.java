/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.di.osgi;

import org.eclipse.e4.core.di.AbstractObjectSupplier;
import org.osgi.framework.*;

public class ProviderHelper {
	static public AbstractObjectSupplier findProvider(String qualifier) {
		BundleContext bundleContext = DIActivator.getDefault().getBundleContext();
		try {
			ServiceReference[] refs = bundleContext.getServiceReferences(AbstractObjectSupplier.SERVICE_NAME, "(" + AbstractObjectSupplier.SERVICE_CONTEXT_KEY + '=' //$NON-NLS-1$
					+ qualifier + ')');
			if (refs != null && refs.length > 0)
				return (AbstractObjectSupplier) bundleContext.getService(refs[0]);
		} catch (InvalidSyntaxException e) {
			// should not happen - we tested the line above
		}
		return null;
	}
}
